package com.example.controller;

import com.example.service.UserService;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PatchMapping("users/current/password")
    public void updateUser(@RequestBody String password) {
        userService.updatePasswordOfCurrentUser(password);
    }
}
