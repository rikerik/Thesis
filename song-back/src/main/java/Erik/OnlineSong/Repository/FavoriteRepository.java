package Erik.OnlineSong.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import Erik.OnlineSong.Model.Favorite;
import Erik.OnlineSong.Model.Track;
import Erik.OnlineSong.Model.User;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Integer> {
    Optional<Favorite> findByUserAndTrack(User user, Track track);

    List<Favorite> findByUser(User user);

}
