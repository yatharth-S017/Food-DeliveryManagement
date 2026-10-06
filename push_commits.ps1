#!/usr/bin/env pwsh
# Run this from D:\foodOrdering in PowerShell

Set-Location D:\foodOrdering

# ─── Commit 1: Project skeleton ──────────────────────────────
git add pom.xml .gitignore .env.example docker-compose.yml init-db.sql DEMO_AND_RUN_GUIDE.md AGENTS.md .mvn mvnw mvnw.cmd
git commit -m "chore: initialise multi-module Maven project skeleton

- Root pom.xml with Spring Boot 3.3.4, Spring Cloud 2023.0.3
- 8 sub-modules declared: config-server, api-gateway, user-service,
  food-service, order-service, payment-service, notification-service
- docker-compose.yml for MySQL 8, Redis 7, Kafka (KRaft) infrastructure
- init-db.sql creates userdb, fooddb, orderdb, paymentdb
- .gitignore excludes target/, .env, IDE files
- .env.example documents all required environment variables"

# ─── Commit 2: Config Server ─────────────────────────────────
git add config-server/
git commit -m "feat(config-server): add centralised Spring Cloud Config Server (port 8888)

- Native profile serves configs from classpath:/configurations/
- Separate YAML per service: user, food, order, payment, notification, api-gateway
- All sensitive values (DB password, JWT secret, Kafka URL) read from
  environment variables via \${VAR:default} — no hardcoded secrets
- Actuator endpoints exposed for runtime config inspection"

# ─── Commit 3: User Service + API Gateway ────────────────────
git add user-service/ api-gateway/
git commit -m "feat(user-service, api-gateway): JWT authentication, RBAC, and API Gateway routing

user-service (port 8081):
- Customer registration and login with BCrypt password encoding
- JWT token generation (HMAC-SHA256) with role claim (CUSTOMER / ADMIN)
- Spring Security: /register and /login public; DELETE /users/** ADMIN only
- JwtAuthenticationFilter validates Bearer tokens on every request
- Connects to MySQL userdb via config-server

api-gateway (port 8080):
- Spring Cloud Gateway routes traffic to all downstream services
- Routes: /users/** → 8081, /foods/** /restaurants/** → 8082,
  /orders/** → 8083, /payments/** → 8084
- Global CORS configuration (GET, POST, PUT, DELETE, OPTIONS)"

# ─── Commit 4: Food Service ───────────────────────────────────
git add food-service/
git commit -m "feat(food-service): food CRUD with Redis caching and role-based access (port 8082)

- GET /foods/{id} annotated @Cacheable('foods') — Redis cache miss/hit
  logged explicitly for demo visibility
- PUT /foods/{id} and DELETE /foods/{id} annotated @CacheEvict('foods')
  — eviction logged on every mutation
- RedisCacheManager configured with 10-minute TTL, JSON serialization,
  null-value caching disabled
- Food entity implements Serializable (required for Redis serialization)
- JWT filter enforces: GET public, POST/PUT/DELETE require ADMIN role
- Connects to MySQL fooddb via config-server"

# ─── Commit 5: Order Service + Payment Service ────────────────
git add order-service/ payment-service/
git commit -m "feat(order-service, payment-service): OpenFeign inter-service calls and Kafka event publishing

order-service (port 8083):
- POST /orders orchestrates full flow in 3 steps:
  Step 1 — validate food availability via OpenFeign FoodClient
  Step 2 — process payment via OpenFeign PaymentClient
  Step 3 — publish OrderCreatedEvent to Kafka topic 'order-created'
- Payment and Kafka failures handled gracefully (no crash, status updated)
- GET /orders/{id}, GET /orders/customer/{id}, PUT /orders/{id}/cancel
- Connects to MySQL orderdb; Feign URLs from config-server env vars

payment-service (port 8084):
- POST /payments — simulated: amount > 0 → SUCCESS, else FAILED
- Persists every payment record to MySQL paymentdb
- Called internally by order-service only (no auth filter needed)"

# ─── Commit 6: Notification Service + environment wiring ──────
git add notification-service/ .env.example
git commit -m "feat(notification-service): Kafka consumer for async order notifications (port 8085)

- @KafkaListener on topic 'order-created', group 'notification-group'
- Deserializes OrderCreatedEvent via Spring Kafka JsonDeserializer
  (spring.json.trusted.packages=* to allow cross-module deserialization)
- Logs simulated customer notification for each order placed
- Decoupled from order-service: order completes instantly, notification
  is processed asynchronously

env wiring (all services):
- All application.yml files use \${VAR:default} for MYSQL_ROOT_PASSWORD,
  MYSQL_HOST, MYSQL_PORT, JWT_SECRET, JWT_EXPIRATION, REDIS_HOST,
  REDIS_PORT, KAFKA_BOOTSTRAP_SERVERS
- docker-compose.yml reads MYSQL_ROOT_PASSWORD, MYSQL_PORT, REDIS_PORT
  from .env automatically
- .env excluded from version control via .gitignore"

Write-Host ""
Write-Host "=== All 6 commits created. Now push: ===" -ForegroundColor Green
Write-Host "git push -u origin main" -ForegroundColor Yellow
