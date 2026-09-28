import { createServer } from 'node:http';
import { timingSafeEqual } from 'node:crypto';

import { config } from './config.js';
import { logger } from './logger.js';
import { GatewayError, assertSessionId, connect, getSession, logout, restoreSessions, sendMessage } from './sessions.js';

const MAX_BODY_BYTES = 25 * 1024 * 1024;

function authorized(request) {
    const received = Buffer.from(request.headers['x-gateway-token'] ?? '');
    const expected = Buffer.from(config.token);
    return received.length === expected.length && timingSafeEqual(received, expected);
}

async function readJson(request) {
    const chunks = [];
    let size = 0;
    for await (const chunk of request) {
        size += chunk.length;
        if (size > MAX_BODY_BYTES) {
            throw new GatewayError(413, 'BODY_TOO_LARGE', 'corpo da requisição muito grande');
        }
        chunks.push(chunk);
    }
    if (size === 0) {
        return {};
    }
    try {
        return JSON.parse(Buffer.concat(chunks).toString('utf8'));
    } catch {
        throw new GatewayError(400, 'INVALID_JSON', 'JSON inválido');
    }
}

function send(response, status, body) {
    response.writeHead(status, { 'Content-Type': 'application/json' });
    response.end(body === undefined ? '' : JSON.stringify(body));
}

async function route(request, response) {
    const url = new URL(request.url, 'http://localhost');
    const parts = url.pathname.split('/').filter(Boolean);

    if (request.method === 'GET' && url.pathname === '/health') {
        return send(response, 200, { status: 'ok' });
    }

    if (!authorized(request)) {
        return send(response, 401, { code: 'UNAUTHORIZED', message: 'token inválido' });
    }

    // /sessions/:id[/connect|/messages]
    if (parts[0] !== 'sessions' || !parts[1]) {
        return send(response, 404, { code: 'NOT_FOUND', message: 'rota inexistente' });
    }
    const id = parts[1];
    assertSessionId(id);
    const action = parts[2];

    if (!action && request.method === 'GET') {
        return send(response, 200, getSession(id));
    }
    if (!action && request.method === 'DELETE') {
        return send(response, 200, await logout(id));
    }
    if (action === 'connect' && request.method === 'POST') {
        return send(response, 200, await connect(id));
    }
    if (action === 'messages' && request.method === 'POST') {
        return send(response, 200, await sendMessage(id, await readJson(request)));
    }

    return send(response, 404, { code: 'NOT_FOUND', message: 'rota inexistente' });
}

const server = createServer((request, response) => {
    route(request, response).catch((error) => {
        if (error instanceof GatewayError) {
            return send(response, error.httpStatus, { code: error.code, message: error.message });
        }
        logger.error({ err: error }, 'erro inesperado');
        return send(response, 500, { code: 'INTERNAL_ERROR', message: error.message });
    });
});

server.listen(config.port, () => {
    logger.info({ port: config.port }, 'whatsapp-gateway ouvindo');
    restoreSessions().catch((error) => logger.error({ err: error.message }, 'falha ao restaurar sessões'));
});

for (const signal of ['SIGINT', 'SIGTERM']) {
    process.on(signal, () => server.close(() => process.exit(0)));
}
