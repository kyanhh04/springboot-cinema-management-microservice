package com.example.user_service.service;

import com.example.user_service.entity.User;
import com.example.user_service.entity.UserPaymentMethod;
import com.example.user_service.repository.UserPaymentMethodRepository;
import com.example.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserPaymentMethodService {

    private final UserPaymentMethodRepository userPaymentMethodRepository;
    private final UserRepository userRepository;

    public List<UserPaymentMethod> getPaymentMethods(Long userId) {
        return userPaymentMethodRepository.findByUserId(userId);
    }

    public UserPaymentMethod getPaymentMethod(Long userId, Long paymentMethodId) {
        UserPaymentMethod paymentMethod = userPaymentMethodRepository.findById(paymentMethodId)
                .orElseThrow(() -> new RuntimeException("Payment method not found with id: " + paymentMethodId));
        if (!paymentMethod.getUser().getId().equals(userId)) {
            throw new RuntimeException("Payment method does not belong to user");
        }
        return paymentMethod;
    }

    @Transactional
    public UserPaymentMethod createPaymentMethod(Long userId, UserPaymentMethod request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        request.setId(null);
        request.setUser(user);
        return userPaymentMethodRepository.save(request);
    }

    @Transactional
    public UserPaymentMethod updatePaymentMethod(Long userId, Long paymentMethodId, UserPaymentMethod request) {
        UserPaymentMethod paymentMethod = getPaymentMethod(userId, paymentMethodId);
        paymentMethod.setPaymentType(request.getPaymentType());
        paymentMethod.setCardNumber(request.getCardNumber());
        paymentMethod.setCardHolderName(request.getCardHolderName());
        paymentMethod.setExpiryDate(request.getExpiryDate());
        paymentMethod.setIsDefault(request.getIsDefault());
        return userPaymentMethodRepository.save(paymentMethod);
    }

    @Transactional
    public void deletePaymentMethod(Long userId, Long paymentMethodId) {
        userPaymentMethodRepository.delete(getPaymentMethod(userId, paymentMethodId));
    }
}
