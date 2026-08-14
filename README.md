# BrickWorkPro 2.0 🧱🏗️

**BrickWorkPro 2.0** is an enterprise-grade, microservices-driven management and sales platform built specifically for brick manufacturing and construction material businesses. It provides end-to-end management across user authentication, product cataloging, raw material inventory, multi-stage production tracking, dynamic ordering, payment gateway processing (Razorpay, COD, UTR), automated PDF invoicing, and real-time sales analytics.

---

## 🌟 Key Features

### 👥 User & Identity Management (`users-brickwork`)
- **Authentication & Security:** Stateless JWT authentication, OTP verification via email, and BCrypt password encryption.
- **Role-Based Access Control (RBAC):** Distinct privileges for `ADMIN`, `EMPLOYEE`, and `CUSTOMER` users.
- **Customer & Staff Management:** Profiles for individual/business customers, employee management, and direct contact inquiry tracking.

### 📦 Product & Production Tracking (`products-brickwork`)
- **Product Catalog:** Manage finished products (fly-ash bricks, red bricks, interlocking pavers, concrete blocks) with images, attachments, and pricing.
- **Raw Material Inventory:** Real-time monitoring and stock level management for raw materials (cement, fly ash, stone dust, sand).
- **Multi-Stage Production Logging:** Track manufacturing batches across distinct production stages (e.g., Mixing, Molding, Drying, Firing, Quality Check, Packaging).
- **Production Analytics:** Insights on batch throughput, yield rates, and raw material consumption efficiency.

### 🛒 Order Management & Fulfillment (`orders-brickwork`)
- **Order Lifecycle:** Complete order tracking across statuses (`PENDING`, `PROCESSING`, `PRODUCED`, `DISPATCHED`, `DELIVERED`, `CANCELLED`).
- **Inter-Service Feign Integration:** Synchronizes seamlessly with the Product service for stock validation and the Finance service for payment triggers.
- **Multi-Channel Notifications:** Automated order confirmations and status updates sent via **Email** and **WhatsApp**.
- **Sales Analytics:** Native projections for revenue by payment method, top-selling products, and regional sales performance.

### 💳 Finance, Payments & Invoicing (`finance-brickwork`)
- **Flexible Payment Options:** Integration with **Razorpay**, Cash on Delivery (COD), and direct bank transfer with **UTR transaction submission**.
- **Webhook Processing:** Asynchronous payment verification via secure Webhook controllers.
- **Automated PDF Invoices:** Dynamic generation of downloadable PDF invoices using HTML/Thymeleaf templates.
- **Refund & Transaction History:** Audit logs for all payment attempts, status updates, and processed refunds.

---

## 🏗️ System Architecture

Built on a robust **Spring Cloud Microservices Architecture**, all client traffic flows through a central **Spring Cloud API Gateway**, while services register dynamically with an **Eureka Discovery Server**. Internal microservices communicate securely using **OpenFeign** with custom security interceptors.

```text
                               ┌────────────────────────┐
                               │   Angular 2.0 Frontend │
                               └───────────┬────────────┘
                                           │ HTTP/REST
                                           ▼
                               ┌────────────────────────┐
                               │   Spring API Gateway   │ (Centralized Route & Security)
                               └───────────┬────────────┘
                                           │
          ┌────────────────────────────────┼────────────────────────────────┐
          │ Discovery Registration         │ Inter-service Routing          │
          ▼                                ▼                                ▼
┌───────────────────┐            ┌───────────────────┐            ┌───────────────────┐
│ Service Registry  │            │   Users Service   │            │ Products Service  │
│  (Eureka Server)  │            │    (Port 8081)    │            │    (Port 8082)    │
└───────────────────┘            └───────────────────┘            └───────────────────┘
                                           │                                │
                                           ▼                                ▼
                                 ┌───────────────────┐            ┌───────────────────┐
                                 │  Orders Service   │ ──(Feign)─►│  Finance Service  │
                                 │    (Port 8083)    │            │    (Port 8084)    │
                                 └───────────────────┘            └───────────────────┘
```

---

## 🛠️ Tech Stack

### Backend

- **Language & Core:** Java 17+, Spring Boot 3.x
- **Microservices Infrastructure:** Spring Cloud Gateway, Spring Cloud Netflix Eureka, OpenFeign
- **Security & Libraries:** Spring Security, JWT (JSON Web Tokens), `common-security-brickwork` shared library
- **Database & ORM:** MySQL / MariaDB, Spring Data JPA, Hibernate
- **Payments & Communication:** Razorpay API, JavaMail, WhatsApp API
- **Document Generation:** Thymeleaf, iText / OpenPDF
- **Build Tool:** Apache Maven

### Frontend

- **Framework:** Angular
- **Styling:** CSS3, Tailwind CSS / PostCSS
- **Build & Package Manager:** Node.js, npm, Angular CLI

---

## 📂 Repository Structure

```text
brickworks-2.0/
├── BrickWorkPro-2.0-Backend/
│   ├── service-registry-brickwork/    # Eureka Discovery Server (Service Registry)
│   ├── api-gateway/                   # Spring Cloud Gateway & Central Routing
│   ├── common-security-brickwork/     # Shared JWT, Feign Interceptors, Global Exceptions
│   ├── users-brickwork/               # Auth, User, Employee & Contact Service
│   ├── products-brickwork/            # Products, Raw Materials & Production Analytics
│   ├── orders-brickwork/              # Order Processing, Analytics & Notifications
│   └── finance-brickwork/             # Payments, Razorpay, Webhooks & PDF Invoices
│
└── BrickWorkPro-2.0-Frontend/          # Angular Single Page Application (SPA)
    ├── src/                           # App source code, components, guards, services
    └── public/assets/                 # Product images, project media, and static assets
```

---

## ⚙️ Getting Started

### Prerequisites

- **Java Development Kit (JDK 17+)**
- **Node.js (v18+)** & **npm**
- **Angular CLI** (`npm install -g @angular/cli`)
- **MySQL / MariaDB Server**

---

### Step 1: Database Setup

Create separate database instances for each microservice:

```sql
CREATE DATABASE brickwork_users_db;
CREATE DATABASE brickwork_products_db;
CREATE DATABASE brickwork_orders_db;
CREATE DATABASE brickwork_finance_db;
```

---

### Step 2: Launch Backend Microservices

Start the services in the following order:

**1. Service Registry (Eureka)**
```bash
cd BrickWorkPro-2.0-Backend/service-registry-brickwork
./mvnw spring-boot:run
```
Dashboard available at: `http://localhost:8761`

**2. API Gateway**
```bash
cd BrickWorkPro-2.0-Backend/api-gateway
./mvnw spring-boot:run
```
Gateway Port: `8080`

**3. Users Service**
```bash
cd BrickWorkPro-2.0-Backend/users-brickwork/users-app
../../mvnw spring-boot:run
```

**4. Products Service**
```bash
cd BrickWorkPro-2.0-Backend/products-brickwork/products-app
../../mvnw spring-boot:run
```

**5. Orders Service**
```bash
cd BrickWorkPro-2.0-Backend/orders-brickwork/orders-app
../../mvnw spring-boot:run
```

**6. Finance Service**
```bash
cd BrickWorkPro-2.0-Backend/finance-brickwork/finance-app
../../mvnw spring-boot:run
```

---

### Step 3: Launch Frontend Application

1. Navigate to the frontend workspace:
```bash
cd BrickWorkPro-2.0-Frontend
```

2. Install dependencies:
```bash
npm install
```

3. Run the development server:
```bash
ng serve
```

4. Open your browser and navigate to `http://localhost:4200/`.

---

## 📡 API Endpoint Overview (routed via Gateway)

| Module | Method | Endpoint | Description | Access |
|---|---|---|---|---|
| Auth | POST | `/api/auth/login` | Authenticate user & return JWT | Public |
| Auth | POST | `/api/auth/register/customer` | Register a new customer | Public |
| Users | GET | `/api/admin/users` | List all users & employees | Admin |
| Products | GET | `/api/products` | Retrieve catalog of products | Public / User |
| Products | POST | `/api/production/logs` | Create a manufacturing batch log | Admin / Staff |
| Orders | POST | `/api/orders` | Place a new order | Customer |
| Orders | GET | `/api/analytics/sales` | Get sales performance metrics | Admin |
| Finance | POST | `/api/payments/razorpay/create` | Initiate Razorpay transaction | Customer |
| Finance | GET | `/api/invoices/{orderId}/download` | Download generated PDF invoice | Customer / Admin |

---

## 📄 License

This project is licensed under the MIT License.
