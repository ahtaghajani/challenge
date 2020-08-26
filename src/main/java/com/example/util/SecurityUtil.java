package com.example.util;

import com.example.entity.User;
import com.example.exception.UnAuthorizedActionException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public class SecurityUtil {
    public static User getCurrentUser() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        return Optional.ofNullable(securityContext.getAuthentication())
                .map(authentication -> {
                    if (authentication.getPrincipal() instanceof User) {
                        return (User) authentication.getPrincipal();
                    }
                    return null;
                }).orElseThrow(UnAuthorizedActionException::new);
    }
}
