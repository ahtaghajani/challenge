package com.example.service.impl;

import com.example.entity.User;
import com.example.exception.UserNotFoundException;
import com.example.repository.UserRepository;
import com.example.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static com.example.util.SecurityUtil.getCurrentUser;

@Service
public class UserServiceImpl implements UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public UserServiceImpl(PasswordEncoder passwordEncoder, UserRepository userRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @Override
    public void updatePasswordOfCurrentUser(String password) {
        final User currentUser = getCurrentUser();
        final User user = userRepository.findById(currentUser.getId())
                .orElseThrow(UserNotFoundException::new);
        final String encodedPass = passwordEncoder.encode(password);
        user.setPassword(encodedPass);
        userRepository.save(user);

        currentUser.setPassword(encodedPass);
    }

}
