package com.brickwork.products.security.service.impl;

import com.brickwork.products.security.service.ProductUserDetailsService;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductUserDetailsServiceImpl implements ProductUserDetailsService {
    // This is a simplified version for services that only validate tokens.
    // It doesn't need to check a database. It just creates a UserDetails object
    // from the username extracted from the token.
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // --- DEV BYPASS ---
        if ("admin_test_user".equals(username)) {
            return new User("admin_test_user", "password", List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        }else
           return new User(username, "", new ArrayList<>());
    }
}

