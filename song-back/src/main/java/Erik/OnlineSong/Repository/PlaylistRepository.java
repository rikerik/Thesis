package Erik.OnlineSong.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import Erik.OnlineSong.Model.Playlist;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, Integer> {

}
