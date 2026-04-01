package com.netpoint.main.filters;

import com.netpoint.main.services.JwtService;
import java.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.ArrayList;



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
        // 1. read the header
        String authHeader = request.getHeader("Authorization");



// 2
// . if missing or doesn't start with Bearer, skip
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;  // stop here, don't continue
        }

// 3. strip "Bearer " to get raw token
        String token = authHeader.substring(7);

// 4. validate the token
        if (jwtService.isTokenValid(token)) {

            // 5. extract the userId
            String userId = jwtService.extractUserId(token);

            // 6. build the authentication object
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userId, null, new ArrayList<>());

            // 7. set it in the security context
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

// 8. always continue the chain
        filterChain.doFilter(request, response);

    }
}


