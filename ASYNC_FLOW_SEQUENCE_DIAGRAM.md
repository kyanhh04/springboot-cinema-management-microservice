
## 🔄 LUỒNG 1: BOOKING CREATED KÈM PRODUCT → GỬI EMAIL XÁC NHẬN

**Mô tả**: User tạo booking mới kèm ghế và sản phẩm bắp/nước/combo, hệ thống lưu thông tin product đi cùng booking và gửi email thông báo "Vui lòng thanh toán trong 15 phút"

### Steps:

```
1. User → BookingService: POST /api/bookings
   Body: {
     showtimeId: 1,
     cinemaId: 1,
     movieId: 1,
     seatIds: [1, 2],
     productItems: [
       { productId: 1, quantity: 1, unitPrice: 50000 }
     ],
     totalAmount: 230000
   }

2. BookingService → CinemaService: POST /showtimes/{id}/reserve (REST API)
   - Reserve seats tạm thời

3. BookingService → Database: BEGIN TRANSACTION
   - INSERT INTO bookings (status = PENDING)
   - INSERT INTO booking_seats
   - INSERT INTO booking_products (booking_id, product_id, quantity, unit_price)

4. BookingService → Database: COMMIT TRANSACTION

5. BookingService → RabbitMQ: Publish BookingEvent
   Exchange: "booking.exchange"
   RoutingKey: "booking.created"
   Message: {
     bookingId: 123,
     bookingReference: "BKG20260525001",
     userId: 1,
     userEmail: "john@example.com",
     movieTitle: "Avengers",
     cinemaName: "CGV Vincom",
     totalAmount: 230000,
     expiryTime: "2026-05-25 14:45:00",
     eventType: "CREATED",
     productItems: [
       { productId: 1, quantity: 1, unitPrice: 50000 }
     ]
   }

6. BookingService → User: Response 201 Created
   - Không đợi email gửi xong (ASYNC)

--- Parallel Processing (Bất đồng bộ) ---

7. RabbitMQ → notification.booking.created.queue: Route message

8. NotificationService: @RabbitListener consume message

9. NotificationService → Database: GET email_template
   - templateName = "BOOKING_CREATED"

10. NotificationService: Merge data với template
    Subject: "Booking created BKG20260525001"
    Body: "Dear john@example.com,
           Your booking for Avengers at CGV Vincom.
           Please pay 230000 VND before 2026-05-25 14:45:00"

11. NotificationService → SMTP Server: Send email
    To: john@example.com

12. NotificationService → Database: INSERT notification
    - status = SENT/FAILED
    - notification_type = BOOKING_CREATED

13. NotificationService → RabbitMQ: ACK message
    - Message bị xóa khỏi queue
    
```
## 🔄 LUỒNG 2: BOOKING CONFIRMED → TRỪ KHO & GỬI EMAIL

**Mô tả**: User thanh toán thành công, hệ thống trừ kho sản phẩm và gửi email vé điện tử

### Steps:

```
1. User → BookingService: POST /api/payments
   Body: {
     bookingId: 123,
     paymentMethod: "BANK_TRANSFER",
     amount: 230000
   }

2. BookingService → Database: INSERT INTO payments
   - status = PENDING

3. PaymentGateway → BookingService: POST /api/payments/callback
   Body: {
     paymentReference: "PAY123",
     paymentStatus: "PAID"
   }

4. BookingService → Database: BEGIN TRANSACTION
   - UPDATE payments SET status = PAID
   - UPDATE bookings SET status = CONFIRMED, confirmed_at = NOW()

5. BookingService → Database: COMMIT TRANSACTION

6. BookingService → RabbitMQ: Publish BookingEvent
   Exchange: "booking.exchange"
   RoutingKey: "booking.confirmed"
   Message: {
     bookingId: 123,
     bookingReference: "BKG20260525001",
     cinemaId: 1,
     userEmail: "john@example.com",
     movieTitle: "Avengers",
     totalSeats: 2,
     totalAmount: 230000,
     eventType: "CONFIRMED",
     productItems: [
       { productId: 1, quantity: 1, unitPrice: 50000 }
     ]
   }

7. BookingService → User: Response 200 OK

--- Parallel Processing 1: Inventory Service ---

8. RabbitMQ → inventory.booking.confirmed.queue: Route message

9. InventoryService: @RabbitListener consume message

10. InventoryService → Database: Check duplicate
    - IF booking_products.exists(bookingId) → SKIP

11. InventoryService → Database: BEGIN TRANSACTION

12. InventoryService → Database: SELECT cinema_inventory FOR UPDATE
    - cinemaId = 1, productId = 1
    - Pessimistic lock (tránh race condition)

13. InventoryService: Validate stock
    - IF quantity < required → ROLLBACK & throw error

14. InventoryService → Database: UPDATE cinema_inventory
    - SET quantity = quantity - 1
    - WHERE cinema_id = 1 AND product_id = 1

15. InventoryService → Database: INSERT booking_products
    - bookingId = 123, productId = 1, quantity = 1

16. InventoryService → Database: INSERT stock_movements
    - movement_type = OUT
    - quantity = 1
    - reference_type = BOOKING
    - reference_id = 123
    - notes = "Stock deducted for booking BKG20260525001"

17. InventoryService → Database: COMMIT TRANSACTION

18. InventoryService → RabbitMQ: ACK message

--- Parallel Processing 2: Notification Service ---

19. RabbitMQ → notification.booking.confirmed.queue: Route message

20. NotificationService: @RabbitListener consume message

21. NotificationService → Database: GET email_template
    - templateName = "BOOKING_CONFIRMED"

22. NotificationService: Merge data
    Subject: "Booking confirmed BKG20260525001"
    Body: "Your booking for Avengers is confirmed.
           Cinema: CGV Vincom
           Seats: 2
           Total: 230000 VND
           Show your QR code at cinema."

23. NotificationService → SMTP Server: Send email
    To: john@example.com
    Attachment: QR code / E-ticket

24. NotificationService → Database: INSERT notification
    - status = SENT

25. NotificationService → RabbitMQ: ACK message

--- End of Flow ---
```

**Note**:
- Step 8-18 (Inventory) và 19-25 (Notification) chạy song song
- Transaction đảm bảo atomicity
- Pessimistic lock tránh oversell

---

## 🔄 LUỒNG 3: BOOKING CANCELLED → HOÀN KHO & GỬI EMAIL

**Mô tả**: User hủy booking, hệ thống hoàn trả tồn kho và gửi email thông báo

### Steps:

```
1. User → BookingService: PUT /api/bookings/{id}/cancel
   Query: ?reason=change-plan

2. BookingService → CinemaService: POST /showtimes/{id}/release (REST API)
   - Release reserved seats

3. BookingService → Database: BEGIN TRANSACTION
   - UPDATE bookings
     SET status = CANCELLED,
         cancelled_at = NOW(),
         cancellation_reason = 'change-plan'

4. BookingService → Database: COMMIT TRANSACTION

5. BookingService → RabbitMQ: Publish BookingEvent
   Exchange: "booking.exchange"
   RoutingKey: "booking.cancelled"
   Message: {
     bookingId: 123,
     bookingReference: "BKG20260525001",
     cinemaId: 1,
     userEmail: "john@example.com",
     movieTitle: "Avengers",
     cancellationReason: "change-plan",
     eventType: "CANCELLED",
     productItems: []
   }

6. BookingService → User: Response 200 OK

--- Parallel Processing 1: Inventory Service ---

7. RabbitMQ → inventory.booking.cancelled.queue: Route message

8. InventoryService: @RabbitListener consume message

9. InventoryService → Database: GET booking_products
   - WHERE booking_id = 123

10. InventoryService: Check if exists
    - IF empty → SKIP (chưa confirm nên chưa trừ kho)

11. InventoryService → Database: BEGIN TRANSACTION

12. FOR EACH product IN booking_products:

13.   InventoryService → Database: SELECT cinema_inventory FOR UPDATE
      - cinemaId = 1, productId = 1

14.   InventoryService → Database: UPDATE cinema_inventory
      - SET quantity = quantity + 1  (HOÀN TRẢ)
      - WHERE cinema_id = 1 AND product_id = 1

15.   InventoryService → Database: INSERT stock_movements
      - movement_type = RETURN
      - quantity = 1
      - reference_type = BOOKING
      - reference_id = 123
      - notes = "Stock restored for cancelled booking"

16. InventoryService → Database: DELETE booking_products
    - WHERE booking_id = 123

17. InventoryService → Database: COMMIT TRANSACTION

18. InventoryService → RabbitMQ: ACK message

--- Parallel Processing 2: Notification Service ---

19. RabbitMQ → notification.booking.cancelled.queue: Route message

20. NotificationService: @RabbitListener consume message

21. NotificationService → Database: GET email_template
    - templateName = "BOOKING_CANCELLED"

22. NotificationService: Merge data
    Subject: "Booking cancelled BKG20260525001"
    Body: "Your booking for Avengers has been cancelled.
           Reason: change-plan
           Refund will be processed in 3-5 business days."

23. NotificationService → SMTP Server: Send email
    To: john@example.com

24. NotificationService → Database: INSERT notification
    - status = SENT

25. NotificationService → RabbitMQ: ACK message

