package com.example.integration;

import com.example.entity.Authority;
import com.example.entity.AuthorityType;
import com.example.entity.User;
import com.example.exception.UserNotFoundException;
import com.example.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private MockMvc mockMvc;

    @Test
    public void updateUserPassword() throws Exception {
        Set<Authority> authoritySet = new HashSet<>();
        authoritySet.add(new Authority(AuthorityType.ROLE_USER));
        final String encodedPassword = passwordEncoder.encode("userpass");
        final String username = "user2";
        final User user = new User(username, encodedPassword, "user2@company.com", authoritySet);
        final User savedUser = userRepository.save(user);

        final String credentialHash = Base64.getEncoder().encodeToString((username+":userpass").getBytes(StandardCharsets.UTF_8));

        String newPassword = "123412";
        mockMvc.perform(patch("/users/current/password")
                .content(newPassword)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Basic " + credentialHash)
        ).andExpect(status().isOk());

        final User updatedUser = userRepository.findById(savedUser.getId()).orElseThrow(UserNotFoundException::new);
        Assertions.assertTrue(passwordEncoder.matches(newPassword, updatedUser.getPassword()));

        Assertions.assertEquals(user.getUsername(), updatedUser.getUsername());
        Assertions.assertEquals(user.getEmail(), updatedUser.getEmail());
    }
}
