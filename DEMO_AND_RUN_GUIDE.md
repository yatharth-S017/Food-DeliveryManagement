# Online Food Ordering & Delivery Management System
## Evaluation & Demonstration Guide

This guide walks through running the microservices and demonstrating every single requirement from the evaluation rubric.

---

### Part 1: Infrastructure Startup (MySQL, Redis, Kafka)

Start Docker Desktop on your machine, then run in PowerShell:
```powershell
docker compose up -d
```
This automatically starts:
* **MySQL 8.0** on `localhost:3307` with databases `userdb`, `fooddb`, `orderdb`, `paymentdb`
* **Redis 7** on `localhost:6379`
* **Apache Kafka (KRaft mode)** on `localhost:9092`

To verify all 3 are running:
```powershell
docker ps
```

---

### Part 2: Starting the Microservices

You can start each service in separate terminal windows or from Eclipse IDE (Run As -> Spring Boot App):

```powershell
# 1. Config Server (MUST START FIRST - Port 8888)
.\mvnw.cmd spring-boot:run -pl config-server

# 2. User Service (Port 8081)
.\mvnw.cmd spring-boot:run -pl user-service

# 3. Food Service (Port 8082)
.\mvnw.cmd spring-boot:run -pl food-service

# 4. Payment Service (Port 8084)
.\mvnw.cmd spring-boot:run -pl payment-service

# 5. Order Service (Port 8083)
.\mvnw.cmd spring-boot:run -pl order-service

# 6. Notification Service (Port 8085)
.\mvnw.cmd spring-boot:run -pl notification-service

# 7. API Gateway (Port 8080)
.\mvnw.cmd spring-boot:run -pl api-gateway
```

---

### Part 3: Step-by-Step Demonstration (7 Evaluation Checks)

All requests can be routed through **API Gateway** (`http://localhost:8080`) or tested directly against the service port.

---

#### Check 1: Centralized Configuration from Config Server
Verify that the Config Server serves configuration centrally:
```powershell
curl http://localhost:8888/user-service/default
curl http://localhost:8888/food-service/default
curl http://localhost:8888/order-service/default
```
*Expected Result*: Returns JSON showing database URLs, Kafka endpoints, Redis endpoints, and JWT secrets served centrally from Config Server.

---

#### Check 2: Customer & Admin Registration & Login (JWT Authentication)

**1. Register a Customer:**
```powershell
curl -X POST http://localhost:8080/users/register `
  -H "Content-Type: application/json" `
  -d '{\"name\":\"Alice\",\"email\":\"alice@example.com\",\"password\":\"password123\",\"phone\":\"9876543210\",\"role\":\"CUSTOMER\"}'
```

**2. Register an Admin:**
```powershell
curl -X POST http://localhost:8080/users/register `
  -H "Content-Type: application/json" `
  -d '{\"name\":\"Admin Bob\",\"email\":\"admin@example.com\",\"password\":\"admin123\",\"phone\":\"9998887770\",\"role\":\"ADMIN\"}'
```

**3. Login as Customer (Obtain Customer JWT):**
```powershell
curl -X POST http://localhost:8080/users/login `
  -H "Content-Type: application/json" `
  -d '{\"email\":\"alice@example.com\",\"password\":\"password123\"}'
```
*Save the returned `token` as `$CUSTOMER_TOKEN`.*

**4. Login as Admin (Obtain Admin JWT):**
```powershell
curl -X POST http://localhost:8080/users/login `
  -H "Content-Type: application/json" `
  -d '{\"email\":\"admin@example.com\",\"password\":\"admin123\"}'
```
*Save the returned `token` as `$ADMIN_TOKEN`.*

---

#### Check 3: Role-Based Authorization & HTTP 403 Forbidden Verification

**1. Customer attempts to add a food item (Unauthorized):**
```powershell
curl -i -X POST http://localhost:8080/foods `
  -H "Authorization: Bearer <CUSTOMER_TOKEN>" `
  -H "Content-Type: application/json" `
  -d '{\"foodName\":\"Margherita Pizza\",\"price\":250.0,\"category\":\"Pizza\",\"availability\":true,\"restaurantId\":1}'
```
*Expected Result*: **HTTP 403 Forbidden** (Customers are not allowed to add food).

**2. Admin adds the food item (Authorized):**
```powershell
curl -i -X POST http://localhost:8080/foods `
  -H "Authorization: Bearer <ADMIN_TOKEN>" `
  -H "Content-Type: application/json" `
  -d '{\"foodName\":\"Margherita Pizza\",\"price\":250.0,\"category\":\"Pizza\",\"availability\":true,\"restaurantId\":1}'
```
*Expected Result*: **HTTP 201 Created** (Food ID: 1 created).

---

#### Check 4: Redis Cache Hit vs Cache Miss Demonstration

**1. First request for Food #1 (Cache Miss):**
```powershell
curl http://localhost:8080/foods/1
```
*Food Service Console Output*:
```text
>>> [REDIS CACHE MISS] Food ID: 1 NOT found in Redis!
>>> Querying MySQL database directly...
>>> Storing fetched result into Redis cache...
```

**2. Second request for Food #1 (Cache Hit):**
```powershell
curl http://localhost:8080/foods/1
```
*Food Service Console Output*:
No database query executed! Served directly from Redis in sub-millisecond response time.

---

#### Check 5: Redis Cache Eviction Demonstration

When an Admin updates or deletes the food item, the Redis cache entry is evicted to prevent stale data:

**1. Admin updates Food #1:**
```powershell
curl -X PUT http://localhost:8080/foods/1 `
  -H "Authorization: Bearer <ADMIN_TOKEN>" `
  -H "Content-Type: application/json" `
  -d '{\"foodName\":\"Margherita Pizza Extra Cheese\",\"price\":300.0,\"category\":\"Pizza\",\"availability\":true,\"restaurantId\":1}'
```
*Food Service Console Output*:
```text
>>> [REDIS CACHE EVICTION] Updating Food ID: 1
>>> Evicting stale entry from Redis cache...
```

**2. Next GET request confirms cache miss and fresh data:**
```powershell
curl http://localhost:8080/foods/1
```
*Result*: Displays updated price ($300.0) after querying database and re-caching in Redis.

---

#### Check 6 & 7: Complete Order Flow (OpenFeign + Payment + Kafka Event -> Notification)

**1. Customer places order:**
```powershell
curl -X POST http://localhost:8080/orders `
  -H "Content-Type: application/json" `
  -d '{\"customerId\":1,\"restaurantId\":1,\"items\":[{\"foodId\":1,\"quantity\":2}]}'
```

**Console Traces during Order Execution**:
1. **Order Service Console**:
   ```text
   >>> [STEP 1] Validating food items via OpenFeign FoodClient...
   >>> [STEP 2] Processing payment of $600.0 via OpenFeign PaymentClient...
   >>> [STEP 3] Order successfully placed! Publishing event to Kafka topic 'order-created'...
   >>> [KAFKA PRODUCER] Publishing event to topic 'order-created'
   >>> Payload: orderId=1, customerId=1, totalAmount=600.0, status=PLACED
   ```
2. **Notification Service Console**:
   ```text
   >>> [KAFKA CONSUMER] Event received from topic 'order-created'!
   >>> Payload: OrderId=1, CustomerId=1, TotalAmount=$600.0, Status=PLACED
   >>> [SIMULATED NOTIFICATION SENT TO CUSTOMER #1]:
   >>> "Your order #1 has been successfully placed."
   ```

**2. Verify Order in Order Service:**
```powershell
curl http://localhost:8080/orders/1
```

**3. Verify Customer Orders:**
```powershell
curl http://localhost:8080/orders/customer/1
```

---

### Part 4: Viva / Justification Answers

| Technology | Why It Is Used in This Project |
| :--- | :--- |
| **Spring Cloud Config Server** | Eliminates hardcoding database credentials and service URLs across individual microservices. Configurations can be updated centrally without rebuilding code. |
| **OpenFeign** | Provides declarative REST clients between microservices. Ensures strict microservice independence by prohibiting direct database sharing (Order Service does not touch Food or Payment DBs). |
| **Apache Kafka** | Provides decoupled, asynchronous event processing. Order Service does not block waiting for notification dispatch; it emits an event, allowing Notification Service to consume independently with guaranteed delivery. |
| **Redis** | High-performance in-memory caching. Accelerates frequently accessed read endpoints (`GET /foods/{id}`) and reduces database load. Eviction hooks ensure cache consistency when data changes. |
| **Spring Security & JWT** | Stateless authentication across distributed microservices. JWT cryptographically conveys customer identity and roles (`CUSTOMER`, `ADMIN`), enabling RBAC and 403 Forbidden enforcement. |
| **API Gateway** | Single entry point for clients, preventing direct exposure of internal service ports and handling URL routing centrally. |
