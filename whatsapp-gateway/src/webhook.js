import { config } from './config.js';
import { logger } from './logger.js';

/** Avisa o backend sobre mudanças de estado; falhas só são logadas (o backend também consulta o estado). */
export async function notify(payload) {
    if (!config.webhookUrl) {
        return;
    }

    try {
        const response = await fetch(config.webhookUrl, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', 'X-Gateway-Token': config.token },
            body: JSON.stringify(payload),
            signal: AbortSignal.timeout(5000),
        });
        if (!response.ok) {
            logger.warn({ status: response.status }, 'webhook rejeitado pelo backend');
        }
    } catch (error) {
        logger.warn({ err: error.message }, 'falha ao chamar webhook do backend');
    }
}
