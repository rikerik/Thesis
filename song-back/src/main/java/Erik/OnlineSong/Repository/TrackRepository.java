package Erik.OnlineSong.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import Erik.OnlineSong.Model.Track;

@Repository
public interface TrackRepository extends JpaRepository<Track, Integer> {

    List<Track> findByPlaylistId(Integer playlistId);
}
