package com.brickwork.finance.security;

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
import org.springframework.security.config.http.SessionCreationPolicy;
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
                        .requestMatchers("/api/finance/webhooks/**").permitAll()

                        .requestMatchers(HttpMethod.POST, "/api/finance/payments/create-order").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/finance/payments/verify").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/finance/payments/cod/select").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/finance/payments/utr/submit").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/finance/invoice/generate/**").permitAll()

                        .requestMatchers(HttpMethod.POST, "/api/finance/payments/utr/verify/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/finance/payments/order/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/finance/payments/refund/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/finance/payments/cod/collect/**").hasRole("ADMIN")

                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.addFilterBefore(internalServiceAuthFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}