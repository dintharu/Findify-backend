//package edu.icet.ecom.config;
//
//import edu.icet.ecom.security.JwtTokenUtil;
//import lombok.RequiredArgsConstructor;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//import org.springframework.web.cors.CorsConfigurationSource;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import java.io.IOException;
//
//@Configuration
//@EnableWebSecurity
//@RequiredArgsConstructor
//public class SecurityConfig {
//
//    private final JwtTokenUtil jwtTokenUtil;
//    private final CorsConfigurationSource corsConfigurationSource; // Inject the existing bean
//
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http.cors(cors -> cors.configurationSource(corsConfigurationSource)) // Use injected bean
//                .csrf(csrf -> csrf.disable())
//                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//                .authorizeHttpRequests(authz -> authz
//                        .requestMatchers("/ws/**").permitAll() // WebSocket endpoint
//                        .requestMatchers("/api/chat/health").permitAll() // Health check
//                        .requestMatchers("/actuator/**").permitAll() // Health check endpoints
//                        .anyRequest().authenticated()
//                )
//                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
//
//        return http.build();
//    }
//
//    @Bean
//    public OncePerRequestFilter jwtAuthenticationFilter() {
//        return new OncePerRequestFilter() {
//            @Override
//            protected void doFilterInternal(HttpServletRequest request,
//                                            HttpServletResponse response,
//                                            FilterChain filterChain) throws ServletException, IOException {
//
//                String authHeader = request.getHeader("Authorization");
//
//                if (authHeader != null && authHeader.startsWith("Bearer ")) {
//                    String token = authHeader.substring(7);
//
//                    try {
//                        if (jwtTokenUtil.validateToken(token)) {
//                            SecurityContextHolder.getContext().setAuthentication(
//                                    jwtTokenUtil.getAuthentication(token));
//                        }
//                    } catch (Exception e) {
//                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//                        return;
//                    }
//                }
//
//                filterChain.doFilter(request, response);
//            }
//        };
//    }
//
//    // Remove the duplicate corsConfigurationSource bean method
//    // It's already defined in CorsConfig.java
//}

package edu.icet.ecom.config;

import edu.icet.ecom.security.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final JwtTokenUtil jwtTokenUtil;
    private final CorsConfigurationSource corsConfigurationSource;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        log.info("Configuring Security Filter Chain...");

        http.cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/ws/**").permitAll() // WebSocket endpoint
                        .requestMatchers("/api/chat/health").permitAll() // Health check
                        .requestMatchers("/api/chat/test-auth").permitAll() // Temporary test endpoint
                        .requestMatchers("/actuator/**").permitAll() // Health check endpoints
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // Allow preflight requests
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        log.info("Security Filter Chain configured successfully");
        return http.build();
    }

    @Bean
    public OncePerRequestFilter jwtAuthenticationFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain filterChain) throws ServletException, IOException {

                String requestURI = request.getRequestURI();
                String method = request.getMethod();

                log.info("=== JWT FILTER - Processing Request ===");
                log.info("Request URI: {} {}", method, requestURI);

                // Skip JWT processing for public endpoints
                if (isPublicEndpoint(requestURI, method)) {
                    log.info("Public endpoint detected, skipping JWT validation");
                    filterChain.doFilter(request, response);
                    return;
                }

                String authHeader = request.getHeader("Authorization");
                log.info("Authorization header present: {}", authHeader != null);
                log.info("Authorization header value: {}", authHeader);

                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    String token = authHeader.substring(7);
                    log.info("JWT token extracted (length: {}): {}...", token.length(),
                            token.length() > 20 ? token.substring(0, 20) : token);

                    try {
                        boolean isValid = jwtTokenUtil.validateToken(token);
                        log.info("JWT token validation result: {}", isValid);

                        if (isValid) {
                            Authentication authentication = jwtTokenUtil.getAuthentication(token);
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                            log.info("Authentication set for user: {}", authentication.getName());
                            log.info("User authorities: {}", authentication.getAuthorities());
                        } else {
                            log.warn("JWT token validation failed");
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.getWriter().write("{\"error\":\"Invalid JWT token\"}");
                            response.setContentType("application/json");
                            return;
                        }
                    } catch (Exception e) {
                        log.error("JWT processing exception: {}", e.getMessage(), e);
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.getWriter().write("{\"error\":\"JWT processing failed: " + e.getMessage() + "\"}");
                        response.setContentType("application/json");
                        return;
                    }
                } else {
                    log.warn("No valid Authorization header found for protected endpoint");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("{\"error\":\"Missing or invalid Authorization header\"}");
                    response.setContentType("application/json");
                    return;
                }

                log.info("JWT filter completed successfully, proceeding to next filter");
                filterChain.doFilter(request, response);
            }

            private boolean isPublicEndpoint(String uri, String method) {
                return uri.equals("/api/chat/health") ||
                        uri.equals("/api/chat/test-auth") ||
                        uri.startsWith("/ws/") ||
                        uri.startsWith("/actuator/") ||
                        "OPTIONS".equals(method);
            }
        };
    }
}