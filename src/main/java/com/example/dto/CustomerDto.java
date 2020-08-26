package com.example.dto;

import com.example.entity.Customer;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

public class CustomerDto {
    private Long id;
    @NotBlank(message = "firstName field is required")
    private String firstName;
    @NotBlank(message = "lastName field is required")
    private String lastName;
    @NotNull(message = "dateOfBirth field is required")
    private LocalDate dateOfBirth;

    public CustomerDto() {
        //jackson
    }

    public CustomerDto(Customer customer) {
        this(customer.getId(), customer.getFirstName(), customer.getLastName(), customer.getDateOfBirth());
    }

    public CustomerDto(Long id, String firstName, String lastName, LocalDate dateOfBirth) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
    }

    public Customer toCustomer() {
        return new Customer(firstName, lastName, dateOfBirth, null);
    }

    public Long getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }
}
