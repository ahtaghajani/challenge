package com.example.controller;

import com.example.dto.CustomerDto;
import com.example.entity.Customer;
import com.example.service.CustomerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<CustomerDto> findCustomemer(@PathVariable("customerId") Long customerId) {
        Customer customer = customerService.findCustomer(customerId);
        return new ResponseEntity<>(new CustomerDto(customer), HttpStatus.OK);
    }

    @PutMapping("/{customerId}")
    public ResponseEntity<CustomerDto> updateCustomer(@Valid @RequestBody CustomerDto customerDto,
                                                      @PathVariable("customerId") Long customerId) {
        Customer customerResult = customerService.updateCustomer(customerDto.toCustomer(), customerId);
        return new ResponseEntity<>(new CustomerDto(customerResult), HttpStatus.OK);
    }

    @GetMapping("/three-youngest")
    public ResponseEntity<List<CustomerDto>> findCustomersOrderByBirthDateDescLimit3() {
        List<CustomerDto> customerList = customerService.findCustomersOrderByBirthDateDescLimit3()
                .stream().map(CustomerDto::new).collect(Collectors.toList());
        return new ResponseEntity<>(customerList, HttpStatus.OK);
    }
}
