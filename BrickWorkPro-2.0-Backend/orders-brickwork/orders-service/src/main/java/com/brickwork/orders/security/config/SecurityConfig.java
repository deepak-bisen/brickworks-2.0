package com.brickwork.orders.security.config;

import com.brickwork.security.filter.JwtRequestFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // Public routes (Guest Checkout, Leads & Internal Feign Updates)
                        .requestMatchers(HttpMethod.POST, "/api/orders/public-quote").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/orders/create").permitAll()
                        // Guest tracking endpoint
                        .requestMatchers(HttpMethod.GET, "/api/orders/track").permitAll()

                        // FIX: Allow Finance Service to update order status via Feign!
                        .requestMatchers(HttpMethod.PUT, "/api/orders/*/status").permitAll()

                        // Customer Portal actions
                        .requestMatchers(HttpMethod.GET, "/api/orders/customer/**").hasAnyRole("CUSTOMER", "ADMIN")

                        // Admins & Managers updating lifecycles
                        .requestMatchers(HttpMethod.GET, "/api/orders/*").hasAnyRole("ADMIN", "MANAGER","STAFF", "CUSTOMER")

                        // Admin Panel specific endpoints
                        .requestMatchers(HttpMethod.GET, "/api/orders/all/orders").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/orders/all/get/public-qoute").hasRole("ADMIN")

                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}