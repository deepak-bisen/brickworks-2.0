package com.brickwork.orders.security.config;

import com.brickwork.security.filter.InternalServiceAuthFilter;
import com.brickwork.security.filter.JwtRequestFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    @Autowired
    private InternalServiceAuthFilter internalServiceAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/orders/public-quote").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/orders/create").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/orders/track").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/orders/*/status")
                            .hasAnyRole("ADMIN", "STAFF", "INTERNAL_SERVICE")
                        .requestMatchers(HttpMethod.GET, "/api/orders/customer/**").hasAnyRole("CUSTOMER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/orders/*")
                            .hasAnyRole("ADMIN", "STAFF", "CUSTOMER", "INTERNAL_SERVICE")
                        .requestMatchers(HttpMethod.GET, "/api/orders/all/orders").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/orders/all/get/public-quote").hasRole("ADMIN")
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(internalServiceAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}