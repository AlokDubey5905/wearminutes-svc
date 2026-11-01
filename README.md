# WearMinutes Service — Deep Documentation, HLD, AWS & Kafka

> **Project**: `wearminutes-svc/service`  
> **Stack**: Spring Boot 3.2.2 · Java 21 · PostgreSQL · JWT · Kafka · AWS  
> **Port**: `8081`  
> **DB**: `postgresql://localhost:5432/wearminutes_db` (`postgres` / `secretpassword`)

---

## 1. Executive Summary

This is the backend microservice for the **WearMinutes** e-commerce platform. It handles multi-role authentication (`CUSTOMER`, `VENDOR`, `DELIVERY`, `ADMIN`), product/shop inventory, order checkout with optimistic locking, event-driven vendor notifications, and role-based API security via JWT.

---

## 2. High-Level Design (HLD)

### 2.1 System Context

```
┌─────────────────────────────────────────────────────────────┐
│                        CLIENTS                               │
│  Web / Mobile / Admin Dashboard / Vendor Portal              │
└──────────────────────┬──────────────────────────────────────┘
                       │ HTTPS / REST + JWT Bearer
                       ▼
┌─────────────────────────────────────────────────────────────┐
│              WEARMINUTES SERVICE (Spring Boot)              │
│  Controllers → Services → Repositories → PostgreSQL         │
│  SecurityConfig + JwtFilter + JwtUtil                       │
│  EventPublisher → Kafka (planned) → NotificationService     │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│  DATA & MESSAGING LAYER                                     │
│  PostgreSQL (RDS)  │  Kafka (MSK / Self-managed)            │
│  S3 (Static Assets / Images)                                │
└─────────────────────────────────────────────────────────────┘
```

### 2.2 Component Diagram

```
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│  Controllers │────▶│   Services   │────▶│ Repositories │
│  (REST API)  │     │  (Business)  │     │  (JPA)       │
└──────────────┘     └──────┬───────┘     └──────┬───────┘
                            │                    │
                            ▼                    ▼
                     ┌──────────────┐     ┌──────────────┐
                     │  Events      │     │ PostgreSQL   │
                     │  (Kafka)     │     │ (RDS)        │
                     └──────┬───────┘     └──────────────┘
                            │
                            ▼
                     ┌──────────────┐
                     │ Notification │
                     │  Service     │
                     └──────────────┘
```

### 2.3 Data Flow — Order Checkout

```
Client (CUSTOMER)
  │ POST /api/orders/checkout {customerId, productId, quantity, paymentMode, deliveryAddress}
  ▼
OrderController
  │
  ▼
OrderService.checkoutOrder()
  │ 1. Find Product (ProductRepository)
  │ 2. Check Stock (OutOfStockException if < quantity)
  │ 3. Decrement Stock (Optimistic Lock @Version)
  │ 4. Create Order (PLACED, random OTP)
  │ 5. Save Order (OrderRepository)
  │ 6. Publish OrderPlacedEvent
  ▼
NotificationService.notifyVendorOnOrderPlaced()
  │
  ▼
ApplicationEventPublisher → OrderPlacedEventListener
  │ (Current: System.out.println)
  │ (Target: Kafka Producer → Topic: `orders.placed`)
  ▼
Kafka Topic: `orders.placed`
  │
  ▼
Kafka Consumer (Notification / Vendor Service)
  │ Send Email / SMS / Push to Vendor
```

---

## 3. Architecture Layers (Detailed)

### 3.1 Controllers (`controller/`)

| Controller | Path | Role Access | Description |
|---|---|---|---|
| `AdminController` | `/api/admin/users` | ADMIN | User CRUD + toggle status |
| `AdminOrderController` | `/api/admin/orders` | ADMIN | Order list, status update, checkout, delete |
| `CustomerAuthController` | `/api/customer/users` | CUSTOMER / ADMIN | Customer auth + profile |
| `CustomerProductController` | `/api/customer/products` | CUSTOMER / ADMIN | View products by shop / ID |
| `DeliveryController` | `/api/delivery/users` | DELIVERY / ADMIN | Delivery auth + profile |
| `OrderController` | `/api/orders` | CUSTOMER / ADMIN | Checkout endpoint |
| `HelloWorldController` | `/api/hello` | Public | Health check |
| `ShopController` | `/api/vendor/shops` | VENDOR / ADMIN | Shop CRUD |
| `VendorAuthController` | `/api/vendor/users` | VENDOR / ADMIN | Vendor auth + profile |
| `VendorProductController` | `/api/vendor/products` | VENDOR / ADMIN | Product CRUD |

### 3.2 Services (`service/`)

| Service | Key Methods | Notes |
|---|---|---|
| `AdminService` | `registerAdmin`, `getAllUsers`, `updateUser`, `deleteUser`, `toggleUserStatus`, `findByEmail` | Manages all roles |
| `CustomerService` | `registerCustomer`, `getAllCustomers`, `updateCustomer`, `deleteCustomer`, `findByEmail` | Filters by `CUSTOMER` |
| `DeliveryService` | `registerDeliveryPersonnel`, `getAllDeliveryPersonnel`, `updateDeliveryPersonnel`, `deleteDeliveryPersonnel`, `findByEmail` | Filters by `DELIVERY` |
| `VendorAuthService` | `registerVendor`, `getAllVendors`, `updateVendor`, `deleteVendor`, `findByEmail` | Filters by `VENDOR` |
| `ShopService` | `registerShop`, `getShopsByVendor`, `getShopById`, `updateShop`, `deleteShop` | Linked to `vendorId` |
| `VendorProductService` | `addProduct`, `getProductsByShop`, `getProductById`, `updateProduct`, `deleteProduct` | Uses `ShopRepository` |
| `OrderService` | `checkoutOrder` | Optimistic locking (`@Version`), `OutOfStockException`, `ConcurrentModificationException` |
| `NotificationService` | `notifyVendorOnOrderPlaced` | Publishes `OrderPlacedEvent` |

### 3.3 Repositories (`repository/`)

| Repository | Entity | Custom Queries |
|---|---|---|
| `UserRepository` | `User` | `findByEmail`, `existsByEmail` |
| `ProductRepository` | `Product` | `findByShopId` (`@Query`) |
| `ShopRepository` | `Shop` | `findByVendorId` |
| `OrderRepository` | `Order` | `findByCustomerId` |

---

## 4. Entities (JPA)

### 4.1 `User` (`users`)
- `id` (PK, AUTO)
- `email` (unique, not null)
- `password` (BCrypt encoded)
- `fullName`, `phoneNumber`, `address`, `city`, `state`, `pincode`
- `role`: `CUSTOMER` | `VENDOR` | `DELIVERY` | `ADMIN`
- `isActive` (default `true`)
- `createdAt`, `updatedAt` (`String` format `yyyy-MM-dd HH:mm:ss`)

### 4.2 `Shop` (`shops`)
- `id` (PK, AUTO)
- `vendorId` (FK reference to `User.id` for vendor)
- `shopName`, `ownerName`, `phoneNumber`, `address`, `city`, `state`, `pincode`
- `isActive` (default `true`)
- `createdAt`, `updatedAt`

### 4.3 `Product` (`products`)
- `id` (PK, AUTO)
- `shop` (`@ManyToOne` lazy, `@JoinColumn(name = "shop_id")`, `@JsonIgnore`)
- `title`, `fabric`, `color`, `price`, `stockQuantity`
- `version` (`@Version` — optimistic locking)
- `imageUrl`
- Index: `idx_products_shop_id`

### 4.4 `Order` (`orders`)
- `id` (PK, AUTO)
- `customerId`, `shopId`
- `totalAmount`
- `paymentMode`: `PREPAID` | `COD` (`@Enumerated(STRING)`)
- `orderStatus`: `PLACED` | `ACCEPTED` | `PACKED` | `OUT_FOR_DELIVERY` | `DELIVERED`
- `deliveryOtp` (4-digit random)
- `createdAt` (`LocalDateTime`, default `now()`)

---

## 5. Security Design

### 5.1 JWT Flow
```
Client Login (POST /api/{role}/users/login)
  │ Body: {email, password}
  ▼
Controller → Service.findByEmail() → PasswordEncoder.matches()
  │
  ▼
JwtUtil.generateToken(email, role) → HS256 signed token
  │
  ▼
Response: {token, role, userId}
```

### 5.2 Filter Chain (`JwtAuthenticationFilter`)
- Extracts `Authorization: Bearer <token>`
- Validates signature + expiration (`JwtUtil.validateToken`)
- Sets `SecurityContextHolder` with `UsernamePasswordAuthenticationToken`
- Authority format: `ROLE_ADMIN`, `ROLE_CUSTOMER`, etc.

### 5.3 Security Config (`SecurityConfig.java`)
- `csrf.disable()`
- `sessionCreationPolicy(STATELESS)`
- Public endpoints: `/register`, `/login`, `/hello/**`, Swagger
- Protected by role (`hasRole` / `hasAnyRole`)

---

## 6. Event-Driven Architecture (Current + Kafka Target)

### 6.1 Current Implementation
- `NotificationService` uses `ApplicationEventPublisher`
- `OrderPlacedEventListener` (`@EventListener`) logs to stdout
- **No actual external notification** (email/SMS/push) implemented

### 6.2 Kafka Integration Plan

**Topic Design**:
```
orders.placed     → OrderPlacedEvent (JSON: orderId, customerId, shopId, totalAmount, status)
orders.status      → OrderStatusUpdateEvent
inventory.updated  → ProductStockUpdateEvent
```

**Producer (Spring Kafka)**:
```java
@Service
public class KafkaNotificationService {
    @Autowired
    private KafkaTemplate<String, OrderEvent> kafkaTemplate;

    public void sendOrderPlaced(Order order) {
        kafkaTemplate.send("orders.placed", new OrderEvent(order));
    }
}
```

**Consumer (Notification / Vendor Service)**:
```java
@Component
public class OrderPlacedConsumer {
    @KafkaListener(topics = "orders.placed", groupId = "vendor-notify")
    public void consume(OrderEvent event) {
        // Send email / SMS / push to vendor
        // Lookup vendor by shopId
    }
}
```

---

## 7. AWS Deployment Architecture

### 7.1 Target Architecture

```
┌──────────────────────────────────────────────────────────────┐
│  AWS REGION (e.g., ap-south-1)                                │
│                                                              │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐   │
│  │  ALB         │───▶│  ECS Fargate │───▶│  RDS         │   │
│  │  (HTTPS)     │    │  (Spring)    │    │  PostgreSQL  │   │
│  └──────────────┘    └──────┬───────┘    └──────────────┘   │
│                            │                                 │
│                            ▼                                 │
│                     ┌──────────────┐                         │
│                     │  MSK / Kafka │                         │
│                     │  (Topics)    │                         │
│                     └──────┬───────┘                         │
│                            │                                 │
│                     ┌──────▼───────┐                         │
│                     │  Lambda /    │                         │
│                     │  Consumer    │                         │
│                     │  (Notify)    │                         │
│                     └──────────────┘                         │
│                                                              │
│  S3 Bucket (Static Assets / Product Images)                  │
│  CloudWatch (Logs / Metrics)                                 │
│  Secrets Manager (DB Password / JWT Secret)                  │
│  Parameter Store (App Config)                                │
└──────────────────────────────────────────────────────────────┘
```

### 7.2 AWS Services Used

| Service | Purpose | Configuration |
|---|---|---|
| **ECS Fargate** | Container orchestration for Spring Boot | Task Definition, Service, Auto Scaling |
| **ALB** | Load balancer + SSL termination | HTTPS listener → Target Group (port 8081) |
| **RDS PostgreSQL** | Managed database | Multi-AZ, encrypted at rest |
| **MSK (Managed Kafka)** | Event streaming | Topics: `orders.placed`, `orders.status` |
| **S3** | Product image storage | Public read / private write via pre-signed URLs |
| **Secrets Manager** | DB credentials, JWT secret | Referenced in `application.yaml` via Spring Cloud AWS |
| **CloudWatch** | Logs (`/aws/ecs/service`) + Metrics (CPU, Memory, Request Count) | Log Groups per service |
| **Parameter Store** | Non-secret config (`jwt.expiration`, `spring.datasource.url`) | `/wearminutes/service/*` |

### 7.3 Deployment Pipeline (CI/CD)

```
GitHub / GitLab
  │ Push to main
  ▼
AWS CodeBuild
  │ Build JAR (gradlew bootJar)
  │ Run Tests (gradlew test)
  ▼
Amazon ECR (Docker Image)
  │ Tag: wearminutes-service:latest
  ▼
AWS CodeDeploy / ECS Update Service
  │ Rolling deployment (Blue/Green optional)
  ▼
ECS Fargate Tasks (Healthy checks on /api/hello)
```

### 7.4 Dockerfile (Reference)

```dockerfile
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY build/libs/service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 7.5 Environment Variables (ECS Task Definition)

```yaml
SPRING_DATASOURCE_URL: jdbc:postgresql://<rds-endpoint>:5432/wearminutes_db
SPRING_DATASOURCE_USERNAME: !Ref DBUsername
SPRING_DATASOURCE_PASSWORD: !Ref DBPassword
JWT_SECRET: !Ref JwtSecret
KAFKA_BOOTSTRAP_SERVERS: <msk-broker-endpoint>:9092
AWS_REGION: ap-south-1
```

---

## 8. Kafka Integration Details

### 8.1 Producer Configuration (`application.yaml` addition)

```yaml
spring:
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
      properties:
        spring.json.type.mapping: orderEvent:com.wearminutes.service.event.OrderEvent
```

### 8.2 Topic Schema

**Topic**: `orders.placed`
- **Partitions**: 3 (scalable for high order volume)
- **Replication Factor**: 3 (MSK default)
- **Retention**: 7 days

**Message Schema (JSON)**:
```json
{
  "eventType": "ORDER_PLACED",
  "timestamp": "2026-10-31T08:00:00Z",
  "orderId": 123,
  "customerId": 456,
  "shopId": 789,
  "totalAmount": 2499.99,
  "orderStatus": "PLACED",
  "deliveryOtp": "4821"
}
```

### 8.3 Consumer Groups

| Group ID | Topic | Purpose |
|---|---|---|
| `vendor-notify` | `orders.placed` | Send vendor notification |
| `inventory-sync` | `orders.placed` | Update external inventory system |
| `analytics` | `orders.placed` | Stream to analytics pipeline |

---

## 9. API Reference (Key Endpoints)

### 9.1 Authentication

```http
POST /api/admin/users/login
POST /api/customer/users/login
POST /api/vendor/users/login
POST /api/delivery/users/login
```
Body: `{ "email": "...", "password": "..." }`
Response: `{ "token": "...", "role": "...", "userId": "..." }`

### 9.2 Admin

```http
GET    /api/admin/users
GET    /api/admin/users/{id}
PUT    /api/admin/users/{id}
DELETE /api/admin/users/{id}
PATCH  /api/admin/users/{id}/status?isActive=true
GET    /api/admin/orders
GET    /api/admin/orders/{id}
PUT    /api/admin/orders/{id}/status?status=ACCEPTED
POST   /api/admin/orders/checkout
DELETE /api/admin/orders/{id}
```

### 9.3 Customer

```http
POST   /api/customer/users/register
GET    /api/customer/users
GET    /api/customer/users/{id}
PUT    /api/customer/users/{id}
DELETE /api/customer/users/{id}
GET    /api/customer/products/shop/{shopId}
GET    /api/customer/products/{id}
```

### 9.4 Vendor

```http
POST   /api/vendor/users/register
GET    /api/vendor/users
GET    /api/vendor/users/{id}
PUT    /api/vendor/users/{id}
DELETE /api/vendor/users/{id}
POST   /api/vendor/shops
GET    /api/vendor/shops/vendor/{vendorId}
GET    /api/vendor/shops/{id}
PUT    /api/vendor/shops/{id}
DELETE /api/vendor/shops/{id}
POST   /api/vendor/products
GET    /api/vendor/products/shop/{shopId}
GET    /api/vendor/products/{id}
PUT    /api/vendor/products/{id}
DELETE /api/vendor/products/{id}
```

### 9.5 Delivery

```http
POST   /api/delivery/users/register
GET    /api/delivery/users
GET    /api/delivery/users/{id}
PUT    /api/delivery/users/{id}
DELETE /api/delivery/users/{id}
```

### 9.6 Orders

```http
POST /api/orders/checkout
Body: {
  "customerId": 1,
  "productId": 2,
  "quantity": 3,
  "paymentMode": "PREPAID",
  "deliveryAddress": "..."
}
```

---

## 10. Build & Run

### 10.1 Local Development

```bash
# Build
./gradlew build

# Run
./gradlew bootRun

# Test
./gradlew test

# Access
# API: http://localhost:8081
# Swagger: http://localhost:8081/swagger-ui.html
# OpenAPI JSON: http://localhost:8081/v3/api-docs
```

### 10.2 Gradle Dependencies (`build.gradle`)

- `spring-boot-starter-data-jpa`
- `spring-boot-starter-web`
- `spring-boot-starter-security`
- `jjwt-api` / `jjwt-impl` / `jjwt-jackson` (0.11.5)
- `springdoc-openapi-starter-webmvc-ui` (2.3.0)
- `postgresql` (runtime)
- `lombok`
- `spring-boot-starter-test`

---

## 11. Testing

```java
// ServiceApplicationTests.java
@SpringBootTest
class ServiceApplicationTests {
    @Test
    void contextLoads() {}
}
```

**Recommended Test Strategy**:
- Unit: Service layer (mock repositories)
- Integration: Controller + Repository (`@SpringBootTest` + `TestRestTemplate`)
- Security: JWT filter validation (`MockMvc` with `Bearer` token)
- Event: `ApplicationEventPublisher` assertions

---

## 12. Monitoring & Observability (AWS)

| Metric | Source | Alert Threshold |
|---|---|---|
| HTTP 5xx Rate | ALB / CloudWatch | > 1% |
| Response Time (p99) | CloudWatch Logs Insights | > 2s |
| DB Connection Pool | RDS Performance Insights | > 80% |
| Kafka Lag | MSK / CloudWatch | > 100 messages |
| ECS Memory | CloudWatch Container Insights | > 85% |

---

## 13. Security Best Practices (Implemented + Recommended)

### Implemented
- JWT HS256 with secret key
- BCrypt password encoding
- Role-based authorization (`hasRole` / `hasAnyRole`)
- Stateless session (`SessionCreationPolicy.STATELESS`)
- CSRF disabled (stateless API)

### Recommended (AWS Hardening)
- Rotate JWT secret via AWS Secrets Manager
- Enable RDS encryption at rest + in transit (TLS)
- Use AWS WAF on ALB (rate limiting, SQL injection rules)
- Enable VPC endpoints for RDS / MSK (no public internet)
- Use IAM Roles for ECS Tasks (no hardcoded AWS credentials)
- Enable CloudTrail for API audit logs

---

## 14. Known Issues & Technical Debt

1. **Duplicate `CheckoutRequest`**: Exists in `dto/request/` and as inner class in `OrderController`.
2. **Missing `CustomerController.java`**: Referenced but file absent.
3. **No delivery order endpoints**: Security config defines `/api/orders/*/status` and `/verify-otp` for `DELIVERY`, but controllers missing.
4. **String timestamps**: `User` and `Shop` use `String` instead of `LocalDateTime` (inconsistent with `Order`).
5. **No pagination**: All list endpoints return full dataset.
6. **Event listener only logs**: No real notification mechanism; Kafka integration is planned.
7. **Field injection**: `@Autowired` on fields (consider constructor injection for testability).

---

## 15. Roadmap

- [ ] Integrate Kafka Producer (`spring-kafka`) for `OrderPlacedEvent`
- [ ] Implement Kafka Consumer for vendor notifications
- [ ] Add pagination (`Pageable`) to all list endpoints
- [ ] Replace `String` timestamps with `LocalDateTime` in `User` / `Shop`
- [ ] Add delivery order status endpoints (`/api/orders/{id}/status`, `/verify-otp`)
- [ ] Implement email/SMS notification service (AWS SES / SNS)
- [ ] Add rate limiting (`Bucket4j` or AWS WAF)
- [ ] Implement caching (`Spring Cache` + Redis / ElastiCache) for product listings
- [ ] Add health checks (`/actuator/health`) for ECS
- [ ] Implement distributed tracing (`AWS X-Ray` or `Micrometer` + `Zipkin`)

---

## 16. References

- **Spring Boot Docs**: https://docs.spring.io/spring-boot/docs/3.2.2/reference/html/
- **Spring Security**: https://docs.spring.io/spring-security/reference/index.html
- **Spring Kafka**: https://docs.spring.io/spring-kafka/reference/html/
- **AWS ECS Fargate**: https://docs.aws.amazon.com/AmazonECS/latest/developerguide/AWS_Fargate.html
- **AWS MSK**: https://docs.aws.amazon.com/msk/latest/developerguide/what-is-msk.html
- **OpenAPI 3**: https://swagger.io/specification/

---

*Document generated from deep codebase analysis of `wearminutes-svc/service`.*
*Includes HLD, AWS deployment architecture, Kafka integration plan, and full API reference.*
