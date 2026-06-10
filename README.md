# BrickWorkPro 2.0

Brick manufacturing and order management platform for catalog browsing, quoting, checkout, payments, production tracking, and admin operations.

Monorepo with a **Spring Cloud** microservices backend and an **Angular** frontend, fronted by an API gateway and Eureka service discovery.

## Features

| Area | Capabilities |
|------|----------------|
| **Public** | Product catalog, quote requests, guest checkout, order tracking, contact form, brick calculator |
| **Customer** | Register/login, cart & checkout, Razorpay / UTR / COD payments, order history, profile, invoices |
| **Admin** | Dashboard analytics, order management, product CRUD, payment verification, refunds, contact messages |
| **Staff** | Production logs, raw materials, production stage management |

## Architecture

```
Angular (4200)
      │
      ▼
API Gateway (9191)
      │
      ├── Eureka (8761)
      │
      ├── Users Service (8083)     → brickworks2users
      ├── Products Service (8081)  → brickworks2products
      ├── Orders Service (8080)    → brickworks2orders
      └── Finance Service (8084) → brickworks2finance
```

Shared library: `common-security-brickwork` (JWT, internal service auth, centralized API exception handling, request logging).

## Tech Stack

| Layer | Technologies |
|-------|----------------|
| Frontend | Angular 21, Tailwind CSS, Chart.js |
| Backend | Java 21, Spring Boot 3.4, Spring Cloud 2024 |
| Data | MariaDB, Spring Data JPA |
| Auth | JWT, Spring Security, role-based access |
| Payments | Razorpay (online), UTR bank transfer, Cash on Delivery |
| Notifications | Email (SMTP), WhatsApp (UltraMsg) |

## Prerequisites

- **Java 21** (JDK)
- **Maven 3.9+**
- **Node.js 20+** and **npm**
- **Docker** (for MariaDB via `docker-compose`)
- **PowerShell** (for helper scripts on Windows)

Optional: IntelliJ IDEA with Maven import of `BrickWorkPro-2.0-Backend`

## Quick Start (Windows)

### 1. Clone and configure environment

```powershell
git clone https://github.com/deepak-bisen/brickworks-2.0.git
cd brickworks-2.0
copy .env.example .env
```

Edit `.env` with your secrets (JWT, Razorpay, mail, WhatsApp). Dev scripts also set common defaults for local runs.

### 2. Start database

```powershell
docker compose up -d
```

This starts MariaDB on port **3306** and creates the four service databases from `scripts/init-databases.sql`.

### 3. Run everything (recommended)

```powershell
powershell -ExecutionPolicy Bypass -File scripts\run-all.ps1
```

This builds the backend (unless `-SkipBuild`), starts all microservices and the frontend in the background, and tails logs in the current window.

| Flag | Effect |
|------|--------|
| `-SkipBuild` | Skip `mvn install` |
| `-SkipDb` | Skip database init |
| `-Fresh` | Stop existing services first |
| `-NoFrontend` | Backend only |
| `-NoLogs` | Start services without log tail |

### 4. Open the app

- **Frontend:** http://localhost:4200
- **API Gateway:** http://localhost:9191
- **Eureka:** http://localhost:8761

### 5. Stop all services

```powershell
powershell -ExecutionPolicy Bypass -File scripts\stop-all.ps1
```

## Service Ports

| Service | Port | Database |
|---------|------|----------|
| Eureka (service registry) | 8761 | — |
| Orders | 8080 | `brickworks2orders` |
| Products | 8081 | `brickworks2products` |
| Users | 8083 | `brickworks2users` |
| Finance | 8084 | `brickworks2finance` |
| API Gateway | 9191 | — |
| Frontend (dev) | 4200 | — |
| MariaDB | 3306 | — |

## Default Admin Account

On first startup, the users service seeds a default admin if none exists:

| Field | Value |
|-------|-------|
| Username | `superadmin` |
| Password | `Admin@123` |

Change this password after first login in any non-dev environment.

## Manual Setup

### Backend only

```powershell
cd BrickWorkPro-2.0-Backend
mvn install -DskipTests
powershell -ExecutionPolicy Bypass -File ..\scripts\start-backend.ps1
```

Start order: **Eureka → Users → Products → Orders → Finance → API Gateway** (wait ~15–30s between groups).

### Frontend only

```powershell
cd BrickWorkPro-2.0-Frontend
npm install
npm start
```

The frontend talks to the API through the gateway at `http://localhost:9191`.

### IntelliJ IDEA

1. Open **`BrickWorkPro-2.0-Backend`** as the Maven project root.
2. Use run configurations in `.run/` (Eureka first, gateway last).
3. Stop script-launched services before running from IntelliJ to avoid port conflicts:

   ```powershell
   powershell -ExecutionPolicy Bypass -File scripts\stop-all.ps1
   ```

## Environment Variables

See [`.env.example`](.env.example) for the full list. Critical values:

| Variable | Used by | Notes |
|----------|---------|-------|
| `JWT_SECRET` | All secured services | Min 32 chars; must match everywhere |
| `INTERNAL_SERVICE_KEY` | Inter-service Feign calls | Required for invoice generation, stock deduction |
| `DB_USERNAME` / `DB_PASSWORD` | All services | Default `root` / `root` for local Docker |
| `RAZORPAY_*` | Finance | Test keys for dev; webhook secret for payment callbacks |
| `MAIL_*` | Orders | Order confirmation & dispatch emails |
| `WHATSAPP_*` | Orders | Dispatch & confirmation notifications |

## API Routes (via Gateway)

| Path prefix | Service |
|-------------|---------|
| `/api/auth/**`, `/api/users/**` | Users |
| `/api/products/**`, `/api/production-logs/**`, `/api/raw-materials/**` | Products |
| `/api/orders/**` | Orders |
| `/api/finance/**` | Finance |

## Project Structure

```
BrickWorkPro-2.0/
├── BrickWorkPro-2.0-Backend/     # Maven multi-module microservices
│   ├── api-gateway/
│   ├── common-security-brickwork/
│   ├── finance-brickwork/
│   ├── orders-brickwork/
│   ├── products-brickwork/
│   ├── service-registry-brickwork/
│   └── users-brickwork/
├── BrickWorkPro-2.0-Frontend/    # Angular SPA
├── scripts/                      # Start/stop, logs, workflow tests
├── docker-compose.yml            # MariaDB for local dev
└── .env.example                  # Environment template
```

## Helper Scripts

| Script | Purpose |
|--------|---------|
| `scripts/run-all.ps1` | Build + start DB, backend, frontend, tail logs |
| `scripts/start-backend.ps1` | Start backend JARs only (background) |
| `scripts/stop-all.ps1` | Stop services by port and Java processes |
| `scripts/tail-logs.ps1` | Tail service log files |
| `scripts/test-workflow.ps1` | End-to-end API workflow test |
| `scripts/test-guest-workflow.ps1` | Guest checkout flow test |

Logs are written to `scripts/logs/`.

## Development Notes

- **Logging:** Services use SLF4J (`@Slf4j`). HTTP requests are logged via `HttpRequestLoggingFilter`. Business events (orders, payments, invoices, stock) emit `log.info`.
- **Errors:** Central `GlobalApiExceptionHandler` in `common-security-brickwork` returns consistent JSON: `{ status, error, message, path }`. The Angular app parses these via `api-error.util.ts`.
- **Build:** Run `mvn install` from the backend root before packaging or IntelliJ runs. Stop running JARs first to avoid file-lock errors during repackage.

## Troubleshooting

| Problem | Fix |
|---------|-----|
| Port already in use | Run `scripts\stop-all.ps1`, then restart |
| IntelliJ run fails | Ensure script-started services are stopped; open backend folder as Maven root |
| `mvn install` JAR lock | Stop services on 8080, 8081, 8083, 8084, 8761, 9191 |
| Gateway 503 / service not found | Wait for Eureka registration; start Eureka before other services |
| Checkout shows no error message | Ensure gateway + orders service are running; check `scripts/logs/orders.log` |
| Payment / invoice internal errors | Set `INTERNAL_SERVICE_KEY` consistently across services |

## License

Private project — all rights reserved unless otherwise specified by the repository owner.