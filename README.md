# Cinema Management Microservice

Hệ thống quản lý rạp chiếu phim xây dựng theo kiến trúc microservice với Spring Boot, Eureka, API Gateway, MySQL và RabbitMQ.

## 1. Thành Phần

| Service | Port | Mô tả |
|---|---:|---|
| Eureka Server | 8761 | Service discovery |
| API Gateway | 8888 | Entry point cho toàn bộ API |
| User Service | 8081 | Auth, user, address, payment methods |
| Notification Service | 8082 | Email notification, template, retry |
| Inventory Service | 8083 | Products, inventory, stock movements |
| Cinema Service | 8084 | Cinemas, movies, screens, seats, showtimes |
| Booking Service | 8085 | Booking, payment, events, circuit breaker |

Database ports:

| Database | Port |
|---|---:|
| cinemadb | 3308 |
| inventorydb | 3309 |
| bookingdb | 3310 |
| userdb | 3311 |
| notificationdb | 3312 |

## 2. Yêu Cầu

- Java 17 hoặc mới hơn
- Maven
- Docker Desktop
- PowerShell hoặc terminal tương đương

## 3. Build Common Library

Chạy trước vì các service phụ thuộc vào `common-lib`.

```bash
cd common-lib
mvn clean install
```

Quay lại thư mục root:

```bash
cd ..
```

## 4. Start Infrastructure

Từ thư mục root:

```bash
docker compose up -d
```

Kiểm tra các service hạ tầng:

- RabbitMQ Management: http://localhost:15672
- Grafana: http://localhost:3000
- Loki: http://localhost:3100

RabbitMQ mặc định:

```text
username: guest
password: guest
```

## 5. Chạy Các Service

Nên chạy theo thứ tự sau.

### Cách 1: Chạy bằng script

```powershell
.\start-all-services.ps1
```

Dừng toàn bộ:

```powershell
.\stop-all-services.ps1
```

### Cách 2: Chạy thủ công từng service

Mở nhiều terminal, mỗi terminal chạy một service.

```bash
cd eureka-server
mvn spring-boot:run
```

```bash
cd gateway
mvn spring-boot:run
```

```bash
cd user-service
mvn spring-boot:run
```

```bash
cd notification-service
mvn spring-boot:run
```

```bash
cd inventory-service
mvn spring-boot:run
```

```bash
cd cinema-service
mvn spring-boot:run
```

```bash
cd booking-service
mvn spring-boot:run
```

## 6. Verify

Eureka Dashboard:

```text
http://localhost:8761
```

Gateway:

```text
http://localhost:8888
```

Tất cả API nên gọi qua Gateway:

```text
http://localhost:8888/api/...
```

## 9. Setup Email Cho Notification Service

File cấu hình:

```text
notification-service/src/main/resources/application.properties
```

Ví dụ Gmail SMTP:

```properties
notification.email.enabled=true
notification.email.from=your-email@gmail.com

spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

Với Gmail, `spring.mail.password` là App Password, không phải mật khẩu Gmail thường.

## 10. Chạy Test

Chạy test từng module:

```bash
cd user-service
mvn test
```

```bash
cd cinema-service
mvn test
```

```bash
cd booking-service
mvn test
```

```bash
cd inventory-service
mvn test
```

```bash
cd notification-service
mvn test
```

```bash
cd gateway
mvn test
```

```bash
cd eureka-server
mvn test
```


## 11. Ghi Chú

- Cần chạy `common-lib` trước bằng `mvn clean install`.
- Nên start Eureka trước các service khác.
- Nên gọi API qua Gateway port `8888`.
- Payment hiện là payment API cơ bản, chưa tích hợp thật VNPay/MoMo/Stripe.
- Notification hỗ trợ SMTP thật nếu cấu hình email đúng.
- Booking Service có circuit breaker/fallback khi gọi Cinema Service.
