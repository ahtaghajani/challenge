package com.example.service.impl;

import com.example.entity.Customer;
import com.example.entity.User;
import com.example.exception.CustomerNotFoundException;
import com.example.repository.CustomerRepository;
import com.example.service.CustomerService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;

import static com.example.util.SecurityUtil.getCurrentUser;

@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerServiceImpl(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @Override
    public Customer findCustomer(Long customerId) {
        Assert.notNull(customerId, "customerId is null");
        final User currentUser = getCurrentUser();

        return customerRepository.findByIdAndUser(customerId, currentUser)
                .orElseThrow(CustomerNotFoundException::new);
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @Override
    public Customer updateCustomer(Customer customer, Long customerId) {
        final User currentUser = getCurrentUser();
        final Customer customerByIdAndUserId = customerRepository.findByIdAndUser(customerId, currentUser)
                .orElseThrow(CustomerNotFoundException::new);
        customer.setId(customerByIdAndUserId.getId());
        customer.setUser(customerByIdAndUserId.getUser());

        return customerRepository.save(customer);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public List<Customer> findCustomersOrderByBirthDateDescLimit3() {
        final Page<Customer> dateOfBirth = customerRepository.findAll(
                PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "dateOfBirth")));
        return dateOfBirth.getContent();
    }


}
