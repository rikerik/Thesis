package Erik.OnlineSong.Filter;

import java.io.IOException;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import Erik.OnlineSong.Service.JwtService;
import Erik.OnlineSong.Service.UserDetailsServiceImp;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
// Custom filter which checks every HTTP request for a valid JWT in the
// Authorization header
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsServiceImp userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsServiceImp userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    // Method that processes the filter
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        // Extracting the Authorization header
        String authHeader = request.getHeader("Authorization");

        // Check if the header is present and starts with "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // If not, continue the filter chain without authentication
            filterChain.doFilter(request, response);
            return;
        }

        // Substring to remove "Bearer " from the token
        String token = authHeader.substring(7);
        String username = jwtService.extractUsername(token);

        // Check if username is not null and no authentication is set in the context
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // Load user details using the username
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // Validate the token with the user details
            if (jwtService.isValid(token, userDetails)) {
                // Create an authentication token with the user details and authorities
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails,
                        null, userDetails.getAuthorities());

                // Sets details on the authorization token
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                // Set the authentication in the security context
                SecurityContextHolder.getContext().setAuthentication(authToken);

            }
        }
        // Continues the filterchain, allowing the request to proceed
        filterChain.doFilter(request, response);
    }

}
