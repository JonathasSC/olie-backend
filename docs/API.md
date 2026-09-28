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
- [Contatos e categorias de contato](#contatos-e-categorias-de-contato)
- [Consultas via WhatsApp](#consultas-via-whatsapp)
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
- **Escopo por usuário**: todo recurso (categorias, tasks, transações, itens planejados, metas, itens de desgaste, contatos, consultas, fotos, notas, notificações, sessão do WhatsApp) pertence ao usuário autenticado. Tentar acessar/editar/excluir um recurso de outro usuário retorna `404 Not Found` (não `403`, para não vazar a existência do recurso).
- **IDs**: todos os identificadores são UUID (string).
- **Datas**:
  - Campos `date`/`estimatedDate`/`purchaseDate`/`installationDate`/`removalDate`/`estimatedReplacementDate` são `LocalDate` no formato `"YYYY-MM-DD"`.
  - Campos `createdAt`/`updatedAt`/`sentAt`/`startedAt`/`finishedAt`/`nextSendAt` são `Instant` no formato ISO-8601 UTC (`"2026-09-16T14:30:00Z"`).
- **Valores monetários** (`value`, `amount`, `estimatedValue`, `purchaseValue`, `suggestedValue`) são números decimais (string ou number conforme serialização padrão do Jackson para `BigDecimal` — trate como number).
- **Erros de validação** (`@Valid` nos bodies) retornam `400 Bad Request` com o corpo de erro padrão do Spring Boot.
- **Erros de negócio com motivo**: quando o frontend precisa distinguir o motivo (contatos e consultas via WhatsApp), o corpo traz um `code` estável, uma `message` em português pronta para exibir e, às vezes, `details`:
  ```json
  { "code": "DUPLICATE_PHONE", "message": "O telefone +5511999999999 já pertence ao contato \"Farmácia Central\".", "details": null }
  ```
  Use o `code` na lógica e a `message` na tela. `details` só aparece quando há dados extras (ex.: a lista de erros de um arquivo importado).
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

## Contatos e categorias de contato

Agenda usada pelas [consultas via WhatsApp](#consultas-via-whatsapp). As categorias de contato (ex.: "Farmácias", "Fornecedores") são **independentes** das [categorias financeiras](#categorias).

### Categorias de contato

| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/api/v1/contact-categories` | Lista as categorias (ordem alfabética), com a quantidade de contatos |
| `POST` | `/api/v1/contact-categories` | Cria uma categoria |
| `PUT` | `/api/v1/contact-categories/{id}` | Renomeia uma categoria |
| `DELETE` | `/api/v1/contact-categories/{id}?unlinkContacts=true` | Remove uma categoria |

**Request body** (`POST`/`PUT`): `{ "name": "Farmácias" }`

**Response**: `{ "id": "uuid", "name": "Farmácias", "contactsCount": 3 }`

**Regras de negócio**
- `409 DUPLICATE_CATEGORY` se já existir uma categoria com o mesmo nome (sem diferenciar maiúsculas).
- Remover uma categoria que ainda tem contatos exige confirmação. Sem `unlinkContacts=true`, retorna `409 CATEGORY_HAS_CONTACTS` com os nomes dos contatos em `details`. Com a confirmação, os contatos só perdem esse vínculo.
- Todo contato precisa de pelo menos uma categoria. Se algum contato ficaria sem nenhuma, retorna `409 CONTACTS_WITHOUT_CATEGORY` (nomes em `details`). Mova esses contatos para outra categoria antes de remover.

### Contatos

| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/api/v1/contacts` | Lista os contatos (ordem alfabética) |
| `POST` | `/api/v1/contacts` | Cria um contato |
| `PUT` | `/api/v1/contacts/{id}` | Atualiza um contato |
| `DELETE` | `/api/v1/contacts/{id}` | Remove um contato (o histórico de consultas guarda uma cópia do nome e do telefone) |
| `POST` | `/api/v1/contacts/import` | Importa contatos e categorias de um arquivo YAML/JSON |
| `GET` | `/api/v1/contacts/export?format=YAML\|JSON` | Baixa todos os contatos e categorias no mesmo formato da importação |

**Query params de `GET /api/v1/contacts`** (opcionais)
| Param | Descrição |
|---|---|
| `categoryId` | Um ou mais (`?categoryId=a&categoryId=b`): contatos de **qualquer** uma das categorias. É o que a tela de consulta usa para pré-selecionar os destinatários. |
| `contactable` | `true` = só quem pode receber consultas (ativo e sem `doNotContact`); `false` = só quem não pode |

**Request body** (`POST`/`PUT`)
```json
{
  "name": "Farmácia Central",
  "phone": "+55 (11) 99999-9999",
  "categoryIds": ["uuid"],
  "active": true,
  "doNotContact": false
}
```
| Campo | Tipo | Regras |
|---|---|---|
| `name` | string | obrigatório |
| `phone` | string | obrigatório, formato internacional **com código do país**. Espaços, parênteses, pontos e hífens são aceitos e removidos: `"+55 (11) 99999-9999"` é gravado como `"+5511999999999"` (E.164). |
| `categoryIds` | UUID[] | obrigatório, pelo menos uma categoria de contato do próprio usuário |
| `active` | boolean | opcional. No `POST` o padrão é `true`; no `PUT`, se omitido, mantém o valor atual. Contatos inativos não recebem consultas. |
| `doNotContact` | boolean | opcional. No `POST` o padrão é `false`; no `PUT`, se omitido, mantém o valor atual. Marca quem **pediu para não ser contatado**: fica fora de qualquer envio, mesmo se estiver ativo. |

**Response** (`ContactResponse`)
```json
{
  "id": "uuid",
  "name": "Farmácia Central",
  "phone": "+5511999999999",
  "categoryIds": ["uuid"],
  "active": true,
  "doNotContact": false,
  "contactable": true,
  "createdAt": "2026-09-28T12:00:00Z"
}
```

**Erros**: `400 INVALID_PHONE` (telefone fora do formato) e `409 DUPLICATE_PHONE` (telefone já cadastrado em outro contato do usuário).

### Importar arquivo — `POST /api/v1/contacts/import`

`multipart/form-data` com o campo `file`. O formato é decidido pela extensão: `.yaml`/`.yml` ou `.json`. Qualquer outra extensão retorna `400 UNSUPPORTED_FORMAT`.

```yaml
categories:
  - Farmácias
  - Fornecedores
contacts:
  - name: Farmácia Central
    phone: "+5511999999999"
    categories: [Farmácias]
    active: true          # opcional, padrão true
    doNotContact: false   # opcional, padrão false
```

- **Tudo ou nada**: se houver qualquer erro, nada é gravado. A resposta é `400 INVALID_CONTACTS_FILE` e `details` lista cada problema com a linha (a do início do contato) e o campo:
  ```json
  {
    "code": "INVALID_CONTACTS_FILE",
    "message": "O arquivo tem 2 problema(s); nada foi importado.",
    "details": [
      { "line": 7, "field": "phone", "message": "Telefone inválido: use o formato internacional, ex.: +5511999999999." },
      { "line": 7, "field": "categories", "message": "Categoria \"Inexistente\" não existe; declare-a em \"categories\"." }
    ]
  }
  ```
  O arquivo é validado quanto a: telefone inválido, telefone repetido no arquivo, categoria inexistente, contato sem nome ou sem categoria, `active`/`doNotContact` que não sejam booleanos, e sintaxe YAML/JSON quebrada.
- **Mescla**: categorias são casadas pelo nome e contatos pelo telefone. O que já existe é atualizado e o que não está no arquivo é mantido. Uma categoria citada num contato precisa estar em `categories` ou já existir no sistema.
- **Response `200 OK`**: `{ "categoriesCreated": 1, "contactsCreated": 2, "contactsUpdated": 1 }`

O `export` gera um arquivo que pode ser reimportado. Serve como backup antes de edições grandes.

---

## Consultas via WhatsApp

Envia a mesma pergunta ("tem esse item?") para vários contatos pelo WhatsApp, um de cada vez e com intervalos, e acompanha o resultado em tempo real.

**Como funciona o envio**
- Cada contato recebe **uma mensagem de texto** e, em seguida, **uma mensagem por foto**, com legenda "número. nome do item".
- A mensagem é montada **na hora do envio para cada contato**: a saudação depende do horário local (`America/Sao_Paulo` por padrão). "Bom dia" vai das 05h00 às 11h59, "Boa tarde" das 12h00 às 17h59 e "Boa noite" das 18h00 às 04h59. A pergunta concorda com a quantidade de itens: "Tem esse item?" ou "Tem esses itens?".
- Modelo padrão (configurável, sempre com as três variáveis):
  ```
  {saudacao}! {pergunta}

  {itens}
  ```
  Resultado: `"Bom dia! Tem esses itens?\n\n1. Dipirona 500mg\n2. Soro fisiológico"`
- Entre um contato e outro há um intervalo **aleatório** (padrão de 20 a 60 s); entre as mensagens do mesmo contato, 2 s. A falha em um contato não interrompe os demais. Falhas passageiras (rede, instabilidade) são tentadas de novo automaticamente (3 tentativas por padrão).
- Todo o estado fica no banco. Se o sistema cair, o envio continua do primeiro contato ainda não enviado.

### Arquitetura

O WhatsApp Web é acessado por um sidecar Node (`whatsapp-gateway/`, com a biblioteca [Baileys](https://github.com/WhiskeySockets/Baileys)) que sobe junto pelo `compose.yaml`. O backend fala com ele por HTTP, e o gateway avisa o backend das mudanças de conexão por webhook. A integração fica isolada atrás da interface `WhatsAppProvider`, então dá para trocar o provedor (por exemplo, pela Cloud API oficial) sem mexer no resto.

> ⚠️ **WhatsApp Web não é uma API oficial.** O uso automatizado pode levar ao bloqueio do número. Por isso os limites padrão são conservadores. Use um número dedicado e só consulte contatos que esperam a mensagem.

**Variáveis de ambiente**
| Variável | Padrão | Descrição |
|---|---|---|
| `WHATSAPP_PROVIDER` | `gateway` | `gateway` (WhatsApp de verdade) ou `simulated` (nada é enviado; conexão sempre "conectada". Útil em desenvolvimento) |
| `WHATSAPP_GATEWAY_URL` | `http://localhost:3100` | Endereço do gateway |
| `WHATSAPP_GATEWAY_TOKEN` | `change-this-gateway-token` | Segredo compartilhado entre backend e gateway. **Troque em produção** (o mesmo valor nos dois) |
| `INQUIRIES_TIMEZONE` | `America/Sao_Paulo` | Fuso usado na saudação e nos filtros de data do histórico |
| `INQUIRIES_MESSAGETEMPLATE` | `{saudacao}! {pergunta}\n\n{itens}` | Modelo da mensagem. O sistema não sobe se faltar alguma das três variáveis |
| `INQUIRIES_MAX_ITEMS` | `10` | Máximo de itens por consulta |
| `INQUIRIES_MAX_RECIPIENTS` | `30` | Máximo de contatos por consulta |
| `INQUIRIES_CONTACT_INTERVAL_MIN` / `_MAX` | `20s` / `60s` | Intervalo aleatório entre contatos |
| `INQUIRIES_MESSAGE_INTERVAL` | `2s` | Intervalo entre as mensagens de um mesmo contato |
| `INQUIRIES_MAX_ATTEMPTS` | `3` | Tentativas por contato em falhas passageiras |
| `INQUIRIES_DUPLICATE_WINDOW` | `7d` | Janela do alerta de consulta repetida |
| `INQUIRIES_PHOTO_STORAGE_DIR` | `./data/inquiry-photos` | Onde as fotos ficam gravadas (fora do banco e do git) |
| `INQUIRIES_PHOTO_MAX_UPLOAD_SIZE` | `10MB` | Tamanho máximo do arquivo enviado |
| `INQUIRIES_PHOTO_MAX_SIZE` / `_MAX_DIMENSION` | `1MB` / `1600` | Acima disso a foto é reduzida (em bytes e em pixels no maior lado) |

### Conexão com o WhatsApp

Cada usuário do Olie tem a própria sessão. Ela fica salva no gateway, então depois do primeiro QR code o sistema reconecta sozinho, inclusive após reinícios.

| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/api/v1/whatsapp/connection` | Estado atual da conexão |
| `POST` | `/api/v1/whatsapp/connection` | Inicia a conexão: com sessão salva conecta direto, senão passa a exibir o QR code |
| `DELETE` | `/api/v1/whatsapp/connection` | Desconecta e apaga a sessão salva (o próximo `POST` pede QR code de novo) |

**Response** (as três rotas)
```json
{
  "status": "WAITING_QR",
  "qrCode": "data:image/png;base64,iVBORw0KGgo...",
  "phone": null,
  "reason": null,
  "connected": false
}
```
| Campo | Descrição |
|---|---|
| `status` | `DISCONNECTED`, `CONNECTING`, `WAITING_QR` ou `CONNECTED` |
| `qrCode` | Imagem PNG em data URL, pronta para `<img src>`. Só vem em `WAITING_QR` |
| `phone` | Número conectado (E.164). Só vem em `CONNECTED` |
| `reason` | Por que está desconectado: `LOGGED_OUT` (o aparelho foi desconectado pelo celular), `QR_TIMEOUT` (os QR codes expiraram sem leitura; chame o `POST` de novo), `CONNECTION_LOST` (queda momentânea, o sistema já está reconectando), `USER_LOGOUT` (desconectado pelo `DELETE`) ou `GATEWAY_UNAVAILABLE` (o gateway não respondeu) |

**Fluxo na tela de conexão**: chame o `POST`, assine `/user/queue/whatsapp` (ver [Tempo real](#tempo-real-das-consultas-websocket)) e mostre o `qrCode` a cada evento. O WhatsApp troca o QR code a cada ~20 s e o novo chega pelo WebSocket. Quando vier `CONNECTED`, pronto. Se chegar `LOGGED_OUT`, também é gerada uma notificação `WHATSAPP_DISCONNECTED` no histórico.

### Fotos dos itens — `/api/v1/inquiry-photos`

As fotos são enviadas **antes** de criar a consulta. O upload devolve um `id`, que vai em `items[].photoId`.

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/api/v1/inquiry-photos` | Upload (`multipart/form-data`, campo `file`) |
| `GET` | `/api/v1/inquiry-photos/{id}` | Baixa a imagem (exige o mesmo `Authorization`; use `fetch` + `URL.createObjectURL` para exibir) |

- Aceita **JPG, PNG e WEBP**, identificados pelo conteúdo e não pela extensão. Outros arquivos: `400 UNSUPPORTED_PHOTO`. Arquivo acima de 10 MB: `400 PHOTO_TOO_LARGE`.
- Imagens acima de 1600 px ou 1 MB são reduzidas e convertidas para JPEG. A rotação da câmera do celular (EXIF) é aplicada, então a foto não chega deitada. WEBP sempre vira JPEG, porque o WhatsApp trataria como figurinha.
- Seleção de arquivo, arrastar e soltar e colar da área de transferência (RF13) são responsabilidade do frontend. Todos acabam no mesmo `POST`.
- Fotos que nunca entram em uma consulta são apagadas automaticamente depois de 1 dia.

**Response `201 Created`**
```json
{ "id": "uuid", "contentType": "image/jpeg", "sizeBytes": 881123, "width": 1600, "height": 800, "url": "/api/v1/inquiry-photos/uuid" }
```

### Configurações e pré-visualização

| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/api/v1/inquiries/settings` | Limites e modelo da mensagem, para o frontend montar o formulário (e, se quiser, a pré-visualização localmente) |
| `POST` | `/api/v1/inquiries/preview` | Sequência exata de mensagens que cada contato vai receber |

**Response de `settings`**
```json
{
  "messageTemplate": "{saudacao}! {pergunta}\n\n{itens}",
  "timezone": "America/Sao_Paulo",
  "maxItems": 10,
  "maxRecipients": 30,
  "contactIntervalMinSeconds": 20,
  "contactIntervalMaxSeconds": 60,
  "duplicateWindowHours": 168,
  "maxPhotoUploadBytes": 10485760
}
```

**Request de `preview`**: `{ "items": [{ "name": "Dipirona 500mg", "photoId": "uuid" }, { "name": "Soro fisiológico", "photoId": null }] }`

**Response de `preview`**
```json
{
  "greeting": "Bom dia",
  "question": "Tem esses itens?",
  "messages": [
    { "type": "TEXT", "text": "Bom dia! Tem esses itens?\n\n1. Dipirona 500mg\n2. Soro fisiológico", "photoId": null },
    { "type": "PHOTO", "text": "1. Dipirona 500mg", "photoId": "uuid" }
  ]
}
```

> Para a pré-visualização atualizar em menos de 300 ms enquanto o usuário digita (RNF05), o frontend pode montar o texto localmente com o `messageTemplate` do `settings` e as regras de saudação e pergunta acima, e chamar o `preview` só antes de confirmar.

### Criar e enviar consulta — `POST /api/v1/inquiries`

```json
{
  "categoryIds": ["uuid"],
  "contactIds": ["uuid", "uuid"],
  "items": [
    { "name": "Dipirona 500mg", "photoId": "uuid" },
    { "name": "Soro fisiológico" }
  ],
  "simulate": false,
  "confirmDuplicates": false
}
```
| Campo | Tipo | Regras |
|---|---|---|
| `categoryIds` | UUID[] | opcional. Categorias escolhidas na tela, gravadas para o filtro do histórico |
| `contactIds` | UUID[] | obrigatório. Destinatários **na ordem de envio**. O frontend pré-seleciona com `GET /contacts?categoryId=...&contactable=true` e envia o que ficou marcado |
| `items` | objeto[] | obrigatório, de 1 a `maxItems`, **na ordem de exibição**. `name` é obrigatório; `photoId` é opcional |
| `simulate` | boolean | opcional (padrão `false`). `true` executa **todo o fluxo sem enviar nada**: status, tempo real e histórico funcionam, cada contato mostra o `messageText` que receberia e o intervalo entre contatos cai para 1 s. Não exige WhatsApp conectado |
| `confirmDuplicates` | boolean | opcional (padrão `false`). Envie `true` depois que o usuário confirmar o alerta de consulta repetida |

A confirmação "Enviar para N contatos?" (RF21) é feita no frontend. Este `POST` já é a confirmação, e o envio começa em até 1 s.

**Response `201 Created`**: `InquiryResponse` (abaixo), com `status: "SENDING"`.

**Erros** (corpo no [formato de erro de negócio](#convenções-gerais))
| Status | `code` | Quando |
|---|---|---|
| `409` | `WHATSAPP_NOT_CONNECTED` | Envio real com o WhatsApp desconectado |
| `409` | `DUPLICATE_INQUIRY` | Algum contato já recebeu um destes itens (mesmo nome, sem diferenciar maiúsculas) dentro da janela. Simulações não contam. `details`: `[{ "contactId", "contactName", "itemName", "sentAt" }]`. Mostre o alerta e reenvie com `confirmDuplicates: true` |
| `400` | `CONTACTS_NOT_CONTACTABLE` | Algum contato está inativo ou marcado `doNotContact` (nomes em `details`) |
| `400` | `TOO_MANY_ITEMS` / `TOO_MANY_RECIPIENTS` | Passou de `maxItems` / `maxRecipients` |
| `400` | — | Item sem nome, `contactIds` ou `items` vazios (validação padrão) |
| `404` | — | Contato, categoria ou foto inexistente ou de outro usuário |

### Acompanhar e controlar

| Método | Rota | Descrição | Permitido quando `status` é |
|---|---|---|---|
| `GET` | `/api/v1/inquiries/{id}` | Estado completo, com status por contato | qualquer |
| `POST` | `/api/v1/inquiries/{id}/pause` | Pausa depois do contato atual | `SENDING` |
| `POST` | `/api/v1/inquiries/{id}/resume` | Retoma (exige WhatsApp conectado, exceto simulação) | `PAUSED` |
| `POST` | `/api/v1/inquiries/{id}/cancel` | Cancela quem ainda está na fila; o contato em envio neste instante termina normalmente | `SENDING`, `PAUSED` |
| `POST` | `/api/v1/inquiries/{id}/resend-failed` | Devolve à fila **só** os contatos com falha | `COMPLETED`, `PAUSED`, `CANCELLED` |

Ação fora do status permitido: `409 INVALID_INQUIRY_STATUS`. Reenviar sem nenhuma falha: `400 NO_FAILED_RECIPIENTS`. As quatro ações de controle retornam o `InquiryResponse` atualizado.

**Response** (`InquiryResponse`)
```json
{
  "id": "uuid",
  "status": "SENDING",
  "pauseReason": null,
  "simulated": false,
  "categoryIds": ["uuid"],
  "items": [
    { "id": "uuid", "position": 0, "name": "Dipirona 500mg", "photoId": "uuid", "photoUrl": "/api/v1/inquiry-photos/uuid" }
  ],
  "recipients": [
    {
      "id": "uuid",
      "contactId": "uuid",
      "contactName": "Farmácia Central",
      "phone": "+5511999999999",
      "status": "SENT",
      "failureReason": null,
      "attempts": 1,
      "sentAt": "2026-09-28T12:43:46Z",
      "messageText": "Bom dia! Tem esse item?\n\n1. Dipirona 500mg"
    }
  ],
  "totals": { "total": 3, "pending": 1, "sending": 1, "sent": 1, "failed": 0, "cancelled": 0 },
  "nextSendAt": "2026-09-28T12:44:21Z",
  "startedAt": "2026-09-28T12:43:45Z",
  "finishedAt": null,
  "createdAt": "2026-09-28T12:43:45Z"
}
```
| Campo | Descrição |
|---|---|
| `status` | `SENDING`, `PAUSED`, `COMPLETED` ou `CANCELLED` |
| `pauseReason` | `USER` (pausado pelo usuário) ou `CONNECTION_LOST` (o WhatsApp caiu no meio: o envio pausa sozinho e o contato atual volta para a fila. Reconecte e chame `resume`) |
| `recipients[].status` | `PENDING` (aguardando), `SENDING` (enviando), `SENT` (enviado), `FAILED` (falhou, motivo em `failureReason`) ou `CANCELLED` |
| `recipients[].messageText` | Texto que o contato recebeu (ou receberia, na simulação). Preenchido quando o envio para ele começa |
| `nextSendAt` | Quando o próximo contato será processado: é a base da **contagem regressiva** (RF31). `null` enquanto alguém está sendo enviado, com a consulta pausada ou encerrada |
| `totals` | Contagem por status. Com `status: COMPLETED`, é o **resumo final** (RF32) |

**Reenvio sem duplicar**: o sistema registra quantas mensagens cada contato já recebeu. Retentativas e o `resend-failed` continuam dali. Se o texto já tinha saído e só a foto falhou, o reenvio manda só a foto.

**Queda do sistema** (RNF07): na volta, o envio continua do primeiro contato pendente. Quem estava **no meio** do envio vira `FAILED` com o motivo "Envio interrompido por reinício do sistema; confira no WhatsApp antes de reenviar.", porque não dá para saber se a mensagem saiu. O usuário decide se usa `resend-failed`.

Ao concluir um envio real, é gerada uma notificação `INQUIRY_FINISHED` no histórico ("Consulta concluída: 3 enviada(s) e 1 com falha.").

### Histórico e repetição

| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/api/v1/inquiries` | Histórico, mais recentes primeiro (`InquirySummaryResponse`: igual ao `InquiryResponse`, sem `recipients`) |
| `GET` | `/api/v1/inquiries/{id}/draft` | Dados da consulta no formato do `POST /inquiries`, para **repetir a consulta** com o formulário preenchido |

**Query params do histórico** (opcionais, combináveis)
| Param | Descrição |
|---|---|
| `from`, `to` | Datas `YYYY-MM-DD` (inclusivas), pelo dia de criação no fuso configurado |
| `categoryId` | Consultas feitas para essa categoria |
| `item` | Parte do nome de algum item, sem diferenciar maiúsculas (`?item=dipi`) |
| `simulated` | `true` só simulações, `false` só envios reais |

**Response de `draft`**
```json
{
  "categoryIds": ["uuid"],
  "contactIds": ["uuid", "uuid"],
  "items": [{ "name": "Dipirona 500mg", "photoId": "uuid" }],
  "skippedContacts": [{ "contactName": "Farmácia Antiga", "reason": "Contato removido" }]
}
```
As fotos são reaproveitadas, sem novo upload. Contatos removidos, inativos ou marcados `doNotContact` saem de `contactIds` e aparecem em `skippedContacts` (motivos: "Contato removido", "Contato inativo" ou "Pediu para não ser contatado"), para o frontend avisar o usuário.

### Tempo real das consultas (WebSocket)

Mesma conexão STOMP das [notificações](#notificações-em-tempo-real-websocket) (`/ws/notifications?token=<jwt>`), com mais duas filas:

| Destino | Conteúdo |
|---|---|
| `/user/queue/whatsapp` | Mudanças da conexão, no mesmo formato de `GET /whatsapp/connection` (inclui cada QR code novo) |
| `/user/queue/inquiries` | Progresso das consultas (`InquiryEvent`) |

**`InquiryEvent`**
```json
{
  "type": "RECIPIENT_UPDATED",
  "inquiryId": "uuid",
  "status": "SENDING",
  "pauseReason": null,
  "simulated": false,
  "nextSendAt": null,
  "totals": { "total": 3, "pending": 1, "sending": 1, "sent": 1, "failed": 0, "cancelled": 0 },
  "recipient": { "...": "mesmo formato de recipients[] do InquiryResponse" }
}
```
| `type` | Quando | Campos relevantes |
|---|---|---|
| `RECIPIENT_UPDATED` | Um contato mudou de status (aguardando → enviando → enviado/falhou) | `recipient` |
| `INQUIRY_UPDATED` | Pausa, retomada, cancelamento ou próximo envio agendado | `status`, `pauseReason`, `nextSendAt` (reinicie a contagem regressiva) |
| `INQUIRY_FINISHED` | Terminou | `totals` = resumo final |

```js
client.subscribe('/user/queue/inquiries', (message) => {
  const event = JSON.parse(message.body);
  if (event.type === 'RECIPIENT_UPDATED') updateRow(event.inquiryId, event.recipient);
  if (event.nextSendAt) startCountdown(new Date(event.nextSendAt));
  if (event.type === 'INQUIRY_FINISHED') showSummary(event.totals);
});
```

### Fora do escopo desta versão

- **Respostas dos contatos (F08, fase 2)**: o gateway ainda não repassa as mensagens recebidas.
- **Restringir a API à máquina ou rede local (RNF11)**: o gateway já só aceita conexões locais (`127.0.0.1:3100`, além do token). Para o backend, use `SERVER_ADDRESS` ou um proxy reverso na implantação.

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
`type`: `PURCHASE_DATE_APPROACHING` (data do item planejado se aproximando), `SUFFICIENT_BALANCE` (categoria já tem saldo suficiente para cobrir o item planejado), `WEAR_ITEM_REPLACEMENT_APPROACHING` (item de desgaste perto do fim da vida útil), `WHATSAPP_DISCONNECTED` (o aparelho foi desconectado pelo celular; é preciso escanear o QR code de novo) ou `INQUIRY_FINISHED` (uma consulta via WhatsApp terminou, com o resumo na mensagem).

Notificações de itens de desgaste trazem `wearItemId` preenchido e `plannedItemId: null`; `WHATSAPP_DISCONNECTED` e `INQUIRY_FINISHED` vêm com os dois `null`.

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
