package com.taogueyuan.service;

import com.taogueyuan.entity.Address;
import com.taogueyuan.entity.User;
import com.taogueyuan.repository.AddressRepository;
import com.taogueyuan.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AddressService {

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Address> getUserAddresses(Long userId) {
        return addressRepository.findByUserId(userId);
    }

    public Address getAddressById(Long id) {
        return addressRepository.findById(id).orElse(null);
    }

    @Transactional
    public Address addAddress(Long userId, Address address) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found")); // 用户未找到
        address.setUser(user);
        
        // 如果是第一个地址，将其设置为默认
        List<Address> existing = addressRepository.findByUserId(userId);
        if (existing.isEmpty()) {
            address.setIsDefault(true);
        } else if (Boolean.TRUE.equals(address.getIsDefault())) {
            // 如果设置为默认，取消其他地址的默认设置
            existing.forEach(a -> {
                if (Boolean.TRUE.equals(a.getIsDefault())) {
                    a.setIsDefault(false);
                    addressRepository.save(a);
                }
            });
        }
        
        return addressRepository.save(address);
    }

    @Transactional
    public Address updateAddress(Long id, Address updatedAddress) {
        Address address = getAddressById(id);
        if (address != null) {
            address.setRecipientName(updatedAddress.getRecipientName());
            address.setPhone(updatedAddress.getPhone());
            address.setProvince(updatedAddress.getProvince());
            address.setCity(updatedAddress.getCity());
            address.setDistrict(updatedAddress.getDistrict());
            address.setDetailAddress(updatedAddress.getDetailAddress());
            
            if (Boolean.TRUE.equals(updatedAddress.getIsDefault()) && !Boolean.TRUE.equals(address.getIsDefault())) {
                // 设置为默认，取消其他地址的默认设置
                List<Address> userAddresses = addressRepository.findByUserId(address.getUser().getId());
                userAddresses.forEach(a -> {
                    if (Boolean.TRUE.equals(a.getIsDefault())) {
                        a.setIsDefault(false);
                        addressRepository.save(a);
                    }
                });
                address.setIsDefault(true);
            }
            
            return addressRepository.save(address);
        }
        return null;
    }

    @Transactional
    public void deleteAddress(Long id) {
        addressRepository.deleteById(id);
    }
}
