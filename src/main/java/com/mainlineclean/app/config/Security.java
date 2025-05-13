package com.mainlineclean.app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

@Configuration
@EnableWebSecurity
public class Security {

    @Value("${client.origin}")
    private String allowedOrigin;

    private final HMacAuthenticationFilter hMacAuthenticationFilter;

    public Security(HMacAuthenticationFilter hMacAuthenticationFilter) {
        this.hMacAuthenticationFilter = hMacAuthenticationFilter;
    }

    @Bean
    @Order(1)
    public SecurityFilterChain basicAuthSecurityFilterChain(org.springframework.security.config.annotation.web.builders.HttpSecurity http) throws Exception {
        return http
                .cors(Customizer.withDefaults())
                .securityMatcher(new AntPathRequestMatcher("/verify-code", "POST"))
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(AbstractHttpConfigurer::disable)
                .build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(org.springframework.security.config.annotation.web.builders.HttpSecurity http) throws Exception {
        return http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(hMacAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET,  "/token").permitAll()
                        .requestMatchers(HttpMethod.GET,  "/verify-code").permitAll()
                        .requestMatchers(HttpMethod.GET,    "/authenticate").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT,    "/update-admin-pricing").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT,    "/update-admin-email").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST,   "/client").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/clients").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST,   "/clients/email").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET,    "/clients").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST,   "/cancel-appointment").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET,    "/paypal-info").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST,   "/availability").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET,    "/appointments").hasRole("ADMIN")
                        // all other requests need at least a valid token
                        .anyRequest().authenticated()
                )
                .httpBasic(AbstractHttpConfigurer::disable)
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowCredentials(true);
        configuration.setAllowedOrigins(Collections.singletonList(allowedOrigin));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Collections.singletonList(CorsConfiguration.ALL));
        configuration.setExposedHeaders(Arrays.asList(CorsConfiguration.ALL, HttpHeaders.AUTHORIZATION));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
