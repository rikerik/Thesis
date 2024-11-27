package Erik.OnlineSong.Service;

import java.util.Date;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import Erik.OnlineSong.Model.User;
import Erik.OnlineSong.Repository.TokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    // Secret key used for signing and verifying JWTs
    private static final String SECRET_KEY = "2bdcd096b8e5997403cc854c7894161f2fd24d38139c3417f4b3d28c7ed47557";

    private final TokenRepository tokenRepository;

    public JwtService(TokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    // Extracts the username (subject) from the token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Extracts the user ID from the token's claims
    public Integer extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("UserId", Integer.class));
    }

    // Validates the token by checking:
    // 1. Username matches the provided user details.
    // 2. The token is not expired.
    // 3. The token is not marked as logged out in the database.
    public boolean isValid(String token, UserDetails user) {
        String username = extractUsername(token);

        boolean isValidToken = tokenRepository.findByToken(token)
                .map(t -> !t.isLoggedOut()).orElse(false);

        return username.equals(user.getUsername()) && !isTokenExpired(token) && isValidToken;
    }

    // Checks if the token is expired by comparing its expiration date with the
    // current date
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // Extracts the expiration date from the token
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Extracts a specific claim from the token using a resolver function
    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = extractAllClaims(token);
        return resolver.apply(claims);
    }

    // Parses and retrieves all claims from the token
    private Claims extractAllClaims(String token) {
        return Jwts
                .parser() // Creates a new JWT parser
                .verifyWith(getSigningKey()) // Sets the signing key to verify the token
                .build() // Builds the parser
                .parseSignedClaims(token) // Parses the signed token and retrieves claims
                .getPayload(); // Returns the payload (claims)
    }

    // Generates a new token for the given user with:
    // - Subject set to the user's username
    // - A custom claim for the user's ID
    // - Issued and expiration timestamps
    public String generateToken(User user) {
        return Jwts.builder()
                .subject(user.getUsername()) // Sets the token's subject
                .claim("UserId", user.getId()) // Adds a custom claim for user ID
                .issuedAt(new Date(System.currentTimeMillis())) // Sets the issued-at timestamp
                .expiration(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000)) // Sets expiration to 24 hours
                .signWith(getSigningKey()) // Signs the token with the secret key
                .compact(); // Builds and returns the compact JWT string
    }

    // Returns the signing key used to sign and verify JWTs
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64URL.decode(SECRET_KEY); // Decodes the Base64URL-encoded secret key
        return Keys.hmacShaKeyFor(keyBytes); // Creates an HMAC-SHA key from the decoded bytes
    }

}
