package com.brickwork.products.security.service;

import org.springframework.security.core.userdetails.UserDetails;

public interface ProductUserDetailsService {
    public UserDetails loadUserByUsername(String username);
}
