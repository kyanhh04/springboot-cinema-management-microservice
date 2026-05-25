package com.example.user_service.service;

import com.example.user_service.entity.User;
import com.example.user_service.entity.UserAddress;
import com.example.user_service.repository.UserAddressRepository;
import com.example.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserAddressService {

    private final UserAddressRepository userAddressRepository;
    private final UserRepository userRepository;

    public List<UserAddress> getAddresses(Long userId) {
        return userAddressRepository.findByUserId(userId);
    }

    public UserAddress getAddress(Long userId, Long addressId) {
        UserAddress address = userAddressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found with id: " + addressId));
        if (!address.getUser().getId().equals(userId)) {
            throw new RuntimeException("Address does not belong to user");
        }
        return address;
    }

    @Transactional
    public UserAddress createAddress(Long userId, UserAddress request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        request.setId(null);
        request.setUser(user);
        return userAddressRepository.save(request);
    }

    @Transactional
    public UserAddress updateAddress(Long userId, Long addressId, UserAddress request) {
        UserAddress address = getAddress(userId, addressId);
        address.setAddressLine1(request.getAddressLine1());
        address.setAddressLine2(request.getAddressLine2());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setIsDefault(request.getIsDefault());
        return userAddressRepository.save(address);
    }

    @Transactional
    public void deleteAddress(Long userId, Long addressId) {
        userAddressRepository.delete(getAddress(userId, addressId));
    }
}
