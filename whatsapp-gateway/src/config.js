export const config = {
    port: Number(process.env.PORT ?? 3100),
    // segredo compartilhado com o backend: chamadas sem ele são rejeitadas (e o webhook o envia de volta)
    token: process.env.WHATSAPP_GATEWAY_TOKEN ?? '',
    sessionsDir: process.env.SESSIONS_DIR ?? './data/sessions',
    webhookUrl: process.env.OLIE_WEBHOOK_URL ?? '',
    logLevel: process.env.LOG_LEVEL ?? 'info',
};

if (!config.token) {
    console.error('WHATSAPP_GATEWAY_TOKEN não definido — recusando iniciar sem autenticação');
    process.exit(1);
}
