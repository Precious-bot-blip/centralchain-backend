package com.b2b.inventorysystem.config;

import com.b2b.inventorysystem.security.JwtAuthFilter;
import com.b2b.inventorysystem.security.RestAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final RestAuthenticationEntryPoint authEntryPoint;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter,
                          RestAuthenticationEntryPoint authEntryPoint) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.authEntryPoint = authEntryPoint;
        System.out.println(">>> Custom SecurityConfig loaded! <<<");
    }
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(ex -> ex.authenticationEntryPoint(authEntryPoint))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        // PUBLIC — auth endpoints
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**"
                        ).permitAll()
                        .requestMatchers("/ws/**").permitAll()

                        // USERS
                        .requestMatchers(HttpMethod.GET, "/api/users/**")
                        .hasAnyRole("ADMIN", "WAREHOUSE_MANAGER", "SUPPLIER", "DRIVER")
                        .requestMatchers(HttpMethod.POST, "/api/users/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/users/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/users/**").hasRole("ADMIN")

                        // INVENTORY
                        .requestMatchers(HttpMethod.GET, "/api/inventory/**")
                        .hasAnyRole("ADMIN", "WAREHOUSE_MANAGER", "SUPPLIER")
                        .requestMatchers(HttpMethod.POST, "/api/inventory/**")
                        .hasAnyRole("ADMIN", "WAREHOUSE_MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/api/inventory/**")
                        .hasAnyRole("ADMIN", "WAREHOUSE_MANAGER")
                        .requestMatchers(HttpMethod.PATCH, "/api/inventory/**")
                        .hasAnyRole("ADMIN", "WAREHOUSE_MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/api/inventory/**")
                        .hasAnyRole("ADMIN", "WAREHOUSE_MANAGER")

                        // PURCHASE ORDERS
                        .requestMatchers(HttpMethod.GET, "/api/purchase-orders/**")
                        .hasAnyRole("ADMIN", "WAREHOUSE_MANAGER", "SUPPLIER", "DRIVER")

                        .requestMatchers(HttpMethod.POST, "/api/purchase-orders")
                        .hasAnyRole("ADMIN", "WAREHOUSE_MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/api/purchase-orders/**")
                        .hasAnyRole("ADMIN", "WAREHOUSE_MANAGER")

                        // Supplier accept — SUPPLIER only
                        .requestMatchers(HttpMethod.PATCH, "/api/purchase-orders/*/accept").hasRole("SUPPLIER")

                        // Driver deliver — DRIVER only
                        .requestMatchers(HttpMethod.PATCH, "/api/purchase-orders/*/deliver").hasRole("DRIVER")

                        // Admin/Manager transitions
                        .requestMatchers(HttpMethod.PATCH, "/api/purchase-orders/*/approve")
                        .hasAnyRole("ADMIN", "WAREHOUSE_MANAGER")
                        .requestMatchers(HttpMethod.PATCH, "/api/purchase-orders/*/dispatch")
                        .hasAnyRole("ADMIN", "WAREHOUSE_MANAGER")
                        .requestMatchers(HttpMethod.PATCH, "/api/purchase-orders/*/cancel")
                        .hasAnyRole("ADMIN", "WAREHOUSE_MANAGER")

                        .requestMatchers(HttpMethod.DELETE, "/api/purchase-orders/**")
                        .hasAnyRole("ADMIN", "WAREHOUSE_MANAGER")

                        // DELIVERIES
                        .requestMatchers(HttpMethod.GET, "/api/deliveries/**")
                        .hasAnyRole("ADMIN", "WAREHOUSE_MANAGER", "SUPPLIER", "DRIVER")
                        .requestMatchers(HttpMethod.PATCH, "/api/deliveries/**")
                        .hasAnyRole("ADMIN", "DRIVER")

                        // MESSAGES
                        .requestMatchers("/api/messages/**")
                        .hasAnyRole("ADMIN", "WAREHOUSE_MANAGER", "SUPPLIER", "DRIVER")

                        // DEFAULT
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}