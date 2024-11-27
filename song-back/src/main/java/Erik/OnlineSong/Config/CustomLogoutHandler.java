package Erik.OnlineSong.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;

import Erik.OnlineSong.Model.Token;
import Erik.OnlineSong.Repository.TokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class CustomLogoutHandler implements LogoutHandler {

    private final TokenRepository tokenRepository;

    public CustomLogoutHandler(TokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    @Override
    public void logout(HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) {
        // Get the Authorization header from the request
        String authHeader = request.getHeader("Authorization");

        // Check if the Authorization header is present and starts with "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return; // Exit if not
        }

        // Substring to remove "Bearer " from the token
        String token = authHeader.substring(7);

        // Retrieve the Token object from the database using the token string
        Token storedToken = tokenRepository.findByToken(token).orElse(null);

        // Retrieve the Token object from the database using the token string
        if (token != null) {
            // Mark the token as logged out
            storedToken.setLoggedOut(true);
            // Save the updated token status in the repository
            tokenRepository.save(storedToken);
        }
    }

}
