import { existsSync } from 'node:fs';
import { readdir, rm } from 'node:fs/promises';
import path from 'node:path';

import makeWASocket, { Browsers, DisconnectReason, useMultiFileAuthState } from 'baileys';
import QRCode from 'qrcode';

import { config } from './config.js';
import { logger } from './logger.js';
import { notify } from './webhook.js';

export const Status = {
    DISCONNECTED: 'DISCONNECTED',
    CONNECTING: 'CONNECTING',
    WAITING_QR: 'WAITING_QR',
    CONNECTED: 'CONNECTED',
};

export class GatewayError extends Error {
    constructor(httpStatus, code, message) {
        super(message);
        this.httpStatus = httpStatus;
        this.code = code;
    }
}

const SESSION_ID = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;
const RECONNECT_DELAY_MS = 3000;

/** @type {Map<string, {sock: any, status: string, qrCode: string|null, phone: string|null, reason: string|null, stopping: boolean, authenticated: boolean}>} */
const sessions = new Map();

export function assertSessionId(id) {
    if (!SESSION_ID.test(id)) {
        throw new GatewayError(400, 'INVALID_SESSION_ID', 'sessionId deve ser um UUID');
    }
}

function sessionDir(id) {
    return path.join(config.sessionsDir, id);
}

function snapshot(id) {
    const session = sessions.get(id);
    if (!session) {
        return { sessionId: id, status: Status.DISCONNECTED, qrCode: null, phone: null, reason: null };
    }
    return {
        sessionId: id,
        status: session.status,
        qrCode: session.qrCode,
        phone: session.phone,
        reason: session.reason,
    };
}

function update(id, changes) {
    const session = sessions.get(id);
    Object.assign(session, changes);
    notify({ event: 'connection', ...snapshot(id) });
}

export function getSession(id) {
    return snapshot(id);
}

export async function connect(id) {
    const existing = sessions.get(id);
    if (existing && existing.status !== Status.DISCONNECTED) {
        return snapshot(id);
    }

    const { state, saveCreds } = await useMultiFileAuthState(sessionDir(id));
    const sock = makeWASocket({
        auth: state,
        browser: Browsers.ubuntu('Olie'),
        logger: logger.child({ sessionId: id, module: 'baileys' }, { level: 'warn' }),
        markOnlineOnConnect: false,
        syncFullHistory: false,
    });

    const authenticated = Boolean(state.creds.registered || state.creds.me);
    sessions.set(id, {
        sock,
        status: Status.CONNECTING,
        qrCode: null,
        phone: null,
        reason: null,
        stopping: false,
        authenticated,
    });
    notify({ event: 'connection', ...snapshot(id) });

    sock.ev.on('creds.update', saveCreds);
    sock.ev.on('connection.update', (event) => handleConnectionUpdate(id, sock, event));

    return snapshot(id);
}

async function handleConnectionUpdate(id, sock, { connection, lastDisconnect, qr }) {
    const session = sessions.get(id);
    // eventos de um socket antigo (já substituído por reconexão) são ignorados
    if (!session || session.sock !== sock) {
        return;
    }

    if (qr) {
        update(id, { status: Status.WAITING_QR, qrCode: await QRCode.toDataURL(qr), reason: null });
    }

    if (connection === 'open') {
        const phone = sock.user?.id ? '+' + sock.user.id.split(':')[0].split('@')[0] : null;
        update(id, { status: Status.CONNECTED, qrCode: null, phone, reason: null, authenticated: true });
        logger.info({ sessionId: id }, 'sessão conectada');
    }

    if (connection === 'close') {
        const code = lastDisconnect?.error?.output?.statusCode;
        logger.info({ sessionId: id, code }, 'conexão encerrada');

        if (session.stopping) {
            return;
        }

        if (code === DisconnectReason.loggedOut) {
            // o usuário desconectou o aparelho pelo celular: a sessão salva não vale mais
            await rm(sessionDir(id), { recursive: true, force: true });
            update(id, { status: Status.DISCONNECTED, qrCode: null, phone: null, reason: 'LOGGED_OUT' });
            return;
        }

        if (!session.authenticated && code === DisconnectReason.timedOut) {
            // os QR codes gerados expiraram sem leitura: para de gerar até o usuário pedir de novo
            update(id, { status: Status.DISCONNECTED, qrCode: null, reason: 'QR_TIMEOUT' });
            return;
        }

        update(id, { status: Status.DISCONNECTED, qrCode: null, reason: 'CONNECTION_LOST' });
        const delay = code === DisconnectReason.restartRequired ? 0 : RECONNECT_DELAY_MS;
        setTimeout(() => {
            const current = sessions.get(id);
            if (current && current.sock === sock && !current.stopping) {
                connect(id).catch((error) => logger.error({ sessionId: id, err: error.message }, 'falha ao reconectar'));
            }
        }, delay);
    }
}

export async function logout(id) {
    const session = sessions.get(id);
    if (session) {
        session.stopping = true;
        try {
            await session.sock.logout();
        } catch {
            session.sock.end(undefined);
        }
        sessions.delete(id);
    }
    await rm(sessionDir(id), { recursive: true, force: true });
    notify({ event: 'connection', ...snapshot(id), reason: 'USER_LOGOUT' });
    return snapshot(id);
}

function requireConnected(id) {
    const session = sessions.get(id);
    if (!session || session.status !== Status.CONNECTED) {
        throw new GatewayError(409, 'NOT_CONNECTED', 'WhatsApp não está conectado');
    }
    return session;
}

export async function sendMessage(id, { to, text, image, mimetype, caption }) {
    if (!/^\+[1-9]\d{7,14}$/.test(to ?? '')) {
        throw new GatewayError(400, 'INVALID_PHONE', 'to deve estar no formato E.164');
    }
    if (!text && !image) {
        throw new GatewayError(400, 'EMPTY_MESSAGE', 'informe text ou image');
    }

    const session = requireConnected(id);

    let results;
    try {
        results = await session.sock.onWhatsApp(to.slice(1));
    } catch (error) {
        throw new GatewayError(502, 'LOOKUP_FAILED', `falha ao verificar o número: ${error.message}`);
    }
    const target = results?.find((result) => result.exists);
    if (!target) {
        throw new GatewayError(422, 'NOT_ON_WHATSAPP', 'o número não tem conta no WhatsApp');
    }

    const content = image
        ? { image: Buffer.from(image, 'base64'), mimetype: mimetype ?? 'image/jpeg', caption: caption ?? undefined }
        : { text };

    try {
        const sent = await session.sock.sendMessage(target.jid, content);
        return { messageId: sent?.key?.id ?? null };
    } catch (error) {
        throw new GatewayError(502, 'SEND_FAILED', error.message);
    }
}

/** Reabre as sessões que já têm credenciais salvas, para não pedir QR code a cada reinício. */
export async function restoreSessions() {
    if (!existsSync(config.sessionsDir)) {
        return;
    }
    const entries = await readdir(config.sessionsDir, { withFileTypes: true });
    for (const entry of entries) {
        if (entry.isDirectory() && SESSION_ID.test(entry.name)
                && existsSync(path.join(sessionDir(entry.name), 'creds.json'))) {
            logger.info({ sessionId: entry.name }, 'restaurando sessão salva');
            await connect(entry.name).catch((error) =>
                logger.error({ sessionId: entry.name, err: error.message }, 'falha ao restaurar sessão'));
        }
    }
}
