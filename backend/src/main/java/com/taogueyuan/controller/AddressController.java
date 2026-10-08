package com.taogueyuan.controller;

import com.taogueyuan.entity.Address;
import com.taogueyuan.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
@CrossOrigin(origins = "*")
public class AddressController {

    @Autowired
    private AddressService addressService;

    @GetMapping
    public List<Address> getUserAddresses(@RequestParam Long userId) {
        return addressService.getUserAddresses(userId);
    }

    @PostMapping
    public Address addAddress(@RequestParam Long userId, @RequestBody Address address) {
        return addressService.addAddress(userId, address);
    }

    @PutMapping("/{id}")
    public Address updateAddress(@PathVariable Long id, @RequestBody Address address) {
        return addressService.updateAddress(id, address);
    }

    @DeleteMapping("/{id}")
    public void deleteAddress(@PathVariable Long id) {
        addressService.deleteAddress(id);
    }
}