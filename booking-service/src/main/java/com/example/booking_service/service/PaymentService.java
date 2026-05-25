package com.example.booking_service.service;

import com.example.booking_service.dto.CreatePaymentRequest;
import com.example.booking_service.dto.PaymentCallbackRequest;
import com.example.booking_service.dto.RefundPaymentRequest;
import com.example.booking_service.entity.Booking;
import com.example.booking_service.entity.Payment;
import com.example.booking_service.repository.BookingRepository;
import com.example.booking_service.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final BookingService bookingService;

    public List<Payment> getPaymentsByBooking(Long bookingId) {
        return paymentRepository.findByBookingId(bookingId);
    }

    public Payment getPaymentById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + id));
    }

    public Payment getPaymentByReference(String paymentReference) {
        return paymentRepository.findByPaymentReference(paymentReference)
                .orElseThrow(() -> new RuntimeException("Payment not found with reference: " + paymentReference));
    }

    @Transactional
    public Payment createPayment(CreatePaymentRequest request) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + request.getBookingId()));

        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setPaymentReference(generatePaymentReference());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setAmount(request.getAmount() != null ? request.getAmount() : booking.getTotalAmount());
        payment.setCurrency(request.getCurrency() != null ? request.getCurrency() : "VND");
        payment.setPaymentStatus(Payment.PaymentStatus.PENDING);
        return paymentRepository.save(payment);
    }

    @Transactional
    public Payment handleCallback(PaymentCallbackRequest request) {
        Payment payment = getPaymentByReference(request.getPaymentReference());
        Payment.PaymentStatus status = request.getPaymentStatus();
        payment.setPaymentStatus(status);

        Booking booking = payment.getBooking();
        if (status == Payment.PaymentStatus.PAID) {
            payment.setPaidAt(LocalDateTime.now());
            bookingService.confirmBooking(booking.getId());
        } else if (status == Payment.PaymentStatus.FAILED) {
            booking.setPaymentStatus(Payment.PaymentStatus.FAILED);
            bookingRepository.save(booking);
        }

        return paymentRepository.save(payment);
    }

    @Transactional
    public Payment refundPayment(Long id, RefundPaymentRequest request) {
        Payment payment = getPaymentById(id);
        payment.setPaymentStatus(Payment.PaymentStatus.REFUNDED);
        payment.setRefundedAt(LocalDateTime.now());
        payment.setRefundAmount(request.getRefundAmount() != null ? request.getRefundAmount() : payment.getAmount());

        Booking booking = payment.getBooking();
        booking.setPaymentStatus(Payment.PaymentStatus.REFUNDED);
        bookingRepository.save(booking);

        return paymentRepository.save(payment);
    }

    private String generatePaymentReference() {
        return "PAY" + UUID.randomUUID().toString().substring(0, 10).toUpperCase();
    }
}
