# Chat App Backend  

<p align="center">
  <strong>Enterprise WhatsApp-style messaging API</strong><br/>
  Spring Boot 4 · MySQL · JWT · Raw WebSocket · Swagger
</p>

<p align="center">
  <img alt="Java" src="https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" />
  <img alt="Spring Boot" src="https://img.shields.io/badge/Spring%20Boot-4.1-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" />
  <img alt="MySQL" src="https://img.shields.io/badge/MySQL-8+-4479A1?style=for-the-badge&logo=mysql&logoColor=white" />
  <img alt="WebSocket" src="https://img.shields.io/badge/WebSocket-Raw%20JSON-010101?style=for-the-badge&logo=socketdotio&logoColor=white" />
  <img alt="OpenAPI" src="https://img.shields.io/badge/Swagger-OpenAPI%203-85EA2D?style=for-the-badge&logo=swagger&logoColor=black" />
</p>

---

Realtime chat backend built for **mobile and React clients**. Clean layered architecture, JWT auth, 1:1 + group conversations, message history, delivery/read receipts, online presence, and profile pictures.

**Repository:** [github.com/rudramadhab22/chat-app](https://github.com/rudramadhab22/chat-app)

---

## Features

| Area | What you get |
|------|----------------|
| Auth | Register / login with **JWT** + BCrypt passwords |
| Chat | Direct (1:1) and **group** conversations |
| Realtime | Raw **WebSocket** JSON (no STOMP) |
| Reliability | Delivery & read receipts (`SENT` → `DELIVERED` → `READ`) |
| Presence | Online / last-seen for shared contacts |
| History | Cursor-based message pagination |
| Profile | Avatar upload + public media URLs |
| Docs | Interactive **Swagger UI** with JWT Authorize |

---

## Architecture

```text
Clients (React / Mobile)
        │
        ├── REST  /api/v1/**     (JWT Bearer)
        └── WS    /ws/chat?token=…  (raw JSON)
                │
        ┌───────▼────────┐
        │  Controllers   │
        │  WebSocket     │
        └───────┬────────┘
                │
        ┌───────▼────────┐
        │   Services     │
        └───────┬────────┘
                │
        ┌───────▼────────┐
        │ Repositories   │──▶ MySQL
        └────────────────┘
                │
           uploads/avatars
```

**Package layout**

```text
com.rudra.ed
├── config/          Security, WebSocket, CORS
│   └── openapi/     Swagger groups + JWT scheme
├── security/        JwtService, filter, UserPrincipal
├── domain/          Entities + enums
├── repository/
├── service/
├── controller/      REST API
├── websocket/       Handler + session registry
├── dto/
└── exception/
```

---

## Quick start

### Prerequisites

- **JDK 21+**
- **MySQL** running locally

### Configure secrets (env / `.env`)

Sensitive values are **not** stored in `application.properties`.  
Copy the example file and edit:

```bash
cp .env.example .env
```

Required keys in `.env` (or OS environment):

| Variable | Purpose |
|----------|---------|
| `DB_URL` | JDBC URL |
| `DB_USERNAME` | MySQL user |
| `DB_PASSWORD` | MySQL password |
| `JWT_SECRET` | JWT signing key (≥ 32 chars) |
| `SERVER_PORT` | HTTP port (default `8080`) |

`.env` is gitignored. Real OS env vars always override `.env`.

### Run

```bash
./mvnw spring-boot:run
```

| Service | URL |
|---------|-----|
| API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON | http://localhost:8080/v3/api-docs |
| WebSocket | `ws://localhost:8080/ws/chat?token=<JWT>` |

---

## Swagger

1. Open [Swagger UI](http://localhost:8080/swagger-ui.html)
2. Use **Register** or **Login** under Authentication
3. Copy `data.accessToken`
4. Click **Authorize** → paste the token
5. Explore grouped definitions: *All*, *Authentication*, *Users & Media*, *Conversations & Messages*

---

## REST API (summary)

Base path: `/api/v1` · Auth header: `Authorization: Bearer <token>`

### Auth

| Method | Path | Access |
|--------|------|--------|
| `POST` | `/auth/register` | Public |
| `POST` | `/auth/login` | Public |
| `GET` | `/auth/me` | JWT |

### Users & media

| Method | Path | Notes |
|--------|------|--------|
| `GET` | `/users/search?q=` | Min 2 chars |
| `GET` | `/users/{id}` | Profile |
| `PUT` | `/users/me` | Display name / about |
| `POST` | `/users/me/avatar` | `multipart` field `file` |
| `GET` | `/media/avatars/{filename}` | Public |

### Conversations & messages

| Method | Path | Notes |
|--------|------|--------|
| `POST` | `/conversations/direct` | `{ "otherUserId": 2 }` |
| `POST` | `/conversations/groups` | `{ "name", "memberIds" }` |
| `GET` | `/conversations` | My inbox |
| `GET` | `/conversations/{id}` | Detail |
| `POST` | `/conversations/{id}/members` | Admin |
| `DELETE` | `/conversations/{id}/members/{userId}` | Leave / kick |
| `GET` | `/conversations/{id}/messages` | `?beforeId=&size=50` |
| `POST` | `/conversations/{id}/messages` | REST send + WS push |

**Standard response**

```json
{
  "success": true,
  "message": "OK",
  "data": {},
  "timestamp": "2026-08-20T07:00:00Z"
}
```

---

## WebSocket protocol

Connect after login:

```text
ws://localhost:8080/ws/chat?token=<JWT>
```

Envelope:

```json
{
  "type": "CHAT_MESSAGE | MESSAGE_ACK | DELIVERED | READ | TYPING | PRESENCE | ERROR",
  "payload": {}
}
```

| Type | Direction | Purpose |
|------|-----------|---------|
| `CHAT_MESSAGE` | ↔ | Send / receive text |
| `MESSAGE_ACK` | ← | Ack with persisted message + `clientMessageId` |
| `DELIVERED` / `READ` | ↔ | Receipt updates |
| `TYPING` | ↔ | Typing indicator |
| `PRESENCE` | ← | Online / last seen |
| `ERROR` | ← | Protocol or business error |

**Send**

```json
{
  "type": "CHAT_MESSAGE",
  "payload": {
    "conversationId": 1,
    "content": "Hi there",
    "clientMessageId": "abc-123"
  }
}
```

**Receipts**

```json
{ "type": "DELIVERED", "payload": { "messageId": 10 } }
```

```json
{ "type": "READ", "payload": { "messageId": 10 } }
```

---

## Client integration checklist

1. Register / login via REST → store JWT  
2. Open WebSocket with the same token  
3. Load conversations + history via REST  
4. Send live traffic over WebSocket (messages, receipts, typing)  
5. Render avatars from `profilePictureUrl` / media URLs as returned  

---

## Tech stack

- Java 21
- Spring Boot 4.1 (WebMVC, Security, Data JPA, Validation, WebSocket)
- MySQL + Hibernate
- jjwt
- SpringDoc OpenAPI 3 / Swagger UI
- Lombok

---

## License

Released under the [MIT License](LICENSE).

---

<p align="center">
  Built for clean mobile & React integration · Backend-first · Ready to extend
</p>
