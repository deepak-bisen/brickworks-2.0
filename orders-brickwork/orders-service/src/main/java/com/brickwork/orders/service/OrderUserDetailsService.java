package com.brickwork.orders.service;

import org.springframework.security.core.userdetails.UserDetails;

public interface OrderUserDetailsService {
    public UserDetails loadUserByUsername(String username);
}