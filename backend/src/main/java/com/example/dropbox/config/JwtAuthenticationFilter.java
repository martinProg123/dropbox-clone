package com.example.dropbox.config;
import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.example.dropbox.service.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final JwtUtil jwtUtil;

    @Autowired
    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, java.io.IOException {
        
        log.debug("Processing request: {} {}", request.getMethod(), request.getRequestURI());
        
        String jwt = extractJwtFromCookie(request);
        log.debug("Extracted JWT from cookie: {}", jwt != null ? "present" : "null");
        
        if (jwt != null) {
            var claims = jwtUtil.validateToken(jwt);
            log.debug("JWT validation result: {}", claims != null ? "success" : "failed");
            
            if (claims != null) {
                String email = claims.getSubject();
                log.debug("Authenticated user: {}", email);
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        email, null, Collections.emptyList());
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        } else {
            log.debug("No JWT cookie found in request");
        }
        
        filterChain.doFilter(request, response);
    }
    
    private String extractJwtFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            log.debug("Cookies in request: {}", cookies.length);
            for (Cookie cookie : cookies) {
                String valuePreview = cookie.getValue() != null && cookie.getValue().length() > 20 
                    ? cookie.getValue().substring(0, 20) + "..." 
                    : cookie.getValue();
                log.debug("  Cookie: {} = {}", cookie.getName(), valuePreview);
                if ("jwt".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}