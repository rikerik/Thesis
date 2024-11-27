package Erik.OnlineSong.Service;

import java.util.List;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import Erik.OnlineSong.Model.AuthenticationResponse;
import Erik.OnlineSong.Model.Token;
import Erik.OnlineSong.Model.User;
import Erik.OnlineSong.Repository.TokenRepository;
import Erik.OnlineSong.Repository.UserRepository;

@Service
public class AuthenticationService {

    private final UserRepository repository;
    private final PasswordEncoder pwdEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final TokenRepository tokenRepository;

    public AuthenticationService(UserRepository repository,
            PasswordEncoder pwdEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager,
            TokenRepository tokenRepository) {
        this.repository = repository;
        this.pwdEncoder = pwdEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.tokenRepository = tokenRepository;
    }

    // Registers a new user and generates a JWT token
    public AuthenticationResponse register(User request) {
        User user = new User();
        // Set user details and encode the password
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setUsername(request.getUsername());
        user.setPassword(pwdEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());

        user.setRole(request.getRole());
        // Save the user in the database
        user = repository.save(user);
        // Generate a JWT token for the user
        String jwt = jwtService.generateToken(user);

        // save generated token
        saveUserToken(user, jwt);
        // Return the response with the generated token
        return new AuthenticationResponse(jwt);

    }

    // Authenticates a user and generates a new JWT token
    public AuthenticationResponse authenticate(User request) {
        // Authenticate the user using username and password
        authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()));
        // Retrieve the authenticated user from the database
        User user = repository.findByUsername(request.getUsername()).orElseThrow();
        // Generate a new token for the user
        String token = jwtService.generateToken(user);

        // Revoke all existing tokens for the user
        revokeAllTokensByUser(user);

        // save generated token
        saveUserToken(user, token);

        return new AuthenticationResponse(token);
    }

    // Saves a token for a user in the database
    private void saveUserToken(User user, String jwt) {
        Token token = new Token();
        token.setToken(jwt);
        token.setLoggedOut(false); // Mark as active
        token.setUser(user);
        tokenRepository.save(token);
    }

    // Revokes all active tokens for a user
    private void revokeAllTokensByUser(User user) {
        // Fetch all tokens for the user
        List<Token> validTokenListByUser = tokenRepository.findAllTokenByUser(user.getId());
        // Mark each token as logged out
        if (!validTokenListByUser.isEmpty()) {
            validTokenListByUser.forEach(t -> {
                t.setLoggedOut(true);
            });
        }
        // Save updated tokens
        tokenRepository.saveAll(validTokenListByUser);
    }

}
