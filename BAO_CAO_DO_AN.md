# BÁO CÁO ĐỒ ÁN
# HỆ THỐNG QUẢN LÝ RẠP CHIẾU PHIM THEO KIẾN TRÚC MICROSERVICE

---

## 1. Tổng Quan

### 1.1. Mục Tiêu

Dự án xây dựng hệ thống quản lý rạp chiếu phim theo kiến trúc microservice. Hệ thống hỗ trợ các nghiệp vụ chính:

- Quản lý tài khoản người dùng.
- Quản lý phim, rạp, phòng chiếu, ghế và suất chiếu.
- Đặt vé, chọn ghế, thêm sản phẩm kèm theo.
- Thanh toán cơ bản và cập nhật trạng thái thanh toán.
- Quản lý sản phẩm, tồn kho, xuất nhập kho.
- Gửi thông báo email theo sự kiện booking.
- Định tuyến tập trung qua API Gateway.
- Service Discovery bằng Eureka.
- Event-driven communication bằng RabbitMQ.
- Circuit breaker/fallback cho lời gọi liên service.
- Contract test cho các API hiện tại.

### 1.2. Kiến Trúc Tổng Thể

```text
Client/Postman/Web
       |
       v
API Gateway (8888)
       |
       +--> User Service (8081)          - userdb:3311
       +--> Notification Service (8082)  - notificationdb:3312
       +--> Inventory Service (8083)     - inventorydb:3309
       +--> Cinema Service (8084)        - cinemadb:3308
       +--> Booking Service (8085)       - bookingdb:3310

Eureka Server (8761)
RabbitMQ (5672, management 15672)
Grafana/Loki monitoring cơ bản
```

### 1.3. Công Nghệ Sử Dụng

| Thành phần | Công nghệ |
|---|---|
| Backend | Spring Boot 4.0.3 |
| Service Discovery | Spring Cloud Netflix Eureka |
| Gateway | Spring Cloud Gateway Server WebMVC |
| Database | MySQL |
| ORM | Spring Data JPA / Hibernate |
| Message Broker | RabbitMQ |
| Authentication | JWT |
| Authorization | Spring Security, RBAC |
| Circuit Breaker | Spring Cloud Circuit Breaker + Resilience4j |
| Email | Spring Mail / JavaMailSender |
| API Docs | SpringDoc OpenAPI |
| Testing | JUnit 5, Mockito, Spring MockMvc |
| Logging/Monitoring | Logback, Loki, Grafana |
| Build Tool | Maven |
| Java | Java 17+ |

---

## 2. Các Service Hiện Tại

### 2.1. Eureka Server

| Thuộc tính | Giá trị |
|---|---|
| Service name | `eureka-server` |
| Port | `8761` |
| Chức năng | Service registry và discovery |

Eureka Server quản lý danh sách service instance, hỗ trợ các service đăng ký và gateway/service khác tra cứu endpoint qua service name.

### 2.2. API Gateway

| Thuộc tính | Giá trị |
|---|---|
| Service name | `gateway` |
| Port | `8888` |
| Chức năng | Single entry point, route request tới service tương ứng |

Routing hiện tại:

| Path | Service |
|---|---|
| `/api/auth/**` | `user-service` |
| `/api/users/**` | `user-service` |
| `/api/cinemas/**` | `cinema-service` |
| `/api/movies/**` | `cinema-service` |
| `/api/screens/**` | `cinema-service` |
| `/api/showtimes/**` | `cinema-service` |
| `/api/bookings/**` | `booking-service` |
| `/api/payments/**` | `booking-service` |
| `/api/products/**` | `inventory-service` |
| `/api/inventory/**` | `inventory-service` |
| `/api/notifications/**` | `notification-service` |

### 2.3. User Service

| Thuộc tính | Giá trị |
|---|---|
| Service name | `user-service` |
| Port | `8081` |
| Database | `userdb` trên MySQL port `3311` |

Chức năng:

- Đăng ký, đăng nhập JWT.
- Xem và cập nhật thông tin cá nhân.
- Đổi mật khẩu.
- Deactivate tài khoản cá nhân.
- Admin xem danh sách user, xem chi tiết user.
- Admin block/unblock tài khoản.
- Quản lý địa chỉ người dùng.
- Quản lý phương thức thanh toán của người dùng.
- Validate token.

### 2.4. Notification Service

| Thuộc tính | Giá trị |
|---|---|
| Service name | `notification-service` |
| Port | `8082` |
| Database | `notificationdb` trên MySQL port `3312` |

Chức năng:

- Lắng nghe booking events từ RabbitMQ.
- Gửi email khi booking created/confirmed/cancelled.
- Gửi email thật qua SMTP bằng `JavaMailSender`.
- Có cơ chế simulate/log email nếu cấu hình SMTP chưa đúng.
- Render email template với biến động dạng `{{bookingReference}}`, `{{movieTitle}}`, `{{totalAmount}}`, ...
- Lưu lịch sử notification.
- Quản lý notification preference theo user.
- Retry notification thất bại bằng scheduled job.
- Admin xem notification failed và resend notification.
- Admin test gửi email.
- Admin CRUD email template.

### 2.5. Inventory Service

| Thuộc tính | Giá trị |
|---|---|
| Service name | `inventory-service` |
| Port | `8083` |
| Database | `inventorydb` trên MySQL port `3309` |

Chức năng:

- CRUD sản phẩm: food, beverage, combo.
- Xem sản phẩm available theo rạp.
- Quản lý tồn kho theo rạp.
- Restock, adjust tồn kho.
- Xem low-stock inventory.
- Xem lịch sử stock movements.
- Lắng nghe booking confirmed/cancelled events để trừ/hoàn tồn kho.

### 2.6. Cinema Service

| Thuộc tính | Giá trị |
|---|---|
| Service name | `cinema-service` |
| Port | `8084` |
| Database | `cinemadb` trên MySQL port `3308` |

Chức năng:

- CRUD rạp chiếu.
- CRUD phim.
- Xem phim đang chiếu và sắp chiếu.
- CRUD phòng chiếu.
- Tạo ghế tự động cho phòng chiếu.
- Xem ghế theo phòng chiếu.
- Xem chi tiết ghế theo `seatId`.
- CRUD suất chiếu.
- Tìm suất chiếu theo phim, rạp, ngày, khoảng ngày.
- Reserve/release số lượng ghế cho suất chiếu.

### 2.7. Booking Service

| Thuộc tính | Giá trị |
|---|---|
| Service name | `booking-service` |
| Port | `8085` |
| Database | `bookingdb` trên MySQL port `3310` |

Chức năng:

- Tạo booking.
- Lưu danh sách ghế đã chọn vào `booking_seats`.
- Lưu sản phẩm kèm booking vào `booking_products`.
- Xem booking cá nhân.
- Admin xem toàn bộ booking.
- Xem chi tiết booking.
- Confirm booking sau payment.
- Cancel booking và release ghế.
- Publish booking events qua RabbitMQ.
- Payment API cơ bản: tạo payment, callback, refund.
- Circuit breaker/fallback khi gọi sang `cinema-service`.

Circuit breaker hiện áp dụng cho các lời gọi:

- Lấy available seats.
- Lấy thông tin ghế theo `seatId`.
- Reserve ghế.
- Release ghế.

Config chính:

```properties
resilience4j.circuitbreaker.instances.cinemaService.sliding-window-size=10
resilience4j.circuitbreaker.instances.cinemaService.minimum-number-of-calls=5
resilience4j.circuitbreaker.instances.cinemaService.failure-rate-threshold=50
resilience4j.circuitbreaker.instances.cinemaService.wait-duration-in-open-state=10s
resilience4j.circuitbreaker.instances.cinemaService.permitted-number-of-calls-in-half-open-state=3
resilience4j.timelimiter.instances.cinemaService.timeout-duration=3s
```

---

## 3. Database Schema

### 3.1. User Service

| Bảng | Mục đích |
|---|---|
| `users` | Thông tin tài khoản |
| `user_addresses` | Địa chỉ người dùng |
| `user_payment_methods` | Phương thức thanh toán người dùng |

### 3.2. Cinema Service

| Bảng | Mục đích |
|---|---|
| `cinemas` | Rạp chiếu |
| `movies` | Phim |
| `screens` | Phòng chiếu |
| `seats` | Ghế |
| `showtimes` | Suất chiếu |

### 3.3. Booking Service

| Bảng | Mục đích |
|---|---|
| `bookings` | Đơn đặt vé |
| `booking_seats` | Ghế trong booking |
| `booking_products` | Sản phẩm kèm booking |
| `payments` | Thanh toán |

### 3.4. Inventory Service

| Bảng | Mục đích |
|---|---|
| `products` | Sản phẩm |
| `cinema_inventory` | Tồn kho theo rạp và sản phẩm |
| `stock_movements` | Lịch sử xuất nhập/điều chỉnh tồn kho |
| `booking_products` | Sản phẩm đã trừ tồn theo booking |

### 3.5. Notification Service

| Bảng | Mục đích |
|---|---|
| `notifications` | Lịch sử thông báo |
| `email_templates` | Template email |
| `notification_preferences` | Cấu hình nhận thông báo theo user |

---

## 4. REST API Hiện Tại

### 4.1. User APIs

#### Authentication

| Method | Endpoint | Mô tả |
|---|---|---|
| `POST` | `/api/auth/register` | Đăng ký tài khoản |
| `POST` | `/api/auth/login` | Đăng nhập, nhận JWT |

#### User Management

| Method | Endpoint | Mô tả |
|---|---|---|
| `GET` | `/api/users/me` | Lấy thông tin user hiện tại |
| `PUT` | `/api/users/me` | Cập nhật profile |
| `PUT` | `/api/users/me/password` | Đổi mật khẩu |
| `DELETE` | `/api/users/me` | Deactivate tài khoản |
| `GET` | `/api/users/{id}` | Admin lấy user theo id |
| `GET` | `/api/users` | Admin lấy danh sách user |
| `PUT` | `/api/users/{id}/block` | Admin block user |
| `PUT` | `/api/users/{id}/unblock` | Admin unblock user |
| `POST` | `/api/users/validate` | Validate JWT token |

#### User Address

| Method | Endpoint | Mô tả |
|---|---|---|
| `GET` | `/api/users/me/addresses` | Lấy địa chỉ của user |
| `POST` | `/api/users/me/addresses` | Thêm địa chỉ |
| `PUT` | `/api/users/me/addresses/{addressId}` | Cập nhật địa chỉ |
| `DELETE` | `/api/users/me/addresses/{addressId}` | Xóa địa chỉ |

#### User Payment Method

| Method | Endpoint | Mô tả |
|---|---|---|
| `GET` | `/api/users/me/payment-methods` | Lấy phương thức thanh toán |
| `POST` | `/api/users/me/payment-methods` | Thêm phương thức thanh toán |
| `PUT` | `/api/users/me/payment-methods/{paymentMethodId}` | Cập nhật phương thức thanh toán |
| `DELETE` | `/api/users/me/payment-methods/{paymentMethodId}` | Xóa phương thức thanh toán |

### 4.2. Cinema APIs

#### Cinema

| Method | Endpoint | Mô tả |
|---|---|---|
| `GET` | `/api/cinemas` | Lấy danh sách rạp |
| `GET` | `/api/cinemas/{id}` | Lấy chi tiết rạp |
| `POST` | `/api/cinemas` | Tạo rạp |
| `PUT` | `/api/cinemas/{id}` | Cập nhật rạp |
| `DELETE` | `/api/cinemas/{id}` | Xóa rạp |
| `GET` | `/api/cinemas/{id}/screens` | Lấy phòng chiếu của rạp |

#### Movie

| Method | Endpoint | Mô tả |
|---|---|---|
| `GET` | `/api/movies` | Lấy danh sách phim |
| `GET` | `/api/movies/{id}` | Lấy chi tiết phim |
| `GET` | `/api/movies/now-showing` | Lấy phim đang chiếu |
| `GET` | `/api/movies/coming-soon` | Lấy phim sắp chiếu |
| `POST` | `/api/movies` | Tạo phim |
| `PUT` | `/api/movies/{id}` | Cập nhật phim |
| `DELETE` | `/api/movies/{id}` | Xóa phim |

#### Screen & Seat

| Method | Endpoint | Mô tả |
|---|---|---|
| `GET` | `/api/screens/{id}` | Lấy chi tiết phòng chiếu |
| `POST` | `/api/screens` | Tạo phòng chiếu |
| `PUT` | `/api/screens/{id}` | Cập nhật phòng chiếu |
| `DELETE` | `/api/screens/{id}` | Xóa phòng chiếu |
| `GET` | `/api/screens/{id}/seats` | Lấy danh sách ghế của phòng |
| `GET` | `/api/screens/seats/{seatId}` | Lấy chi tiết ghế |
| `POST` | `/api/screens/{id}/generate-seats` | Tạo ghế tự động |

#### Showtime

| Method | Endpoint | Mô tả |
|---|---|---|
| `GET` | `/api/showtimes` | Lấy danh sách suất chiếu |
| `GET` | `/api/showtimes/{id}` | Lấy chi tiết suất chiếu |
| `GET` | `/api/showtimes/movie/{movieId}?date=yyyy-MM-dd` | Lấy suất chiếu theo phim/ngày |
| `GET` | `/api/showtimes/movie/{movieId}/cinema/{cinemaId}?date=yyyy-MM-dd` | Lấy suất chiếu theo phim/rạp/ngày |
| `GET` | `/api/showtimes/date-range?startDate=yyyy-MM-dd&endDate=yyyy-MM-dd` | Lấy suất chiếu theo khoảng ngày |
| `POST` | `/api/showtimes` | Tạo suất chiếu |
| `PUT` | `/api/showtimes/{id}` | Cập nhật suất chiếu |
| `DELETE` | `/api/showtimes/{id}` | Xóa suất chiếu |
| `POST` | `/api/showtimes/{id}/reserve?seats=n` | Reserve số lượng ghế |
| `POST` | `/api/showtimes/{id}/release?seats=n` | Release số lượng ghế |

### 4.3. Booking APIs

| Method | Endpoint | Mô tả |
|---|---|---|
| `GET` | `/api/bookings` | Admin lấy toàn bộ booking |
| `GET` | `/api/bookings/my-bookings` | User lấy booking của mình |
| `POST` | `/api/bookings` | Tạo booking |
| `GET` | `/api/bookings/{id}` | Lấy chi tiết booking |
| `PUT` | `/api/bookings/{id}/confirm` | Confirm booking |
| `PUT` | `/api/bookings/{id}/cancel?reason=...` | Cancel booking |

Response booking hiện trả kèm:

- Thông tin booking chính.
- Danh sách `seats`.
- Danh sách `products`.

Ví dụ request tạo booking:

```json
{
  "showtimeId": 1,
  "cinemaId": 1,
  "movieId": 1,
  "seatIds": [1, 2],
  "totalAmount": 180000,
  "productItems": [
    {
      "productId": 5,
      "quantity": 1,
      "unitPrice": 50000
    }
  ]
}
```

### 4.4. Payment APIs

| Method | Endpoint | Mô tả |
|---|---|---|
| `POST` | `/api/payments` | Tạo payment |
| `GET` | `/api/payments/{id}` | Lấy payment theo id |
| `GET` | `/api/payments/reference/{paymentReference}` | Lấy payment theo reference |
| `GET` | `/api/payments/booking/{bookingId}` | Lấy payment theo booking |
| `POST` | `/api/payments/callback` | Callback cập nhật trạng thái payment |
| `POST` | `/api/payments/{id}/refund` | Admin refund payment |

Ghi chú: Payment hiện là xử lý cơ bản trong hệ thống, chưa tích hợp thật với VNPay/MoMo/Stripe.

### 4.5. Product APIs

| Method | Endpoint | Mô tả |
|---|---|---|
| `GET` | `/api/products` | Lấy danh sách sản phẩm |
| `GET` | `/api/products/{id}` | Lấy chi tiết sản phẩm |
| `GET` | `/api/products/cinema/{cinemaId}` | Lấy sản phẩm available tại rạp |
| `POST` | `/api/products` | Admin tạo sản phẩm |
| `PUT` | `/api/products/{id}` | Admin cập nhật sản phẩm |
| `DELETE` | `/api/products/{id}` | Admin xóa sản phẩm |

### 4.6. Inventory APIs

| Method | Endpoint | Mô tả |
|---|---|---|
| `GET` | `/api/inventory` | Admin lấy toàn bộ inventory |
| `GET` | `/api/inventory/cinema/{cinemaId}` | Admin lấy inventory theo rạp |
| `GET` | `/api/inventory/low-stock` | Admin lấy danh sách tồn kho thấp |
| `POST` | `/api/inventory` | Admin tạo inventory |
| `PUT` | `/api/inventory` | Admin cập nhật inventory |
| `POST` | `/api/inventory/adjust` | Admin điều chỉnh tồn kho |
| `POST` | `/api/inventory/restock` | Admin nhập thêm hàng |
| `GET` | `/api/inventory/movements` | Admin xem toàn bộ stock movement |
| `GET` | `/api/inventory/{inventoryId}/movements` | Admin xem movement theo inventory |

### 4.7. Notification APIs

| Method | Endpoint | Mô tả |
|---|---|---|
| `GET` | `/api/notifications` | Admin lấy toàn bộ notification |
| `GET` | `/api/notifications/{id}` | Lấy notification theo id |
| `GET` | `/api/notifications/failed` | Admin lấy notification gửi lỗi |
| `POST` | `/api/notifications/{id}/resend` | Admin gửi lại notification |
| `POST` | `/api/notifications/test-email` | Admin test gửi email |
| `GET` | `/api/notifications/users/{userId}` | Lấy notification theo user |
| `GET` | `/api/notifications/preferences/users/{userId}` | Lấy preference theo user |
| `PUT` | `/api/notifications/preferences/users/{userId}` | Cập nhật preference |
| `GET` | `/api/notifications/templates` | Admin lấy danh sách template |
| `POST` | `/api/notifications/templates` | Admin tạo template |
| `PUT` | `/api/notifications/templates/{id}` | Admin cập nhật template |
| `DELETE` | `/api/notifications/templates/{id}` | Admin xóa template |

---

## 5. Event-Driven Architecture

### 5.1. RabbitMQ

Booking Service publish events qua RabbitMQ. Notification Service và Inventory Service consume events.

Các loại event chính:

| Event | Routing key | Mục đích |
|---|---|---|
| Booking created | `booking.created` | Gửi email tạo booking |
| Booking confirmed | `booking.confirmed` | Gửi email xác nhận, trừ tồn kho |
| Booking cancelled | `booking.cancelled` | Gửi email hủy, hoàn tồn kho |

### 5.2. Luồng Tạo Booking

```text
Client
  -> Gateway
  -> Booking Service
  -> Cinema Service: lấy thông tin ghế, reserve số lượng ghế
  -> Booking DB: lưu booking, booking_seats, booking_products
  -> RabbitMQ: publish booking.created
  -> Notification Service: gửi email tạo booking
```

### 5.3. Luồng Confirm Booking

```text
Client/Payment callback
  -> Booking Service
  -> Cập nhật booking CONFIRMED, payment PAID
  -> RabbitMQ: publish booking.confirmed
  -> Notification Service: gửi email xác nhận
  -> Inventory Service: trừ tồn kho sản phẩm
```

### 5.4. Luồng Cancel Booking

```text
Client/Admin
  -> Booking Service
  -> Cinema Service: release ghế
  -> Cập nhật booking CANCELLED
  -> RabbitMQ: publish booking.cancelled
  -> Notification Service: gửi email hủy
  -> Inventory Service: hoàn tồn kho sản phẩm
```

---

## 6. Bảo Mật

### 6.1. Authentication

- User đăng nhập qua `/api/auth/login`.
- User nhận JWT.
- Các API cần bảo vệ đọc JWT từ header:

```http
Authorization: Bearer <JWT_TOKEN>
```

### 6.2. Authorization

Hệ thống dùng role:

- `USER`
- `ADMIN`

Các API public:

- Auth.
- Xem phim, rạp, suất chiếu.
- Xem sản phẩm.
- Một số endpoint đọc screen/seat.

Các API admin:

- Quản lý user.
- CRUD phim/rạp/phòng chiếu/suất chiếu.
- CRUD sản phẩm.
- Quản lý inventory.
- Quản lý notification template.
- Refund payment.

### 6.3. Circuit Breaker

`booking-service` dùng circuit breaker khi gọi `cinema-service`. Nếu `cinema-service` lỗi/chậm nhiều lần, circuit breaker tạm ngắt lời gọi và trả fallback nhanh để tránh kéo sập booking-service.

---

## 7. Notification Email Setup

Notification Service hỗ trợ gửi email thật qua SMTP.

Cấu hình trong `notification-service/src/main/resources/application.properties`:

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

Với Gmail, `spring.mail.password` là App Password, không phải mật khẩu đăng nhập Gmail thông thường.

Test email:

```http
POST /api/notifications/test-email
Authorization: Bearer <ADMIN_TOKEN>
Content-Type: application/json

{
  "recipient": "target@example.com",
  "subject": "Test Cinema Email",
  "message": "Email service is working."
}
```

---

## 8. Kiểm Thử

### 8.1. Contract Tests Đã Có

| Service | Test file |
|---|---|
| User Service | `UserApiContractTest.java` |
| Cinema Service | `CinemaApiContractTest.java` |
| Booking Service | `BookingApiContractTest.java` |
| Inventory Service | `InventoryApiContractTest.java` |
| Notification Service | `NotificationApiContractTest.java` |
| Gateway | `GatewayRouteContractTest.java` |

### 8.2. Kết Quả Test

Đã chạy `mvn -q test` cho các module:

| Module | Kết quả |
|---|---|
| `user-service` | Pass |
| `cinema-service` | Pass |
| `booking-service` | Pass |
| `inventory-service` | Pass |
| `notification-service` | Pass |
| `gateway` | Pass |
| `eureka-server` | Pass |

Tổng kết:

- 21 tests pass.
- 0 failures.
- 0 errors.

Các test đã kiểm tra:

- Contract HTTP method/path/status/body.
- Auth/User/address/payment-method APIs.
- Cinema/movie/screen/seat/showtime APIs.
- Booking/payment APIs.
- Product/inventory/stock movement APIs.
- Notification/template/preference/test-email/resend APIs.
- Gateway route cho toàn bộ API prefix.

Ghi chú: Đây là controller/API contract test và context-load test. Các phần tích hợp thật như SMTP thật, RabbitMQ end-to-end, payment gateway thật cần kiểm thử riêng khi chạy đầy đủ infrastructure.

---

## 9. Cách Chạy Hệ Thống

### 9.1. Prerequisites

- Java 17+
- Maven
- Docker Desktop
- MySQL/RabbitMQ qua Docker Compose

### 9.2. Build Common Library

```bash
cd common-lib
mvn clean install
```

### 9.3. Start Infrastructure

```bash
docker compose up -d
```

Các thành phần cần có:

- MySQL ports: `3308`, `3309`, `3310`, `3311`, `3312`
- RabbitMQ: `localhost:5672`
- RabbitMQ Management: `http://localhost:15672`
- Grafana: `http://localhost:3000`
- Loki: `http://localhost:3100`

### 9.4. Start Services

Chạy theo thứ tự:

```bash
cd eureka-server
mvn spring-boot:run

cd gateway
mvn spring-boot:run

cd user-service
mvn spring-boot:run

cd notification-service
mvn spring-boot:run

cd inventory-service
mvn spring-boot:run

cd cinema-service
mvn spring-boot:run

cd booking-service
mvn spring-boot:run
```

Hoặc dùng script:

```powershell
.\start-all-services.ps1
```

### 9.5. Verify

- Eureka Dashboard: `http://localhost:8761`
- Gateway: `http://localhost:8888`
- API qua gateway: `http://localhost:8888/api/...`

---

## 10. Các Chức Năng Chưa Làm / Có Thể Phát Triển Tiếp

Các API hiện tại đã ổn theo project hiện có. Những phần sau là hướng phát triển tiếp:

### 10.1. Payment Gateway Thật

- Tích hợp VNPay/MoMo/Stripe.
- Tạo payment URL.
- Verify chữ ký callback/webhook.
- Lưu transaction id và gateway response.
- Refund thật qua gateway.

### 10.2. Review & Rating

- User đánh giá phim sau khi xem.
- Admin duyệt/ẩn review.
- Tính rating trung bình theo phim.

### 10.3. Voucher / Promotion

- CRUD voucher.
- Validate voucher khi booking.
- Apply discount vào booking.
- Theo dõi số lần dùng voucher.

### 10.4. Membership / Loyalty

- Tích điểm sau booking confirmed.
- Đổi điểm lấy voucher.
- Membership tiers: Bronze/Silver/Gold.

### 10.5. Reporting / Analytics

- Doanh thu theo ngày/tháng/năm.
- Doanh thu theo phim/rạp.
- Sản phẩm bán chạy.
- Tỉ lệ booking confirmed/cancelled.
- Export Excel/PDF.

### 10.6. Seat Locking Nâng Cao

- Lock ghế theo từng `seatId` và từng `showtimeId`.
- Tự release booking hết hạn.
- Chống double booking ở mức ghế/suất chiếu chi tiết hơn.

---

## 11. Kết Luận

Hệ thống hiện đã hoàn thiện các chức năng cốt lõi của bài toán quản lý rạp chiếu phim:

- Người dùng có thể đăng ký, đăng nhập, quản lý tài khoản.
- Admin có thể quản lý phim, rạp, phòng chiếu, suất chiếu, sản phẩm và tồn kho.
- User có thể tạo booking, chọn ghế, thêm sản phẩm.
- Booking có thể confirm/cancel, cập nhật payment và publish event.
- Notification Service có email thật, template, retry, resend và preference.
- Inventory Service tự động trừ/hoàn kho theo booking event.
- Gateway route đầy đủ tới các service.
- Circuit breaker giúp tăng khả năng chịu lỗi khi service phụ thuộc gặp sự cố.
- Contract tests đã kiểm tra toàn bộ API chính hiện có.

Dự án hiện là nền tảng microservice đầy đủ cho hệ thống cinema management, sẵn sàng mở rộng thêm payment gateway thật, voucher, loyalty và reporting.

---

**Version:** 2.0  
**Ngày cập nhật:** 2026-05-25  
**Trạng thái:** API hiện tại đã được bổ sung contract test và chạy pass.
