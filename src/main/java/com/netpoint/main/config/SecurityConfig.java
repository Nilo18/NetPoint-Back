package com.netpoint.main.config;

import com.netpoint.main.filters.JwtAuthFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
//import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    //ეს მოკლედ ყველა მოთხოვნას აჩერებსავით, რომ ტოკენის ვალიდურობა შეამოწმოს
    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }


    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(8);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    //ეს არის იმენა ბელადი ფილტრი რა, ანუ ყველა HTTP უსაფრთხოების წესებს აქ უკეთებს DEFINE-ს
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. Disable CSRF for Postman/REST testing
                .csrf(csrf -> csrf.disable())

                // 2. Configure CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 3. Make the session stateless (standard for JWT)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
//                .securityMatcher(AntPathRequestMatcher.antMatcher("/**"))
                // 4. Set up Authorization Rules
                .authorizeHttpRequests(auth -> auth
                        // Allow login and registration without a token
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/error", "/error/**").permitAll()
                        .requestMatchers("/css/**", "/images/**", "/*.html").permitAll()
                                .requestMatchers("/welcome", "/api/demo/**").permitAll()
                                // Use hasAnyAuthority because your DB stores "ADMIN" instead of "ROLE_ADMIN"
                        .requestMatchers("/admin/**").hasAnyAuthority("ADMIN", "OWNER")

                        // Everything else requires a valid JWT
//                        .anyRequest().permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/settings/**").hasAuthority("OWNER")
                        .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                        .requestMatchers("/actuator/**").hasAnyAuthority("ADMIN", "OWNER")
                        .anyRequest().authenticated()

                )

                // 5. Add your JWT Filter before the standard Username/Password filter
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) ->
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED))
                );

        return http.build();
    }

    // ეს აკონტროლებს რა ჰედერები/მეთოდები/ორიჯინებია დაშვებული(ანუ cookies, HTTP methodebi, angularis motxovnebs da mageebs)
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:4200")); // Your Angular URL
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring()
                .requestMatchers("/error", "/error/**", "/css/**", "/images/**", "/*.html");
    }
}