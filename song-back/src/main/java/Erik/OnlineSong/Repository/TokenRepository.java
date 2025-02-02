package Erik.OnlineSong.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import Erik.OnlineSong.Model.Token;

@Repository
public interface TokenRepository extends JpaRepository<Token, Integer> {

    // Query to retrieve all active (not logged out) tokens for a specific user.
    @Query("""
            Select t from Token t inner join User u
            on t.user.id = u.id
            where t.user.id = :userId and t.loggedOut = false
            """)
    List<Token> findAllTokenByUser(Integer userId);

    // Method to find a specific token by its value
    Optional<Token> findByToken(String token);
}
