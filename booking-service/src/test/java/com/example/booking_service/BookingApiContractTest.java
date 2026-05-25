package com.example.booking_service;

import com.example.booking_service.controller.BookingController;
import com.example.booking_service.controller.PaymentController;
import com.example.booking_service.entity.Booking;
import com.example.booking_service.entity.BookingProduct;
import com.example.booking_service.entity.BookingSeat;
import com.example.booking_service.entity.Payment;
import com.example.booking_service.service.BookingService;
import com.example.booking_service.service.PaymentService;
import com.example.common.security.JwtAuthenticationDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class BookingApiContractTest {

    private MockMvc mockMvc;

    @Mock
    private BookingService bookingService;
    @Mock
    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                new BookingController(bookingService),
                new PaymentController(paymentService)
        ).build();
        authenticateAs(7L, "user", "USER");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void bookingEndpointsRespectContractAndIncludeSeatsProducts() throws Exception {
        Booking booking = booking();
        when(bookingService.getAllBookings()).thenReturn(List.of(booking));
        when(bookingService.getBookingsByUserId(7L)).thenReturn(List.of(booking));
        when(bookingService.createBooking(any(), eq(List.of(1L, 2L)))).thenReturn(booking);
        when(bookingService.getBookingById(100L)).thenReturn(booking);
        when(bookingService.confirmBooking(100L)).thenReturn(booking);
        when(bookingService.cancelBooking(eq(100L), eq("changed"))).thenReturn(booking);

        mockMvc.perform(get("/api/bookings")).andExpect(status().isOk()).andExpect(jsonPath("$[0].bookingReference").value("BK001"));
        mockMvc.perform(get("/api/bookings/my-bookings")).andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(100));
        mockMvc.perform(post("/api/bookings").contentType(MediaType.APPLICATION_JSON).content("""
                        {"showtimeId":1,"cinemaId":1,"movieId":1,"seatIds":[1,2],"totalAmount":180000,
                         "productItems":[{"productId":5,"quantity":1,"unitPrice":50000}]}
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.seats[0].seatLabel").value("A1"))
                .andExpect(jsonPath("$.products[0].totalPrice").value(50000));
        mockMvc.perform(get("/api/bookings/100")).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(100));
        mockMvc.perform(put("/api/bookings/100/confirm")).andExpect(status().isOk());
        mockMvc.perform(put("/api/bookings/100/cancel").param("reason", "changed")).andExpect(status().isOk());
    }

    @Test
    void paymentEndpointsRespectContract() throws Exception {
        Payment payment = new Payment();
        payment.setId(9L);
        payment.setPaymentReference("PAY001");
        payment.setPaymentMethod(Payment.PaymentMethod.BANK_TRANSFER);
        payment.setAmount(BigDecimal.valueOf(180000));
        payment.setCurrency("VND");
        payment.setPaymentStatus(Payment.PaymentStatus.PENDING);

        when(paymentService.createPayment(any())).thenReturn(payment);
        when(paymentService.getPaymentById(9L)).thenReturn(payment);
        when(paymentService.getPaymentByReference("PAY001")).thenReturn(payment);
        when(paymentService.getPaymentsByBooking(100L)).thenReturn(List.of(payment));
        when(paymentService.handleCallback(any())).thenReturn(payment);
        when(paymentService.refundPayment(eq(9L), any())).thenReturn(payment);

        mockMvc.perform(post("/api/payments").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bookingId\":100,\"paymentMethod\":\"BANK_TRANSFER\",\"amount\":180000,\"currency\":\"VND\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.paymentReference").value("PAY001"));
        mockMvc.perform(get("/api/payments/9")).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(9));
        mockMvc.perform(get("/api/payments/reference/PAY001")).andExpect(status().isOk());
        mockMvc.perform(get("/api/payments/booking/100")).andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(9));
        mockMvc.perform(post("/api/payments/callback").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"paymentReference\":\"PAY001\",\"paymentStatus\":\"PAID\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/payments/9/refund").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refundAmount\":180000}"))
                .andExpect(status().isOk());
    }

    private Booking booking() {
        Booking booking = new Booking();
        booking.setId(100L);
        booking.setBookingReference("BK001");
        booking.setUserId(7L);
        booking.setShowtimeId(1L);
        booking.setCinemaId(1L);
        booking.setMovieId(1L);
        booking.setTotalSeats(2);
        booking.setTotalAmount(BigDecimal.valueOf(230000));
        booking.setBookingStatus(Booking.BookingStatus.PENDING);
        booking.setPaymentStatus(Payment.PaymentStatus.PENDING);
        booking.setBookingDate(LocalDateTime.now());
        booking.setExpiryTime(LocalDateTime.now().plusMinutes(15));

        BookingSeat seat = new BookingSeat();
        seat.setSeatId(1L);
        seat.setSeatRow("A");
        seat.setSeatNumber(1);
        seat.setSeatType(BookingSeat.SeatType.REGULAR);
        seat.setPrice(BigDecimal.valueOf(90000));
        booking.setBookingSeats(List.of(seat));

        BookingProduct product = new BookingProduct();
        product.setProductId(5L);
        product.setQuantity(1);
        product.setUnitPrice(BigDecimal.valueOf(50000));
        booking.setBookingProducts(List.of(product));
        return booking;
    }

    private void authenticateAs(Long userId, String username, String role) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                username,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role)));
        authentication.setDetails(new JwtAuthenticationDetails(userId, role, new MockHttpServletRequest()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
