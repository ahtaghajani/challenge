package com.example.service;

import com.example.entity.Customer;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface CustomerService {

    Customer findCustomer(Long customerId);

    Customer updateCustomer(Customer customer, Long customerId);

    List<Customer> findCustomersOrderByBirthDateDescLimit3();
}
