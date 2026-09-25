# Olie API — Documentação de Endpoints

Documentação para integração do frontend com a API do Olie.

- **Base URL local**: `http://localhost:8090` (porta configurável via `SERVER_PORT`)
- **Prefixo de todas as rotas REST**: `/api/v1`
- **Formato**: JSON (`Content-Type: application/json`) em todos os requests e responses
- **Autenticação**: JWT Bearer token (ver seção [Autenticação](#autenticação))

> Autenticação está **ativa**. Todo endpoint listado abaixo exige o header `Authorization: Bearer <token>`, exceto os marcados como **público**.

---

## Sumário

- [Autenticação](#autenticação)
- [Convenções gerais](#convenções-gerais)
- [Usuário atual](#usuário-atual)
- [Categorias](#categorias)
- [Tasks (quadro Kanban)](#tasks-quadro-kanban)
- [Transações (receitas e despesas)](#transações-receitas-e-despesas)
- [Itens planejados](#itens-planejados)
- [Metas de economia](#metas-de-economia)
- [Controle de desgaste de itens](#controle-de-desgaste-de-itens)
- [Notas](#notas)
- [Notificações (histórico)](#notificações-histórico)
- [Notificações em tempo real (WebSocket)](#notificações-em-tempo-real-websocket)

---

## Autenticação

### `POST /api/v1/auth/register` — público

Cria um novo usuário e já retorna um token de acesso.

**Request body**
```json
{
  "name": "Jonathas Cardoso",
  "email": "jonathas@example.com",
  "password": "senha-com-8-ou-mais-caracteres"
}
```
| Campo | Tipo | Regras |
|---|---|---|
| `name` | string | obrigatório |
| `email` | string | obrigatório, formato de e-mail, único |
| `password` | string | obrigatório, mínimo 8 caracteres |

**Response `201 Created`**
```json
{ "token": "eyJhbGciOi...", "tokenType": "Bearer" }
```

### `POST /api/v1/auth/login` — público

**Request body**
```json
{ "email": "jonathas@example.com", "password": "senha" }
```

**Response `200 OK`**
```json
{ "token": "eyJhbGciOi...", "tokenType": "Bearer" }
```

### `POST /api/v1/auth/logout`

Invalida o token atual (revogação server-side — o token deixa de funcionar mesmo antes de expirar naturalmente). Requer o header `Authorization` com o próprio token a ser invalidado.

**Response**: `204 No Content`

Chamar `/logout` sem header, ou com um token já expirado/revogado/inválido, retorna `401 Unauthorized`.

### Usando o token

Em toda requisição autenticada, envie:
```
Authorization: Bearer eyJhbGciOi...
```

O token expira em **60 minutos** por padrão (`JWT_EXPIRATION_MINUTES`). Não há endpoint de refresh — ao expirar (ou após logout), é necessário logar novamente.

**Erros comuns**
| Status | Quando ocorre |
|---|---|
| `401 Unauthorized` | token ausente, inválido, expirado ou revogado (pós-logout); também credenciais inválidas no login |
| `400 Bad Request` | payload de registro/validação inválido (ex.: e-mail já cadastrado, senha curta) |

---

## Convenções gerais

- **CORS**: a API libera CORS apenas para as origens configuradas em `CORS_ALLOWED_ORIGINS` (padrão local: `http://localhost:8123`, `http://localhost:3000`, `http://localhost:5173`). Se o frontend rodar em outra porta/host, peça para adicionar a origem nessa variável de ambiente.
- **Escopo por usuário**: todo recurso (categorias, tasks, transações, itens planejados, metas, itens de desgaste, notas, notificações) pertence ao usuário autenticado. Tentar acessar/editar/excluir um recurso de outro usuário retorna `404 Not Found` (não `403`, para não vazar a existência do recurso).
- **IDs**: todos os identificadores são UUID (string).
- **Datas**:
  - Campos `date`/`estimatedDate`/`purchaseDate`/`installationDate`/`removalDate`/`estimatedReplacementDate` são `LocalDate` no formato `"YYYY-MM-DD"`.
  - Campos `createdAt`/`updatedAt`/`sentAt` são `Instant` no formato ISO-8601 UTC (`"2026-09-16T14:30:00Z"`).
- **Valores monetários** (`value`, `amount`, `estimatedValue`, `purchaseValue`, `suggestedValue`) são números decimais (string ou number conforme serialização padrão do Jackson para `BigDecimal` — trate como number).
- **Erros de validação** (`@Valid` nos bodies) retornam `400 Bad Request` com o corpo de erro padrão do Spring Boot.
- **Recurso não encontrado / não pertence ao usuário**: `404 Not Found`.
- **Criação bem-sucedida**: `201 Created` com o recurso no corpo.
- **Atualização bem-sucedida**: `200 OK` com o recurso atualizado no corpo.
- **Exclusão bem-sucedida**: `204 No Content`.

---

## Usuário atual

### `GET /api/v1/me`

**Response `200 OK`**
```json
{ "id": "uuid", "name": "Jonathas Cardoso", "email": "jonathas@example.com", "role": "USER" }
```
`role` é `USER` ou `ADMIN` (hoje sem restrições de endpoint por role).

---

## Categorias

Categorias têm no máximo **um nível de subcategoria** (uma subcategoria não pode ter subcategorias).

| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/api/v1/categories` | Lista as categorias do usuário |
| `POST` | `/api/v1/categories` | Cria uma categoria |
| `PUT` | `/api/v1/categories/{id}` | Atualiza uma categoria |
| `DELETE` | `/api/v1/categories/{id}` | Remove uma categoria |

**Request body** (`POST`/`PUT`)
```json
{ "name": "Alimentação", "parentId": null }
```
| Campo | Tipo | Regras |
|---|---|---|
| `name` | string | obrigatório |
| `parentId` | UUID \| null | opcional; deve ser uma categoria do próprio usuário, sem pai (não pode apontar para uma subcategoria) |

**Response**
```json
{ "id": "uuid", "name": "Alimentação", "parentId": null }
```

**Regras de negócio**
- `400 Bad Request` se tentar usar uma subcategoria como `parentId` (subcategoria não pode ter subcategorias).
- `400 Bad Request` se uma categoria com subcategorias for definida como subcategoria de outra.
- `400 Bad Request` se `parentId` for o próprio id da categoria.
- `404 Not Found` se `parentId` não existir/pertencer a outro usuário.

---

## Tasks (quadro Kanban)

Board com três colunas fixas: `TODO`, `DOING`, `DONE`.

| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/api/v1/tasks` | Retorna o board completo, agrupado por coluna |
| `POST` | `/api/v1/tasks` | Cria uma task (entra em `TODO`) |
| `PUT` | `/api/v1/tasks/{id}` | Atualiza título/descrição |
| `PATCH` | `/api/v1/tasks/{id}/move` | Move a task entre colunas/posições |
| `DELETE` | `/api/v1/tasks/{id}` | Remove uma task |

**Request body** (`POST`/`PUT`)
```json
{ "title": "Revisar orçamento do mês", "description": "opcional" }
```

**Request body** (`PATCH .../move`)
```json
{ "status": "DOING", "position": 0 }
```
`status`: `TODO` | `DOING` | `DONE`. `position` é a posição (zero-based) dentro da coluna de destino.

**Response de um item** (`TaskResponse`)
```json
{
  "id": "uuid",
  "title": "Revisar orçamento do mês",
  "description": "opcional",
  "status": "TODO",
  "position": 0,
  "createdAt": "2026-09-16T14:30:00Z"
}
```

**Response de `GET /api/v1/tasks`** (`BoardResponse`)
```json
{ "todo": [ /* TaskResponse[] */ ], "doing": [ /* ... */ ], "done": [ /* ... */ ] }
```

---

## Transações (receitas e despesas)

| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/api/v1/transactions` | Lista as transações do usuário |
| `GET` | `/api/v1/transactions/balance` | Saldo total (soma de `INCOME` − soma de `EXPENSE`) |
| `POST` | `/api/v1/transactions` | Registra uma receita ou despesa |
| `PUT` | `/api/v1/transactions/{id}` | Atualiza uma transação |
| `DELETE` | `/api/v1/transactions/{id}` | Remove uma transação |

**Request body** (`POST`/`PUT`)
```json
{
  "value": 150.90,
  "date": "2026-09-16",
  "paymentMethod": "Cartão de crédito",
  "type": "EXPENSE",
  "categoryId": "uuid ou null"
}
```
| Campo | Tipo | Regras |
|---|---|---|
| `value` | number | obrigatório, positivo |
| `date` | date (`YYYY-MM-DD`) | obrigatório — data efetiva da receita/despesa (pode ser retroativa ou futura) |
| `paymentMethod` | string | obrigatório (texto livre, ex.: "Pix", "Dinheiro", "Cartão de débito") |
| `type` | string | obrigatório: `INCOME` (receita) ou `EXPENSE` (despesa) |
| `categoryId` | UUID \| null | opcional; deve ser uma categoria do próprio usuário |

**Response**
```json
{
  "id": "uuid",
  "value": 150.90,
  "date": "2026-09-16",
  "paymentMethod": "Cartão de crédito",
  "type": "EXPENSE",
  "categoryId": "uuid",
  "createdAt": "2026-09-16T14:30:00Z"
}
```
`createdAt` é o instante em que o registro foi criado no sistema (auditoria) — não confundir com `date`, a data da transação escolhida pelo usuário.

**Response de `GET /api/v1/transactions/balance`**
```json
{ "balance": 1234.56 }
```

---

## Itens planejados

Representa uma compra/gasto futuro planejado (ex.: "trocar o notebook"), com prioridade e vínculo opcional a uma categoria.

| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/api/v1/planned-items` | Lista os itens planejados do usuário |
| `POST` | `/api/v1/planned-items` | Cria um item planejado |
| `PUT` | `/api/v1/planned-items/{id}` | Atualiza um item planejado |
| `DELETE` | `/api/v1/planned-items/{id}` | Remove um item planejado |
| `PATCH` | `/api/v1/planned-items/{id}/complete` | **Efetiva a compra**: gera uma transação e remove o item da lista de planejados |

**Request body** (`POST`/`PUT`)
```json
{
  "name": "Trocar o notebook",
  "priority": "ESSENTIAL",
  "estimatedValue": 4500.00,
  "estimatedDate": "2026-12-01",
  "categoryId": "uuid ou null"
}
```
| Campo | Tipo | Regras |
|---|---|---|
| `name` | string | obrigatório |
| `priority` | string | obrigatório: `ESSENTIAL`, `DESIRABLE` ou `SUPERFLUOUS` |
| `estimatedValue` | number | obrigatório, positivo |
| `estimatedDate` | date | obrigatório |
| `categoryId` | UUID \| null | opcional |

**Response** (`PlannedItemResponse`)
```json
{
  "id": "uuid",
  "name": "Trocar o notebook",
  "priority": "ESSENTIAL",
  "estimatedValue": 4500.00,
  "estimatedDate": "2026-12-01",
  "categoryId": "uuid"
}
```

### Efetivar a compra — `PATCH /api/v1/planned-items/{id}/complete`

Marca a compra como concluída: cria automaticamente uma transação (`EXPENSE`, com a data de hoje e a categoria do item planejado) e **remove** o item da lista de planejados. É assim que a compra "migra" para o histórico de transações.

**Request body**
```json
{ "paymentMethod": "Pix", "value": 4300.00 }
```
| Campo | Tipo | Regras |
|---|---|---|
| `paymentMethod` | string | obrigatório |
| `value` | number | opcional — se omitido, usa o `estimatedValue` do item planejado |

**Response `200 OK`** — retorna a `TransactionResponse` criada (ver seção [Transações](#transações-receitas-e-despesas)).

> Se o item planejado estiver vinculado a uma meta de economia (`SavingsGoal.plannedItemId`), a meta **não** é excluída ao efetivar a compra — apenas perde o vínculo (`plannedItemId` passa a `null`).

---

## Metas de economia

Metas do tipo "guardar X por período", vinculadas ou não a um item planejado.

| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/api/v1/savings-goals` | Lista as metas do usuário |
| `POST` | `/api/v1/savings-goals` | Cria uma meta |
| `PUT` | `/api/v1/savings-goals/{id}` | Atualiza uma meta |
| `DELETE` | `/api/v1/savings-goals/{id}` | Remove uma meta |

**Request body** (`POST`/`PUT`)
```json
{
  "name": "Viagem de fim de ano",
  "amount": 500.00,
  "period": "MONTHLY",
  "plannedItemId": "uuid ou null"
}
```
| Campo | Tipo | Regras |
|---|---|---|
| `name` | string | obrigatório |
| `amount` | number | obrigatório, positivo — valor a guardar por período |
| `period` | string | obrigatório: `DAILY`, `WEEKLY`, `MONTHLY`, `QUARTERLY`, `SEMI_ANNUAL` ou `ANNUAL` |
| `plannedItemId` | UUID \| null | opcional; vincula a meta a um item planejado do próprio usuário |

**Response**
```json
{ "id": "uuid", "name": "Viagem de fim de ano", "amount": 500.00, "period": "MONTHLY", "plannedItemId": "uuid" }
```

---

## Controle de desgaste de itens

Acompanha itens que se desgastam com o uso e precisam ser recomprados periodicamente (ex.: filtro de água, pneus, escova de dentes, colchão). Para cada item fica registrado **quando foi comprado** e **quando foi instalado/começou a ser usado**, e a API estima **quando será preciso comprar de novo**.

Cada item tem um **ciclo atual** (a unidade em uso) e um **histórico de ciclos** (unidades anteriores já substituídas). Conforme o histórico cresce, a estimativa passa a usar a durabilidade real observada em vez da vida útil informada manualmente.

| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/api/v1/wear-items` | Lista os itens do usuário, com a estimativa de recompra calculada |
| `GET` | `/api/v1/wear-items/{id}` | Detalha um item, incluindo o histórico de ciclos |
| `POST` | `/api/v1/wear-items` | Cadastra um item (e seu primeiro ciclo) |
| `PUT` | `/api/v1/wear-items/{id}` | Atualiza os dados do item e do ciclo atual |
| `DELETE` | `/api/v1/wear-items/{id}` | Remove o item e todo o seu histórico |
| `POST` | `/api/v1/wear-items/{id}/replace` | **Registra a troca**: encerra o ciclo atual e inicia um novo |

**Request body** (`POST`/`PUT`)
```json
{
  "name": "Filtro de água da cozinha",
  "categoryId": "uuid ou null",
  "expectedLifespan": 6,
  "expectedLifespanUnit": "MONTHS",
  "purchaseDate": "2026-03-10",
  "installationDate": "2026-03-15",
  "purchaseValue": 89.90
}
```
| Campo | Tipo | Regras |
|---|---|---|
| `name` | string | obrigatório |
| `categoryId` | UUID \| null | opcional; deve ser uma categoria do próprio usuário |
| `expectedLifespan` | integer | obrigatório, positivo — vida útil esperada (fabricante/experiência) |
| `expectedLifespanUnit` | string | obrigatório: `DAYS`, `WEEKS`, `MONTHS` ou `YEARS` |
| `purchaseDate` | date | obrigatório — data de compra da unidade atual; não pode ser futura |
| `installationDate` | date \| null | opcional — data em que começou a ser usada; se `null`, o item é considerado **comprado mas ainda não instalado** (em estoque). Deve ser `>= purchaseDate` e não pode ser futura |
| `purchaseValue` | number \| null | opcional, positivo — usado para sugerir o valor da próxima compra |

No `PUT`, os campos `purchaseDate`, `installationDate` e `purchaseValue` alteram o **ciclo atual** (correção de dados); para registrar uma nova unidade use `POST .../replace`.

**Response** (`WearItemResponse`)
```json
{
  "id": "uuid",
  "name": "Filtro de água da cozinha",
  "categoryId": "uuid",
  "expectedLifespan": 6,
  "expectedLifespanUnit": "MONTHS",
  "currentCycle": {
    "id": "uuid",
    "purchaseDate": "2026-03-10",
    "installationDate": "2026-03-15",
    "purchaseValue": 89.90
  },
  "estimate": {
    "lifespanDays": 176,
    "source": "HISTORY",
    "estimatedReplacementDate": "2026-09-07",
    "daysRemaining": -18,
    "wearPercentage": 110,
    "status": "OVERDUE",
    "suggestedValue": 92.45
  },
  "cyclesCount": 3,
  "createdAt": "2026-01-05T14:30:00Z"
}
```

**Campos de `estimate`** (calculados pelo backend a cada leitura, não persistidos)
| Campo | Descrição |
|---|---|
| `lifespanDays` | vida útil usada no cálculo, em dias |
| `source` | `EXPECTED` — usa `expectedLifespan` (sem histórico ainda); `HISTORY` — usa a média de duração real dos ciclos anteriores |
| `estimatedReplacementDate` | `installationDate` do ciclo atual + `lifespanDays`. `null` se o item ainda não foi instalado |
| `daysRemaining` | dias entre hoje e `estimatedReplacementDate` (negativo = já passou). `null` se não instalado |
| `wearPercentage` | dias em uso ÷ `lifespanDays` × 100, arredondado (pode passar de 100). `null` se não instalado |
| `status` | `IN_STOCK` (comprado, não instalado), `OK`, `NEAR_END` (faltam 15 dias ou menos, ou desgaste `>= 80%`) ou `OVERDUE` (passou da data estimada) |
| `suggestedValue` | média de `purchaseValue` dos ciclos (atual + anteriores) que têm valor; `null` se nenhum tiver |

**Regras de estimativa**
- A duração real de um ciclo encerrado é `removalDate − installationDate` (em dias). Ciclos que nunca foram instalados não entram na média.
- Com **1 ou mais** ciclos encerrados válidos, `source = HISTORY` e `lifespanDays` é a média das durações reais; caso contrário, `source = EXPECTED` e `lifespanDays` é `expectedLifespan` convertido para dias (`WEEKS` = 7, `MONTHS` = 30, `YEARS` = 365).

**Query params de `GET /api/v1/wear-items`** (todos opcionais)
| Param | Descrição |
|---|---|
| `status` | filtra por `estimate.status` (ex.: `?status=NEAR_END&status=OVERDUE`) |
| `categoryId` | filtra por categoria |

A lista vem ordenada por `estimatedReplacementDate` crescente (itens mais próximos da troca primeiro; itens `IN_STOCK` ao final).

### Detalhe com histórico — `GET /api/v1/wear-items/{id}`

Retorna o `WearItemResponse` acrescido de `history`, com os ciclos encerrados do mais recente para o mais antigo:
```json
{
  "...": "campos do WearItemResponse",
  "history": [
    {
      "id": "uuid",
      "purchaseDate": "2025-09-01",
      "installationDate": "2025-09-02",
      "removalDate": "2026-03-15",
      "purchaseValue": 95.00,
      "lifespanDays": 194
    }
  ]
}
```

### Registrar a troca — `POST /api/v1/wear-items/{id}/replace`

Encerra o ciclo atual (preenche `removalDate`, move para o histórico) e cria um novo ciclo com a unidade nova. Opcionalmente registra a despesa como transação.

**Request body**
```json
{
  "purchaseDate": "2026-09-20",
  "installationDate": "2026-09-22",
  "removalDate": "2026-09-22",
  "purchaseValue": 94.90,
  "paymentMethod": "Pix"
}
```
| Campo | Tipo | Regras |
|---|---|---|
| `purchaseDate` | date | obrigatório — compra da nova unidade; não pode ser futura |
| `installationDate` | date \| null | opcional — instalação da nova unidade; `null` = comprada e guardada (`IN_STOCK`). Deve ser `>= purchaseDate` |
| `removalDate` | date \| null | opcional — quando a unidade antiga saiu de uso. Se omitido, usa `installationDate` da nova unidade ou, na falta dela, a data de hoje. Deve ser `>= installationDate` do ciclo atual (ou `>= purchaseDate`, se o ciclo atual ainda estiver em estoque) |
| `purchaseValue` | number \| null | opcional, positivo |
| `paymentMethod` | string \| null | opcional — se informado **junto com `purchaseValue`**, cria automaticamente uma transação `EXPENSE` com `date = purchaseDate`, o valor e a categoria do item |

**Response `200 OK`** — o `WearItemResponse` atualizado (com o novo ciclo e a estimativa recalculada).

**Regras de negócio**
- `400 Bad Request` se as datas violarem a ordem `purchaseDate <= installationDate`, se `purchaseDate`/`installationDate` forem futuras, ou se `removalDate` for anterior à instalação do ciclo atual.
- `400 Bad Request` se `paymentMethod` for enviado sem `purchaseValue`.
- Um ciclo atual ainda não instalado (`IN_STOCK`) pode ser substituído; ele vai para o histórico mas não entra no cálculo da média.

> **Notificação**: a verificação periódica de notificações também passa a gerar `WEAR_ITEM_REPLACEMENT_APPROACHING` quando um item entra em `NEAR_END` (ou já está `OVERDUE`), uma única vez por ciclo — editar o item ou registrar uma troca reabilita o alerta (ver [Notificações](#notificações-histórico)).

---

## Notas

Bloco de notas livre — texto simples, sem vínculo com nenhuma outra entidade.

| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/api/v1/notes` | Lista as notas do usuário |
| `POST` | `/api/v1/notes` | Cria uma nota |
| `PUT` | `/api/v1/notes/{id}` | Atualiza o conteúdo de uma nota |
| `DELETE` | `/api/v1/notes/{id}` | Remove uma nota |

**Request body** (`POST`/`PUT`)
```json
{ "content": "Lembrar de renegociar a assinatura da academia." }
```

**Response**
```json
{
  "id": "uuid",
  "content": "Lembrar de renegociar a assinatura da academia.",
  "createdAt": "2026-09-16T14:30:00Z",
  "updatedAt": "2026-09-16T14:30:00Z"
}
```

---

## Notificações (histórico)

Histórico somente-leitura das notificações já geradas pelo sistema (ver próxima seção para o recebimento em tempo real).

| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/api/v1/notifications` | Lista as notificações do usuário, mais recentes primeiro |

**Response**
```json
[
  {
    "id": "uuid",
    "type": "PURCHASE_DATE_APPROACHING",
    "message": "A data estimada da compra \"Trocar o notebook\" está se aproximando (2026-12-01).",
    "plannedItemId": "uuid",
    "wearItemId": null,
    "sentAt": "2026-11-28T12:00:00Z"
  }
]
```
`type`: `PURCHASE_DATE_APPROACHING` (data do item planejado se aproximando), `SUFFICIENT_BALANCE` (categoria já tem saldo suficiente para cobrir o item planejado) ou `WEAR_ITEM_REPLACEMENT_APPROACHING` (item de desgaste perto do fim da vida útil).

Notificações de itens de desgaste trazem `wearItemId` preenchido e `plannedItemId: null`; nas demais, `wearItemId` é `null`.

---

## Notificações em tempo real (WebSocket)

O backend roda uma verificação a cada hora e, quando encontra uma das condições abaixo, gera uma notificação — persistida no histórico (`GET /api/v1/notifications`) **e** entregue em tempo real via WebSocket, caso o usuário esteja conectado:

1. **Data se aproximando**: `estimatedDate` do item planejado está a 3 dias ou menos (configurável no backend).
2. **Saldo suficiente**: a categoria vinculada ao item planejado já acumula saldo (receitas − despesas) maior ou igual ao `estimatedValue`.
3. **Troca de item de desgaste se aproximando**: o item de desgaste entrou em `NEAR_END` ou `OVERDUE` (ver [Controle de desgaste de itens](#controle-de-desgaste-de-itens)).

Cada condição notifica **uma única vez** por item (a menos que o item seja editado — ou, no caso de itens de desgaste, trocado —, o que reabilita o alerta).

### Conectando

- **Protocolo**: STOMP sobre WebSocket (não é WebSocket "cru" — use uma lib STOMP, ex.: `@stomp/stompjs`).
- **Endpoint de handshake**: `ws://localhost:8090/ws/notifications?token=<jwt>` (em produção, `wss://`).
  - O token é o mesmo JWT usado nas chamadas REST, passado como **query param** `token` (não é possível enviar header `Authorization` no handshake de WebSocket do browser).
  - Handshake sem token válido é rejeitado com `401` antes mesmo do upgrade para WebSocket.
- **Destino para assinar**: `/user/queue/notifications` (destino "por usuário" do STOMP — a lib de cliente cuida de mapear isso para a fila privada da sua sessão autenticada).

### Payload recebido

Mesmo formato do histórico, com dois campos extras (`userId`/`userEmail`, usados internamente para roteamento):
```json
{
  "id": "uuid",
  "userId": "uuid",
  "userEmail": "jonathas@example.com",
  "type": "SUFFICIENT_BALANCE",
  "message": "Você já tem saldo suficiente na categoria \"Viagens\" para realizar a compra \"Trocar o notebook\".",
  "plannedItemId": "uuid",
  "wearItemId": null,
  "createdAt": "2026-09-16T14:30:00Z"
}
```

### Exemplo mínimo (JavaScript, `@stomp/stompjs`)

```js
import { Client } from '@stomp/stompjs';

const token = localStorage.getItem('token');

const client = new Client({
  brokerURL: `ws://localhost:8090/ws/notifications?token=${token}`,
  onConnect: () => {
    client.subscribe('/user/queue/notifications', (message) => {
      const notification = JSON.parse(message.body);
      console.log(notification.type, notification.message);
    });
  },
});

client.activate();
```
