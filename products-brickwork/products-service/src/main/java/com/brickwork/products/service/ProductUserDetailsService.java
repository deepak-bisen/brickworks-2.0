package com.brickwork.products.service;

import org.springframework.security.core.userdetails.UserDetails;

public interface ProductUserDetailsService {
    public UserDetails loadUserByUsername(String username);
}
