package com.example.geoserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.bedatadriven.jackson.datatype.jts.JtsModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.locationtech.jts.geom.Point;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

                http
                        .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Ajouter cette
                                                                                                // ligne
                        .csrf(csrf -> csrf.disable())
                        .formLogin(form -> form.disable())
                        .httpBasic(basic -> basic.disable())

                        .sessionManagement(session -> session
                                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                        .authorizeHttpRequests(auth -> auth
                                        // Swagger
                                        .requestMatchers(
                                                        "/swagger-ui.html",
                                                        "/swagger-ui/**",
                                                        "/v3/api-docs/**")
                                        .permitAll()

                                        // Auth API
                                        .requestMatchers("/api/signalements/**").permitAll()
                                        .requestMatchers("/api/statuts/**").permitAll()
                                        .requestMatchers("/api/types-signalement/**").permitAll()

                                        // Tout le reste sécurisé
                                        .anyRequest().authenticated());

                return http.build();
        }

        @Bean
        public ObjectMapper objectMapper() {
                ObjectMapper mapper = new ObjectMapper();
                SimpleModule module = new SimpleModule();
                module.addDeserializer(Point.class, new PointDeserializer());
                mapper.registerModule(module);
                mapper.registerModule(new JtsModule()); // pour la géométrie
                mapper.registerModule(new JavaTimeModule()); // pour LocalDateTime
                mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
                return mapper;
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();

                // Autoriser localhost:5173 (Vite dev server)
                configuration.setAllowedOrigins(Arrays.asList(
                                "http://localhost:5173",
                                "http://localhost:3000",
                                "http://127.0.0.1:5173"));

                // Autoriser les méthodes HTTP
                configuration.setAllowedMethods(Arrays.asList(
                                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

                // Autoriser les headers
                configuration.setAllowedHeaders(Arrays.asList(
                        "Authorization",
                        "Content-Type",
                        "Accept",
                        "Origin",
                        "X-Requested-With",
                        "Access-Control-Request-Method",
                        "Access-Control-Request-Headers"));

                // Autoriser les credentials (cookies, auth headers)
                configuration.setAllowCredentials(true);

                // Max age pour le preflight cache
                configuration.setMaxAge(3600L);

                // Headers exposés au client
                configuration.setExposedHeaders(Arrays.asList(
                                "Access-Control-Allow-Origin",
                                "Access-Control-Allow-Credentials",
                                "Authorization"));

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);

                return source;
        }
}