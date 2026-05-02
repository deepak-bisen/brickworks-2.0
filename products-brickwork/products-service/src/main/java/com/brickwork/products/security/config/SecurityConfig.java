package com.brickwork.products.security.config;

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
                        // Public Routes (No token needed to view catalog)
                        .requestMatchers(HttpMethod.GET, "/api/products", "/api/products/**").permitAll()

                        // Admin Only: Creating or updating products
                        .requestMatchers(HttpMethod.POST, "/api/products").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/products/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/products/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasRole("ADMIN")

                        // Managers & Admins: Handling factory lifecycle
                        .requestMatchers("/api/raw-materials/**", "/api/production-logs/**").hasAnyRole("ADMIN", "MANAGER","STAFF")

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}