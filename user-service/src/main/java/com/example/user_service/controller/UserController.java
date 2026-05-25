package com.example.user_service.controller;

import com.example.user_service.dto.ChangePasswordRequest;
import com.example.user_service.dto.UpdateUserRequest;
import com.example.user_service.dto.UserDTO;
import com.example.user_service.entity.UserAddress;
import com.example.user_service.entity.UserPaymentMethod;
import com.example.user_service.service.UserAddressService;
import com.example.user_service.service.UserPaymentMethodService;
import com.example.user_service.service.UserService;
import com.example.common.security.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserAddressService userAddressService;
    private final UserPaymentMethodService userPaymentMethodService;

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.ok(userService.getUserByUsername(username));
    }

    @PutMapping("/me")
    public ResponseEntity<UserDTO> updateProfile(@Valid @RequestBody UpdateUserRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(userService.updateProfile(userId, request));
    }

    @PutMapping("/me/password")
    public ResponseEntity<Map<String, String>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        userService.changePassword(userId, request);
        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }

    @DeleteMapping("/me")
    public ResponseEntity<Map<String, String>> deactivateAccount() {
        Long userId = SecurityUtils.getCurrentUserId();
        userService.deactivateAccount(userId);
        return ResponseEntity.ok(Map.of("message", "Account deactivated successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PutMapping("/{id}/block")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> blockUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.setActive(id, false));
    }

    @PutMapping("/{id}/unblock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> unblockUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.setActive(id, true));
    }

    @PostMapping("/validate")
    public ResponseEntity<Map<String, Boolean>> validateToken(@RequestHeader("Authorization") String token) {
        String jwtToken = token.replace("Bearer ", "");
        boolean isValid = userService.validateToken(jwtToken);
        return ResponseEntity.ok(Map.of("valid", isValid));
    }

    @GetMapping("/me/addresses")
    public ResponseEntity<List<UserAddress>> getMyAddresses() {
        return ResponseEntity.ok(userAddressService.getAddresses(SecurityUtils.getCurrentUserId()));
    }

    @PostMapping("/me/addresses")
    public ResponseEntity<UserAddress> createMyAddress(@RequestBody UserAddress address) {
        return ResponseEntity.ok(userAddressService.createAddress(SecurityUtils.getCurrentUserId(), address));
    }

    @PutMapping("/me/addresses/{addressId}")
    public ResponseEntity<UserAddress> updateMyAddress(
            @PathVariable Long addressId,
            @RequestBody UserAddress address) {
        return ResponseEntity.ok(userAddressService.updateAddress(SecurityUtils.getCurrentUserId(), addressId, address));
    }

    @DeleteMapping("/me/addresses/{addressId}")
    public ResponseEntity<Map<String, String>> deleteMyAddress(@PathVariable Long addressId) {
        userAddressService.deleteAddress(SecurityUtils.getCurrentUserId(), addressId);
        return ResponseEntity.ok(Map.of("message", "Address deleted successfully"));
    }

    @GetMapping("/me/payment-methods")
    public ResponseEntity<List<UserPaymentMethod>> getMyPaymentMethods() {
        return ResponseEntity.ok(userPaymentMethodService.getPaymentMethods(SecurityUtils.getCurrentUserId()));
    }

    @PostMapping("/me/payment-methods")
    public ResponseEntity<UserPaymentMethod> createMyPaymentMethod(@RequestBody UserPaymentMethod paymentMethod) {
        return ResponseEntity.ok(userPaymentMethodService.createPaymentMethod(SecurityUtils.getCurrentUserId(), paymentMethod));
    }

    @PutMapping("/me/payment-methods/{paymentMethodId}")
    public ResponseEntity<UserPaymentMethod> updateMyPaymentMethod(
            @PathVariable Long paymentMethodId,
            @RequestBody UserPaymentMethod paymentMethod) {
        return ResponseEntity.ok(userPaymentMethodService.updatePaymentMethod(
                SecurityUtils.getCurrentUserId(), paymentMethodId, paymentMethod));
    }

    @DeleteMapping("/me/payment-methods/{paymentMethodId}")
    public ResponseEntity<Map<String, String>> deleteMyPaymentMethod(@PathVariable Long paymentMethodId) {
        userPaymentMethodService.deletePaymentMethod(SecurityUtils.getCurrentUserId(), paymentMethodId);
        return ResponseEntity.ok(Map.of("message", "Payment method deleted successfully"));
    }
}
