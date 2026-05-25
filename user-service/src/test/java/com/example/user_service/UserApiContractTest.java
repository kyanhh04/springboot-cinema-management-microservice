package com.example.user_service;

import com.example.common.security.JwtAuthenticationDetails;
import com.example.user_service.controller.AuthController;
import com.example.user_service.controller.UserController;
import com.example.user_service.dto.AuthResponse;
import com.example.user_service.dto.UserDTO;
import com.example.user_service.entity.UserAddress;
import com.example.user_service.entity.UserPaymentMethod;
import com.example.user_service.service.UserAddressService;
import com.example.user_service.service.UserPaymentMethodService;
import com.example.user_service.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserApiContractTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private MockMvc mockMvc;

    @Mock
    private UserService userService;
    @Mock
    private UserAddressService userAddressService;
    @Mock
    private UserPaymentMethodService userPaymentMethodService;

    @BeforeEach
    void setUp() {
        AuthController authController = new AuthController(userService);
        UserController userController = new UserController(userService, userAddressService, userPaymentMethodService);
        mockMvc = MockMvcBuilders.standaloneSetup(authController, userController).build();
        authenticateAs(1L, "john", "USER");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void authEndpointsRespectContract() throws Exception {
        when(userService.register(any())).thenReturn(new AuthResponse("token", 1L, "john", "john@example.com", "USER"));
        when(userService.login(any())).thenReturn(new AuthResponse("token", 1L, "john", "john@example.com", "USER"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"john","email":"john@example.com","password":"secret123","fullName":"John Doe"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token"))
                .andExpect(jsonPath("$.type").value("Bearer"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"john\",\"password\":\"secret123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("john"));
    }

    @Test
    void userManagementEndpointsRespectContract() throws Exception {
        UserDTO user = userDto(1L);
        when(userService.getUserByUsername("john")).thenReturn(user);
        when(userService.updateProfile(eq(1L), any())).thenReturn(user);
        doNothing().when(userService).changePassword(eq(1L), any());
        doNothing().when(userService).deactivateAccount(1L);
        when(userService.getUserById(2L)).thenReturn(userDto(2L));
        when(userService.getAllUsers()).thenReturn(List.of(user));
        when(userService.setActive(2L, false)).thenReturn(userDto(2L));
        when(userService.setActive(2L, true)).thenReturn(userDto(2L));
        when(userService.validateToken("abc")).thenReturn(true);

        mockMvc.perform(get("/api/users/me").principal(authentication(1L, "john", "USER")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(1));
        mockMvc.perform(put("/api/users/me").contentType(MediaType.APPLICATION_JSON).content("{\"fullName\":\"John\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/users/me/password").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"oldpass\",\"newPassword\":\"newpass\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.message").value("Password changed successfully"));
        mockMvc.perform(delete("/api/users/me")).andExpect(status().isOk());
        mockMvc.perform(get("/api/users/2")).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(2));
        mockMvc.perform(get("/api/users")).andExpect(status().isOk()).andExpect(jsonPath("$[0].username").value("john"));
        mockMvc.perform(put("/api/users/2/block")).andExpect(status().isOk());
        mockMvc.perform(put("/api/users/2/unblock")).andExpect(status().isOk());
        mockMvc.perform(post("/api/users/validate").header("Authorization", "Bearer abc"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.valid").value(true));
    }

    @Test
    void addressAndPaymentMethodEndpointsRespectContract() throws Exception {
        UserAddress address = new UserAddress();
        address.setId(10L);
        address.setAddressLine1("123 Main");
        address.setCity("HCM");
        UserPaymentMethod paymentMethod = new UserPaymentMethod();
        paymentMethod.setId(20L);
        paymentMethod.setPaymentType(UserPaymentMethod.PaymentType.CREDIT_CARD);

        when(userAddressService.getAddresses(1L)).thenReturn(List.of(address));
        when(userAddressService.createAddress(eq(1L), any())).thenReturn(address);
        when(userAddressService.updateAddress(eq(1L), eq(10L), any())).thenReturn(address);
        doNothing().when(userAddressService).deleteAddress(1L, 10L);
        when(userPaymentMethodService.getPaymentMethods(1L)).thenReturn(List.of(paymentMethod));
        when(userPaymentMethodService.createPaymentMethod(eq(1L), any())).thenReturn(paymentMethod);
        when(userPaymentMethodService.updatePaymentMethod(eq(1L), eq(20L), any())).thenReturn(paymentMethod);
        doNothing().when(userPaymentMethodService).deletePaymentMethod(1L, 20L);

        mockMvc.perform(get("/api/users/me/addresses")).andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(10));
        mockMvc.perform(post("/api/users/me/addresses").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"addressLine1\":\"123 Main\",\"city\":\"HCM\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/users/me/addresses/10").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"addressLine1\":\"123 Main\",\"city\":\"HCM\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/users/me/addresses/10")).andExpect(status().isOk());
        mockMvc.perform(get("/api/users/me/payment-methods")).andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(20));
        mockMvc.perform(post("/api/users/me/payment-methods").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"paymentType\":\"CREDIT_CARD\",\"cardNumber\":\"4111111111111111\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/users/me/payment-methods/20").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"paymentType\":\"CREDIT_CARD\",\"cardNumber\":\"4111111111111111\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/users/me/payment-methods/20")).andExpect(status().isOk());
    }

    private UserDTO userDto(Long id) {
        UserDTO dto = new UserDTO();
        dto.setId(id);
        dto.setUsername("john");
        dto.setEmail("john@example.com");
        dto.setRole("USER");
        dto.setActive(true);
        return dto;
    }

    private void authenticateAs(Long userId, String username, String role) {
        SecurityContextHolder.getContext().setAuthentication(authentication(userId, username, role));
    }

    private UsernamePasswordAuthenticationToken authentication(Long userId, String username, String role) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                username,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role)));
        authentication.setDetails(new JwtAuthenticationDetails(userId, role, new MockHttpServletRequest()));
        return authentication;
    }
}
