package com.brickwork.orders.order.service;

import org.springframework.security.core.userdetails.UserDetails;

public interface OrderUserDetailsService {
    public UserDetails loadUserByUsername(String username);
}