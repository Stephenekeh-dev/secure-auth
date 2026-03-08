#  Secure Auth

> A production-ready, enterprise-grade **JWT Authentication & Authorization** system built with **Spring Boot 3**, **PostgreSQL**, **Redis**, and **Flyway**. Designed with security-first principles including email verification, refresh token rotation, MFA scaffolding, and role-based access control.

<br/>

![Java](https://img.shields.io/badge/Java-17+-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5.4-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15+-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-7+-DC382D?style=for-the-badge&logo=redis&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-JJWT_0.12-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white)
![Flyway](https://img.shields.io/badge/Flyway-Migrations-CC0200?style=for-the-badge&logo=flyway&logoColor=white)

---

##  Table of Contents

- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [Architecture Overview](#-architecture-overview)
- [Database Schema](#-database-schema)
- [Authentication Flow](#-authentication-flow)
- [API Reference](#-api-reference)
- [Project Structure](#-project-structure)
- [Getting Started](#-getting-started)
- [Configuration](#-configuration)
- [Security Design](#-security-design)
- [Roadmap](#-roadmap)

---

## Features

| Feature | Status |
|---|---|
| User Registration with Email Verification | Complete |
| JWT Access Token Authentication | Complete |
| Refresh Token Rotation |  Complete |
| Role-Based Access Control (RBAC) | Complete |
| Secure Logout (single token & all sessions) | Complete |
| Flyway Database Migrations | Complete |
| Redis OTP/Session Support | Complete |
| Account Locking & Expiry Controls |  Complete |
| Forgot Password / Reset Password | 🔧 Stubbed |
| Multi-Factor Authentication (TOTP) | 🔧 Stubbed |
| Profile Image Upload | 🔧 In Progress |

---

## 🛠 Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17+ |
| Framework | Spring Boot 3.5.4 |
| Security | Spring Security 6, JWT (JJWT 0.12) |
| Database | PostgreSQL 15+ |
| ORM | Hibernate / Spring Data JPA |
| Migrations | Flyway |
| Caching / OTP | Redis 7+ |
| Email | Spring Mail (SMTP / Gmail) |
| Build Tool | Maven |
| Boilerplate | Lombok |

---

## 🏛 Architecture Overview

```mermaid
graph TB
    Client(["Client<br/>(Browser / Mobile App)"])

    subgraph API["Spring Boot Application"]
        direction TB
        AC["AuthController<br/>/api/auth/**"]
        SF["JwtAuthenticationFilter"]
        AS["AuthService"]
        UDS["CustomUserDetailsService"]
        JP["JwtProvider"]
        RTS["RefreshTokenService"]
        ES["EmailService"]
        RS["RedisService"]
        SC["SecurityConfig"]
    end

    subgraph Storage["Persistence Layer"]
        PG[(" PostgreSQL")]
        RD[("Redis")]
    end

    subgraph Mail["External Services"]
        SMTP[" Gmail SMTP"]
    end

    Client -->|"HTTP Request"| SF
    SF -->|"Validate JWT"| JP
    SF -->|"Load User"| UDS
    AC --> AS
    AS --> JP
    AS --> RTS
    AS --> ES
    AS --> RS
    UDS -->|"findByEmail"| PG
    RTS -->|"CRUD"| PG
    ES -->|"Send Email"| SMTP
    RS -->|"OTP Store/Read"| RD
    SC -->|"Configures"| SF

    style API fill:#f0f7ff,stroke:#2196F3,stroke-width:2px
    style Storage fill:#fff3e0,stroke:#FF9800,stroke-width:2px
    style Mail fill:#f3e5f5,stroke:#9C27B0,stroke-width:2px
```

---

## 🗄 Database Schema

```mermaid
erDiagram
    USERS {
        bigserial id PK
        varchar username UK
        varchar email UK
        varchar password
        varchar first_name
        varchar last_name
        varchar profile_image
        boolean enabled
        boolean account_non_locked
        boolean account_non_expired
        boolean credentials_non_expired
        timestamptz created_at
        timestamptz updated_at
    }

    ROLES {
        bigserial id PK
        varchar name UK
        varchar description
    }

    USER_ROLES {
        bigint user_id FK
        bigint role_id FK
    }

    REFRESH_TOKENS {
        bigserial id PK
        bigint user_id FK
        varchar token UK
        timestamptz expiry_date
        timestamptz created_at
    }

    EMAIL_VERIFICATION_TOKENS {
        bigserial id PK
        varchar token UK
        bigint user_id FK
        timestamptz expiry_date
        timestamptz created_at
    }

    USERS ||--o{ USER_ROLES : "has"
    ROLES ||--o{ USER_ROLES : "assigned to"
    USERS ||--o{ REFRESH_TOKENS : "owns"
    USERS ||--o{ EMAIL_VERIFICATION_TOKENS : "issued"
```

---

## 🔄 Authentication Flow

### Registration & Email Verification

```mermaid
sequenceDiagram
    actor User
    participant Controller as AuthController
    participant Service as AuthService
    participant DB as PostgreSQL
    participant Mail as Gmail SMTP

    User->>Controller: POST /api/auth/register
    Controller->>Service: register(RegisterRequest)
    Service->>Service: Encode password (BCrypt)
    Service->>DB: Save User (enabled=false)
    Service->>DB: Save VerificationToken (24h TTL)
    Service->>Mail: Send verification email
    Mail-->>User:  Email with token link
    Controller-->>User: 200 OK — "Check your email"

    Note over User,Mail: User clicks the link in their email

    User->>Controller: GET /api/auth/verify-email?token=xxx
    Controller->>Service: verifyEmail(token)
    Service->>DB: Find token → validate expiry
    Service->>DB: Set user.enabled = true
    Service->>DB: Delete used token
    Controller-->>User: 200 OK — "Email verified. You can now log in."
```

### Login & Token Issuance

```mermaid
sequenceDiagram
    actor User
    participant Controller as AuthController
    participant AuthMgr as AuthenticationManager
    participant Service as AuthService
    participant JWT as JwtProvider
    participant DB as PostgreSQL

    User->>Controller: POST /api/auth/login
    Controller->>Service: login(AuthRequest)
    Service->>AuthMgr: authenticate(email, password)
    AuthMgr-->>Service:  Authentication passed
    Service->>DB: findByEmail → check enabled
    Service->>JWT: generateToken(email)
    JWT-->>Service: accessToken (1h)
    Service->>DB: Delete old refresh tokens
    Service->>DB: Save new RefreshToken (30 days)
    Service-->>Controller: AuthenticationResponse
    Controller-->>User: 200 OK {accessToken, refreshToken, userInfo}
```

### Token Refresh

```mermaid
sequenceDiagram
    actor User
    participant Controller as AuthController
    participant Service as AuthService
    participant RTS as RefreshTokenService
    participant JWT as JwtProvider
    participant DB as PostgreSQL

    User->>Controller: POST /api/auth/refresh
    Controller->>Service: refresh(RefreshRequest)
    Service->>RTS: validate(refreshToken)
    RTS->>DB: findByToken
    RTS->>RTS: Check expiry date
    RTS-->>Service:  Valid RefreshToken
    Service->>JWT: generateToken(email)
    JWT-->>Service: New accessToken
    Service-->>Controller: AuthenticationResponse
    Controller-->>User: 200 OK {newAccessToken, sameRefreshToken, userInfo}
```

### Request Authorization (JWT Filter)

```mermaid
sequenceDiagram
    actor User
    participant Filter as JwtAuthenticationFilter
    participant JWT as JwtProvider
    participant UDS as CustomUserDetailsService
    participant DB as PostgreSQL
    participant Controller as Protected Controller

    User->>Filter: GET /api/protected (Bearer token)
    Filter->>Filter: Extract token from Authorization header
    Filter->>JWT: validateToken(token)
    JWT-->>Filter: Valid
    Filter->>JWT: getEmailFromToken(token)
    JWT-->>Filter: email
    Filter->>UDS: loadUserByUsername(email)
    UDS->>DB: findByEmail
    DB-->>UDS: User entity
    UDS-->>Filter: UserDetails
    Filter->>Filter: Set SecurityContext authentication
    Filter->>Controller: Pass request through
    Controller-->>User: 200 OK — Protected resource
```

---

## API Reference

### Base URL
```
http://localhost:8080/api/auth
```

### Endpoints

| Method | Endpoint | Auth Required | Description |
|---|---|---|---|
| `POST` | `/register` | ❌ | Register a new user |
| `GET` | `/verify-email?token=` | ❌ | Verify email address |
| `POST` | `/login` | ❌ | Login and get tokens |
| `POST` | `/refresh` | ❌ | Refresh access token |
| `POST` | `/logout` | ✅ | Logout (revoke refresh token) |
| `POST` | `/forgot-password` | ❌ | Request password reset link |
| `POST` | `/reset-password` | ❌ | Reset password with token |
| `GET` | `/mfa/setup` | ✅ | Get MFA QR code + secret |
| `POST` | `/mfa/verify` | ✅ | Verify MFA TOTP code |

### Request & Response Examples

#### Register
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "stevejobs",
  "email": "steve@example.com",
  "password": "SecurePass123!",
  "firstName": "Steve",
  "lastName": "Jobs",
  "profileImage": "/uploads/profile-pictures/steve.jpg"
}
```

#### Login Response
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
  "user": {
    "id": 1,
    "email": "steve@example.com",
    "firstName": "Steve",
    "lastName": "Jobs",
    "profileImage": "/uploads/profile-pictures/steve.jpg",
    "roles": ["ROLE_USER"]
  }
}
```

#### Authenticated Request
```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

---

##  Project Structure

```
secure-auth/
├── src/
│   ├── main/
│   │   ├── java/com/steve/secure_auth/
│   │   │   ├── config/
│   │   │   │   ├── SecurityConfig.java          # Spring Security filter chain
│   │   │   │   └── JwtAuthenticationFilter.java # JWT request interceptor
│   │   │   ├── controller/
│   │   │   │   └── AuthController.java          # REST endpoints
│   │   │   ├── dto/
│   │   │   │   ├── AuthRequest.java             # Login request
│   │   │   │   ├── RegisterRequest.java         # Registration request
│   │   │   │   ├── RefreshRequest.java          # Token refresh request
│   │   │   │   ├── AuthenticationResponse.java  # Login/refresh response + UserInfo
│   │   │   │   ├── ForgotPasswordRequest.java
│   │   │   │   ├── ResetPasswordRequest.java
│   │   │   │   ├── MfaSetupResponse.java
│   │   │   │   └── MfaVerifyRequest.java
│   │   │   ├── model/
│   │   │   │   ├── User.java                    # User entity
│   │   │   │   ├── Role.java                    # Role entity
│   │   │   │   ├── RefreshToken.java            # Refresh token entity
│   │   │   │   └── VerificationToken.java       # Email verification token entity
│   │   │   ├── repository/
│   │   │   │   ├── UserRepository.java
│   │   │   │   ├── RoleRepository.java
│   │   │   │   ├── RefreshTokenRepository.java
│   │   │   │   └── VerificationTokenRepository.java
│   │   │   ├── service/
│   │   │   │   ├── AuthService.java             # Core auth business logic
│   │   │   │   ├── CustomUserDetailsService.java
│   │   │   │   ├── RefreshTokenService.java
│   │   │   │   ├── EmailService.java
│   │   │   │   └── RedisService.java
│   │   │   └── util/
│   │   │       └── JwtProvider.java             # JWT generation & validation
│   │   └── resources/
│   │       ├── application.yml                  # App configuration
│   │       └── db/migration/
│   │           └── V1__init_schema.sql          # Flyway baseline migration
│   └── test/
└── pom.xml
```

---

##  Getting Started

### Prerequisites

| Tool | Version |
|---|---|
| Java JDK | 17 or 21 |
| Maven | 3.8+ (or use `./mvnw`) |
| PostgreSQL | 15+ |
| Redis | 7+ |

### 1. Clone the Repository

```bash
git clone https://github.com/your-username/secure-auth.git
cd secure-auth
```

### 2. Create the Database

```sql
CREATE DATABASE secure_auth;
```

### 3. Configure the Application

Create `src/main/resources/application-local.yml` (never commit this):

```yaml
spring:
  datasource:
    username: postgres
    password: your_db_password
  mail:
    username: your_email@gmail.com
    password: your_gmail_app_password

jwt:
  secret: "your-256-bit-base64-secret"

app:
  base-url: http://localhost:8080
```

### 4. Run the Application

```bash
# Using local profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

Flyway will automatically run `V1__init_schema.sql` and seed the default roles on first startup.

### 5. Test Registration Flow

```bash
# Register a new user
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "SecurePass123!",
    "firstName": "Test",
    "lastName": "User"
  }'

# Check your email, click the verification link, then login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "SecurePass123!"
  }'
```

---

##  Configuration

### `application.yml` Reference

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/secure_auth
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD}

  jpa:
    hibernate:
      ddl-auto: validate           # Flyway manages schema — never use create/update
    database-platform: org.hibernate.dialect.PostgreSQLDialect

  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true

  mail:
    host: smtp.gmail.com
    port: 465
    username: ${MAIL_USERNAME}
    password: ${MAIL_PASSWORD}     # Use Gmail App Password, not your account password
    properties:
      mail.smtp.ssl.enable: true

  redis:
    host: localhost
    port: 6379

jwt:
  secret: ${JWT_SECRET}            # Min 256-bit Base64 encoded secret
  expiration-ms: 3600000           # 1 hour

app:
  base-url: ${APP_BASE_URL:http://localhost:8080}
```

### Environment Variables (Production)

| Variable | Description |
|---|---|
| `DB_USERNAME` | PostgreSQL username |
| `DB_PASSWORD` | PostgreSQL password |
| `MAIL_USERNAME` | Gmail address |
| `MAIL_PASSWORD` | Gmail App Password |
| `JWT_SECRET` | Base64 encoded 256-bit secret |
| `APP_BASE_URL` | Public base URL of the application |

---

## Security Design

```mermaid
graph LR
    subgraph Tokens["Token Strategy"]
        AT["Access Token\n⏱ 1 hour TTL\nJWT — stateless"]
        RT["Refresh Token\n 30 day TTL\nUUID — stored in DB"]
    end

    subgraph Protection["Security Layers"]
        BCrypt[" BCrypt\nPassword Hashing"]
        Verify["Email Verification\nAccount disabled until confirmed"]
        Revoke[" Token Revocation\nLogout invalidates refresh token"]
        RBAC["🛡 RBAC\nROLE_USER / ROLE_ADMIN"]
        Stateless[" Stateless Sessions\nNo server-side session storage"]
    end

    AT --> Stateless
    RT --> Revoke
    BCrypt --> Verify
    Verify --> RBAC
```

- **Passwords** are hashed with BCrypt before storage — never stored in plaintext
- **Access tokens** are short-lived (1 hour) JWTs signed with HMAC-SHA256
- **Refresh tokens** are opaque UUIDs stored in the database and can be revoked
- **Email verification** disables accounts until the user confirms their email
- **One refresh token per user** — old tokens are deleted on new login
- **Stateless sessions** — Spring Security is configured with `STATELESS` session policy; no HTTP sessions are created
- **RBAC** — every endpoint can be protected by role via `@PreAuthorize` or `requestMatchers`

---

## 🗺 Roadmap

- [x] User registration + email verification
- [x] JWT login / refresh / logout
- [x] Role-based access control
- [x] Flyway schema migrations
- [x] Redis integration
- [ ] Forgot password / reset password flow
- [ ] TOTP-based Multi-Factor Authentication (Google Authenticator)
- [ ] Profile image upload (local file system)
- [ ] OAuth2 Social Login (Google, GitHub)
- [ ] Rate limiting on auth endpoints
- [ ] Audit logging (login attempts, IP tracking)
- [ ] Docker + Docker Compose setup

---

## Author

**Stephen Ekeh**
- GitHub: [@steve](https://github.com/Stephenekeh-dev/secure-auth)
- Email: ekehsteven2@gmail.com

---

##  License

This project is licensed under the MIT License.

---

<div align="center">
  <sub>Built with ❤️ using Spring Boot 3 · PostgreSQL · Redis · JWT</sub>
</div>
