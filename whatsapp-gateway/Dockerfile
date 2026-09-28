FROM node:22-alpine

WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci --omit=dev --no-fund --no-audit

COPY src ./src

# o volume nasce com o dono deste diretório: sem isso o usuário node não consegue gravar as sessões
RUN mkdir -p /data/sessions && chown -R node:node /data

ENV NODE_ENV=production \
    PORT=3100 \
    SESSIONS_DIR=/data/sessions

VOLUME ["/data"]
EXPOSE 3100

USER node
CMD ["node", "src/server.js"]
