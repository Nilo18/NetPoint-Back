package com.netpoint.main.filters;

import com.netpoint.main.services.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1. Read the header
        String authHeader = request.getHeader("Authorization");

        // 2. If missing or doesn't start with Bearer, move to the next filter
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Strip "Bearer " to get the raw token
        String token = authHeader.substring(7);

        // 4. Validate the token and ensure the user isn't already authenticated
        if (jwtService.isTokenValid(token) && SecurityContextHolder.getContext().getAuthentication() == null) {

            // 5. Extract userId and Role from the JWT
            String userId = jwtService.extractUserId(token);
            String role = jwtService.extractRole(token);

            // 6. Map the role string (e.g., "ADMIN") to a GrantedAuthority
            // This is critical because your SecurityConfig uses .hasAnyAuthority()
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userId,
                    null,
                    List.of(new SimpleGrantedAuthority(role))
            );

            // 7. Set it in the security context
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // 8. Always continue the chain
        filterChain.doFilter(request, response);
    }
}