# 🎬 Cinema Management Microservice - Project Roadmap

## 📊 Project Status Overview

### ✅ Completed Services
- **User Service** - Authentication, JWT, User Management
- **Cinema Service** - Cinema, Movie, Screen, Showtime, Seat Management  
- **Eureka Server** - Service Discovery
- **API Gateway** - Routing & Load Balancing
- **Common Library** - Shared utilities, exceptions, security components

### 🚧 In Progress / Incomplete
- **Booking Service** - Partially implemented (needs payment & integration)
- **Notification Service** - Structure only (no implementation)
- **Inventory Service** - Structure only (no implementation)

---

## 🎯 Development Roadmap

### **PHASE 1: Complete Booking Service** 🎫
**Priority: HIGH** | **Estimated Time: 2-3 weeks**

#### 1.1 Cinema Service Integration
**Files to modify:**
- `booking-service/service/BookingService.java`
- `booking-service/client/CinemaClient.java`

**Tasks:**
- [ ] Validate showtime exists before creating booking
  ```java
  // Add method in CinemaClient
  public ShowtimeDTO getShowtime(Long showtimeId)
  ```
- [ ] Check seat availability via Cinema Service
  ```java
  // Add method in CinemaClient  
  public boolean checkSeatAvailability(Long showtimeId, List<String> seatNumbers)
  ```
- [ ] Reserve seats when booking is created
  ```java
  // Call Cinema Service API
  POST /api/showtimes/{id}/reserve-seats
  ```
- [ ] Release seats when booking is cancelled
  ```java
  // Call Cinema Service API
  POST /api/showtimes/{id}/release-seats
  ```

#### 1.2 Payment Integration
**Files to create:**
- `booking-service/entity/Payment.java`
- `booking-service/repository/PaymentRepository.java`
- `booking-service/service/PaymentService.java`
- `booking-service/controller/PaymentController.java`
- `booking-service/dto/PaymentRequest.java`
- `booking-service/dto/PaymentResponse.java`

**Tasks:**
- [ ] Create Payment entity with fields:
  - paymentId, bookingId, amount, paymentMethod
  - paymentStatus, transactionId, paymentDate
- [ ] Integrate payment gateway (choose one):
  - **VNPay** (Vietnam)
  - **MoMo** (Vietnam)
  - **Stripe** (International)
- [ ] Implement payment flow:
  ```
  1. Create booking (status: PENDING)
  2. Initiate payment
  3. Redirect to payment gateway
  4. Handle payment callback
  5. Update booking status (CONFIRMED/FAILED)
  ```
- [ ] Handle payment timeout (15 minutes)
- [ ] Implement refund logic for cancellations

#### 1.3 Booking Business Logic
**Files to modify:**
- `booking-service/service/BookingService.java`
- `booking-service/entity/Booking.java`

**Tasks:**
- [ ] Add validation rules:
  - Cannot book past showtimes
  - Cannot book more than 10 seats per booking
  - Showtime must be at least 30 minutes in future
- [ ] Calculate total price:
  ```java
  totalPrice = (basePrice * numberOfSeats) + seatTypePremium
  ```
- [ ] Handle concurrent bookings (optimistic locking):
  ```java
  @Version
  private Long version;
  ```
- [ ] Implement scheduled job to auto-cancel expired bookings:
  ```java
  @Scheduled(fixedRate = 60000) // Every minute
  public void cancelExpiredBookings()
  ```

#### 1.4 Booking APIs to Complete
**Endpoints to implement:**
- `POST /api/bookings/{id}/payment` - Initiate payment
- `GET /api/bookings/{id}/payment-status` - Check payment status
- `POST /api/bookings/{id}/confirm` - Confirm booking after payment
- `GET /api/bookings/user/{userId}` - Get user's booking history
- `POST /api/bookings/{id}/cancel` - Cancel booking with refund

---

### **PHASE 2: Implement Notification Service** 📧
**Priority: MEDIUM** | **Estimated Time: 1-2 weeks**

#### 2.1 Setup Message Queue
**Files to modify:**
- `docker-compose.yml`
- All service `pom.xml` files

**Tasks:**
- [ ] Add Kafka or RabbitMQ to docker-compose:
  ```yaml
  # Option 1: Kafka
  kafka:
    image: confluentinc/cp-kafka:latest
    ports:
      - "9092:9092"
  
  # Option 2: RabbitMQ
  rabbitmq:
    image: rabbitmq:3-management
    ports:
      - "5672:5672"
      - "15672:15672"
  ```
- [ ] Add Spring Kafka/AMQP dependencies:
  ```xml
  <!-- For Kafka -->
  <dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
  </dependency>
  
  <!-- For RabbitMQ -->
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-amqp</artifactId>
  </dependency>
  ```

#### 2.2 Event Publishing
**Files to create:**
- `common-lib/event/UserRegisteredEvent.java`
- `common-lib/event/BookingCreatedEvent.java`
- `common-lib/event/BookingConfirmedEvent.java`
- `common-lib/event/BookingCancelledEvent.java`
- `common-lib/event/PaymentCompletedEvent.java`

**User Service Events:**
- [ ] Publish `UserRegisteredEvent` after successful registration
  ```java
  kafkaTemplate.send("user-events", new UserRegisteredEvent(user));
  ```
- [ ] Publish `PasswordResetEvent` when user requests password reset

**Booking Service Events:**
- [ ] Publish `BookingCreatedEvent` when booking is created
- [ ] Publish `BookingConfirmedEvent` after successful payment
- [ ] Publish `BookingCancelledEvent` when booking is cancelled
- [ ] Publish `PaymentCompletedEvent` after payment processing

#### 2.3 Notification Service Implementation
**Files to create:**
- `notification-service/entity/Notification.java`
- `notification-service/repository/NotificationRepository.java`
- `notification-service/service/NotificationService.java`
- `notification-service/service/EmailService.java`
- `notification-service/service/SmsService.java` (optional)
- `notification-service/listener/UserEventListener.java`
- `notification-service/listener/BookingEventListener.java`
- `notification-service/controller/NotificationController.java`
- `notification-service/template/` (email templates)

**Tasks:**
- [ ] Create Notification entity:
  ```java
  - notificationId, userId, type (EMAIL/SMS/PUSH)
  - subject, content, status, sentAt, createdAt
  ```
- [ ] Configure JavaMailSender:
  ```yaml
  spring:
    mail:
      host: smtp.gmail.com
      port: 587
      username: ${MAIL_USERNAME}
      password: ${MAIL_PASSWORD}
  ```
- [ ] Implement email templates:
  - Welcome email (user registration)
  - Booking confirmation email
  - Booking cancellation email
  - Payment receipt email
- [ ] Listen to events and send notifications:
  ```java
  @KafkaListener(topics = "booking-events")
  public void handleBookingEvent(BookingConfirmedEvent event)
  ```
- [ ] Store notification history in database
- [ ] Implement retry mechanism for failed notifications

#### 2.4 Notification APIs
**Endpoints to implement:**
- `GET /api/notifications/user/{userId}` - Get user notifications
- `PUT /api/notifications/{id}/read` - Mark as read
- `GET /api/notifications/user/{userId}/unread-count` - Unread count
- `POST /api/notifications/resend/{id}` - Resend failed notification

---

### **PHASE 3: Implement Inventory Service** 📦
**Priority: LOW** | **Estimated Time: 1 week**

#### 3.1 Product Management
**Files to create:**
- `inventory-service/controller/ProductController.java`
- `inventory-service/service/ProductService.java`
- `inventory-service/repository/ProductRepository.java`
- `inventory-service/dto/ProductDTO.java`

**Tasks:**
- [ ] Implement CRUD operations for products:
  - Popcorn (Small, Medium, Large)
  - Drinks (Soft drinks, Water)
  - Combo deals
- [ ] Add product categories and pricing
- [ ] Manage stock levels per cinema location

#### 3.2 Booking Integration
**Files to modify:**
- `booking-service/entity/BookingProduct.java`
- `booking-service/service/BookingService.java`
- `inventory-service/listener/BookingEventListener.java`

**Tasks:**
- [ ] Add products to booking:
  ```java
  POST /api/bookings/{id}/products
  {
    "productId": 1,
    "quantity": 2
  }
  ```
- [ ] Deduct stock when booking is confirmed
- [ ] Restore stock when booking is cancelled
- [ ] Listen to booking events:
  ```java
  @KafkaListener(topics = "booking-events")
  public void handleBookingConfirmed(BookingConfirmedEvent event)
  ```

#### 3.3 Stock Management
**Files to create:**
- `inventory-service/service/StockService.java`
- `inventory-service/entity/StockMovement.java`
- `inventory-service/controller/StockController.java`

**Tasks:**
- [ ] Track stock movements (IN/OUT)
- [ ] Low stock alerts (< 10 items)
- [ ] Generate inventory reports
- [ ] Stock adjustment APIs

---

### **PHASE 4: Monitoring & Observability** 📊
**Priority: MEDIUM** | **Estimated Time: 1 week**

#### 4.1 Logging & Monitoring Stack
**Files to modify:**
- `docker-compose.yml`
- All service `logback-spring.xml` files

**Tasks:**
- [ ] Verify Grafana, Loki, Promtail are running
- [ ] Configure Prometheus metrics:
  ```xml
  <dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
  </dependency>
  ```
- [ ] Create Grafana dashboards:
  - Service health status
  - Request rate & latency
  - Error rates
  - Database connection pool
  - JVM metrics

#### 4.2 Distributed Tracing
**Files to modify:**
- All service `pom.xml` files
- `application.properties` files

**Tasks:**
- [ ] Add Micrometer Tracing:
  ```xml
  <dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-tracing-bridge-brave</artifactId>
  </dependency>
  ```
- [ ] Setup Zipkin or Jaeger:
  ```yaml
  zipkin:
    image: openzipkin/zipkin
    ports:
      - "9411:9411"
  ```
- [ ] Add correlation IDs to all logs
- [ ] Trace requests across services

#### 4.3 Resilience & Circuit Breaker
**Files to create:**
- `common-lib/config/ResilienceConfig.java`

**Tasks:**
- [ ] Add Resilience4j:
  ```xml
  <dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-spring-boot3</artifactId>
  </dependency>
  ```
- [ ] Configure circuit breaker for external calls
- [ ] Add retry mechanism with exponential backoff
- [ ] Implement fallback methods
- [ ] Add rate limiting

---

### **PHASE 5: Security Enhancements** 🔒
**Priority: HIGH** | **Estimated Time: 1 week**

#### 5.1 API Security
**Files to modify:**
- `gateway/config/SecurityConfig.java`
- `common-lib/security/RateLimitFilter.java` (create)

**Tasks:**
- [ ] Implement rate limiting:
  ```java
  @RateLimiter(name = "api", fallbackMethod = "rateLimitFallback")
  ```
- [ ] Configure CORS properly:
  ```java
  @Bean
  public CorsConfigurationSource corsConfigurationSource()
  ```
- [ ] Add API key authentication for service-to-service calls
- [ ] Implement request signing for sensitive operations
- [ ] Add IP whitelisting for admin endpoints

#### 5.2 Data Security
**Files to modify:**
- `booking-service/entity/Payment.java`
- `user-service/entity/User.java`

**Tasks:**
- [ ] Encrypt sensitive data at rest:
  ```java
  @Convert(converter = EncryptedStringConverter.class)
  private String cardNumber;
  ```
- [ ] Implement audit logging:
  ```java
  @EntityListeners(AuditingEntityListener.class)
  ```
- [ ] Add data masking for logs:
  ```java
  logger.info("Payment processed for card: ****{}", 
    cardNumber.substring(cardNumber.length() - 4));
  ```
- [ ] Implement GDPR compliance (data export/deletion)

---

### **PHASE 6: Testing** 🧪
**Priority: HIGH** | **Estimated Time: 2 weeks**

#### 6.1 Unit Tests
**Target Coverage: 80%+**

**Tasks:**
- [ ] Service layer tests with Mockito:
  ```java
  @ExtendWith(MockitoExtension.class)
  class BookingServiceTest {
    @Mock private BookingRepository repository;
    @InjectMocks private BookingService service;
  }
  ```
- [ ] Repository tests with @DataJpaTest
- [ ] Controller tests with @WebMvcTest
- [ ] Validation tests for DTOs

#### 6.2 Integration Tests
**Files to create:**
- `*/src/test/java/**/integration/`

**Tasks:**
- [ ] API integration tests with RestAssured:
  ```java
  @SpringBootTest(webEnvironment = RANDOM_PORT)
  @AutoConfigureTestDatabase
  class BookingIntegrationTest
  ```
- [ ] Database integration tests with Testcontainers:
  ```java
  @Testcontainers
  @Container
  static MySQLContainer mysql = new MySQLContainer("mysql:8.0")
  ```
- [ ] Message queue integration tests
- [ ] End-to-end workflow tests

#### 6.3 Contract Tests
**Files to use:**
- `*/src/test/resources/contracts/*.groovy`

**Tasks:**
- [ ] Define contracts for each service API
- [ ] Generate tests from contracts
- [ ] Verify provider-consumer compatibility
- [ ] Add contract tests to CI/CD pipeline

#### 6.4 Performance Tests
**Tools:** JMeter or Gatling

**Tasks:**
- [ ] Load testing (1000 concurrent users)
- [ ] Stress testing (find breaking point)
- [ ] Spike testing (sudden traffic increase)
- [ ] Endurance testing (sustained load)

---

### **PHASE 7: Documentation** 📚
**Priority: MEDIUM** | **Estimated Time: 3-4 days**

#### 7.1 API Documentation
**Files to create:**
- `*/API-DOCUMENTATION.md`
- Swagger/OpenAPI specs

**Tasks:**
- [ ] Add Swagger/OpenAPI to all services:
  ```xml
  <dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
  </dependency>
  ```
- [ ] Document all endpoints with examples
- [ ] Add request/response schemas
- [ ] Include authentication requirements
- [ ] Provide cURL examples

#### 7.2 Architecture Documentation
**Files to create:**
- `ARCHITECTURE.md`
- `DEPLOYMENT.md`
- `DEVELOPMENT-GUIDE.md`

**Tasks:**
- [ ] Create system architecture diagram
- [ ] Document service dependencies
- [ ] Explain data flow between services
- [ ] Document event-driven architecture
- [ ] Add sequence diagrams for key flows

#### 7.3 Setup & Deployment Guide
**Files to update:**
- `README.md`

**Tasks:**
- [ ] Prerequisites and requirements
- [ ] Local development setup
- [ ] Docker deployment instructions
- [ ] Kubernetes deployment (if applicable)
- [ ] Environment variables documentation
- [ ] Troubleshooting guide

---

### **PHASE 8: Performance Optimization** ⚡
**Priority: LOW** | **Estimated Time: 1 week**

#### 8.1 Caching Strategy
**Files to create:**
- `common-lib/config/CacheConfig.java`

**Tasks:**
- [ ] Add Redis to docker-compose:
  ```yaml
  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
  ```
- [ ] Implement caching for:
  - Movie listings (TTL: 1 hour)
  - Cinema information (TTL: 24 hours)
  - Showtime schedules (TTL: 30 minutes)
- [ ] Add cache invalidation on updates
- [ ] Implement distributed caching

#### 8.2 Database Optimization
**Files to modify:**
- Entity classes
- Repository classes

**Tasks:**
- [ ] Add database indexes:
  ```java
  @Table(indexes = {
    @Index(name = "idx_user_email", columnList = "email"),
    @Index(name = "idx_booking_user", columnList = "user_id")
  })
  ```
- [ ] Optimize N+1 queries with @EntityGraph
- [ ] Implement pagination for large datasets
- [ ] Add database connection pooling (HikariCP)
- [ ] Enable query logging and analysis

#### 8.3 API Optimization
**Tasks:**
- [ ] Implement response compression (GZIP)
- [ ] Add ETag support for caching
- [ ] Optimize JSON serialization
- [ ] Implement GraphQL (optional)
- [ ] Add API versioning

---

## 📅 Recommended Timeline

| Phase | Duration | Priority |
|-------|----------|----------|
| Phase 1: Booking Service | 2-3 weeks | HIGH |
| Phase 5: Security | 1 week | HIGH |
| Phase 6: Testing | 2 weeks | HIGH |
| Phase 2: Notification | 1-2 weeks | MEDIUM |
| Phase 4: Monitoring | 1 week | MEDIUM |
| Phase 7: Documentation | 3-4 days | MEDIUM |
| Phase 3: Inventory | 1 week | LOW |
| Phase 8: Optimization | 1 week | LOW |

**Total Estimated Time: 8-10 weeks**

---

## 🎯 Success Criteria

### Functional Requirements
- ✅ Users can register, login, and manage profiles
- ✅ Users can browse movies and showtimes
- ✅ Users can book tickets and select seats
- ✅ Users can make payments securely
- ✅ Users receive booking confirmations via email
- ✅ Users can view booking history
- ✅ Users can cancel bookings with refunds
- ✅ Admin can manage movies, cinemas, and showtimes

### Non-Functional Requirements
- ✅ System handles 1000+ concurrent users
- ✅ API response time < 500ms (95th percentile)
- ✅ 99.9% uptime
- ✅ Zero data loss
- ✅ Secure payment processing (PCI DSS compliant)
- ✅ 80%+ test coverage
- ✅ Complete API documentation

---

## 🚀 Getting Started

### Current Status
You have completed User Service and Cinema Service. 

### Next Steps
1. **Start with Phase 1** - Complete Booking Service
2. Focus on payment integration first
3. Then implement seat reservation logic
4. Test the complete booking flow

### Quick Start Command
```bash
# Build all services
cd common-lib && mvn clean install
cd ../user-service && mvn clean install -DskipTests
cd ../cinema-service/cinema-service && mvn clean install -DskipTests
cd ../../booking-service/booking-service && mvn clean install -DskipTests

# Start infrastructure
docker compose up -d

# Start services
./start-all-services.ps1
```

---

## 📞 Support & Resources

### Documentation
- Spring Boot: https://spring.io/projects/spring-boot
- Spring Cloud: https://spring.io/projects/spring-cloud
- Kafka: https://kafka.apache.org/documentation/
- Docker: https://docs.docker.com/

### Tools
- Postman Collection: `postman/cinema-management.json`
- Database Schema: `docs/database-schema.sql`
- Architecture Diagrams: `docs/architecture/`

---

**Last Updated:** April 14, 2026  
**Version:** 1.0  
**Maintainer:** Development Team
