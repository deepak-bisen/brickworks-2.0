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
                        // Public lead generation (No token needed to request a quote)
                        .requestMatchers(HttpMethod.POST, "/api/orders/public-quote").permitAll()

                        // Customer Portal actions
                        .requestMatchers(HttpMethod.POST, "/api/orders/create").hasAnyRole("CUSTOMER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/orders/customer/**").hasAnyRole("CUSTOMER", "ADMIN")

                        // Admins & Managers updating lifecycles and fetching specific orders
                        .requestMatchers(HttpMethod.GET, "/api/orders/{id}").hasAnyRole("ADMIN", "MANAGER","STAFF", "CUSTOMER")
                        .requestMatchers(HttpMethod.PUT, "/api/orders/{id}/status").hasAnyRole("ADMIN", "MANAGER", "STAFF")

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