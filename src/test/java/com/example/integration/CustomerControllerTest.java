package com.example.integration;

import com.example.entity.Authority;
import com.example.entity.AuthorityType;
import com.example.entity.Customer;
import com.example.entity.User;
import com.example.exception.UserNotFoundException;
import com.example.repository.CustomerRepository;
import com.example.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CustomerControllerTest {

    private final String baseUrl = "/customers";
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeAll
    void init() {
        Set<Authority> authoritySet = new HashSet<>();
        authoritySet.add(new Authority(AuthorityType.ROLE_USER));
        final String encodedPassword = passwordEncoder.encode("userpass");
        final User user = new User("user", encodedPassword, "user@company.com", authoritySet);
        userRepository.save(user);

        authoritySet = new HashSet<>();
        authoritySet.add(new Authority(AuthorityType.ROLE_ADMIN));
        final String adminpass = passwordEncoder.encode("adminpass");
        final User admin = new User("admin", adminpass, "admin@company.com", authoritySet);
        userRepository.save(admin);
    }

    @Test
    public void updateCustomerTest_NotFound() throws Exception {
        final User user = userRepository.findById(1L).orElseThrow(UserNotFoundException::new);

        Customer customer = new Customer("firstCustomer", "lastnameCustomer", LocalDate.now(), user);
        final String requestBodyJson = objectMapper.writeValueAsString(customer);

        final String credentialHash = Base64.getEncoder().encodeToString("user:userpass".getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(
                put(baseUrl + "/1000000")
                        .content(requestBodyJson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Basic " + credentialHash)
        ).andExpect(status().isNotFound());
    }

    @Test
    public void updateCustomer_BadRequest() throws Exception {
        final String credentialHash = Base64.getEncoder().encodeToString("user:userpass".getBytes(StandardCharsets.UTF_8));

        //request with no body
        mockMvc.perform(
                put(baseUrl + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Basic " + credentialHash))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.error").value("INVALID_REQUEST_BODY"));

        //request body has no field
        mockMvc.perform(
                put(baseUrl + "/1")
                        .content("{}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Basic " + credentialHash))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.error.firstName").value("firstName field is required"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.error.lastName").value("lastName field is required"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.error.dateOfBirth").value("dateOfBirth field is required"));
    }

    @Test
    public void updateCustomerTest() throws Exception {
        final User user = userRepository.findById(1L).orElseThrow(UserNotFoundException::new);

        Customer customer = new Customer("firstCustomer", "lastnameCustomer", LocalDate.now(), user);
        final Customer savedCustomer = customerRepository.save(customer);

        Customer updatedCustomer = new Customer("testfirstname1", "testlastname1", LocalDate.now().minusDays(1), user);
        final String requestBodyJson = objectMapper.writeValueAsString(updatedCustomer);

        final String credentioalHash = Base64.getEncoder().encodeToString("user:userpass".getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(
                put(baseUrl + "/" + savedCustomer.getId())
                        .content(requestBodyJson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Basic " + credentioalHash)
        ).andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isMap())
                .andExpect(MockMvcResultMatchers.jsonPath("$.firstName").value(updatedCustomer.getFirstName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.lastName").value(updatedCustomer.getLastName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.dateOfBirth").value(updatedCustomer.getDateOfBirth().toString()));
    }

    @Test
    public void findCustomerTest() throws Exception {
        final User user = userRepository.findById(1L).orElseThrow(UserNotFoundException::new);
        Customer customer2 =
                new Customer("testfirstname2", "testlastname2", LocalDate.now().minusDays(2), user);
        final Customer savedCustomer = customerRepository.save(customer2);

        final String credentioalHash = Base64.getEncoder().encodeToString("user:userpass".getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(
                get(baseUrl + "/" + savedCustomer.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Basic " + credentioalHash)
        )
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isMap())
                .andExpect(MockMvcResultMatchers.jsonPath("$.firstName").value(customer2.getFirstName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.lastName").value(customer2.getLastName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.dateOfBirth").value(customer2.getDateOfBirth().toString()));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    public void findCustomersOrderByBirthDateDescLimit3() throws Exception {
        final User user = userRepository.findById(1L).orElseThrow(UserNotFoundException::new);
        List<Customer> customers = new ArrayList<>(+5);
        for (int i = 0; i < 5; i++) {
            Customer customer = new Customer("testfirstname" + i, "testlastname" + i,
                    LocalDate.now().plusDays(i), user);
            customerRepository.save(customer);
            customers.add(customer);
        }

        mockMvc.perform(
                get(baseUrl + "/three-youngest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(3)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[0].firstName").value(customers.get(4).getFirstName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[0].lastName").value(customers.get(4).getLastName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[0].dateOfBirth").value(customers.get(4).getDateOfBirth().toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[1].firstName").value(customers.get(3).getFirstName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[1].lastName").value(customers.get(3).getLastName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[1].dateOfBirth").value(customers.get(3).getDateOfBirth().toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[2].firstName").value(customers.get(2).getFirstName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[2].lastName").value(customers.get(2).getLastName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[2].dateOfBirth").value(customers.get(2).getDateOfBirth().toString()));
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void findCustomersOrderByBirthDateDescLimit3_Forbidden() throws Exception {
        mockMvc.perform(
                get(baseUrl + "/three-youngest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}
