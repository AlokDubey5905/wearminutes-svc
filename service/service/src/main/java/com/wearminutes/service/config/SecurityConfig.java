package com.wearminutes.service.config;

import com.wearminutes.service.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/api/customer/users/register", "/api/customer/users/login").permitAll()
                .requestMatchers("/api/vendor/users/register", "/api/vendor/users/login").permitAll()
                .requestMatchers("/api/delivery/users/register", "/api/delivery/users/login").permitAll()
                .requestMatchers("/api/admin/users/register", "/api/admin/users/login").permitAll()
                .requestMatchers("/api/hello/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/v3/api-docs", "/swagger-resources/**").permitAll()
                
                // Admin endpoints - ADMIN role only (full platform access)
                .requestMatchers("/api/admin/orders/checkout").hasAnyRole("CUSTOMER", "ADMIN")
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/admin/orders/**").hasRole("ADMIN")
                
                // Customer endpoints - CUSTOMER or ADMIN role
                .requestMatchers("/api/customer/users/**").hasAnyRole("CUSTOMER", "ADMIN")
                .requestMatchers("/api/customer/products/**").hasAnyRole("CUSTOMER", "ADMIN")
                
                // Vendor endpoints - VENDOR or ADMIN role
                .requestMatchers("/api/vendor/users/email").hasAnyRole("VENDOR", "ADMIN")
                .requestMatchers("/api/vendor/shops/**").hasAnyRole("VENDOR", "ADMIN")
                .requestMatchers("/api/vendor/products/**").hasAnyRole("VENDOR", "ADMIN")
                
                // Delivery endpoints - DELIVERY or ADMIN role
                .requestMatchers("/api/delivery/**").hasAnyRole("DELIVERY", "ADMIN")
                
                // Order endpoints - role-based access with ADMIN override
                .requestMatchers("/api/orders/*/status").hasAnyRole("DELIVERY", "ADMIN")
                .requestMatchers("/api/orders/*/verify-otp").hasAnyRole("DELIVERY", "ADMIN")
                
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
