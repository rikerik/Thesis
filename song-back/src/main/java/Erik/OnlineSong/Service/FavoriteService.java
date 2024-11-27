package Erik.OnlineSong.Service;

import Erik.OnlineSong.Model.Favorite;
import Erik.OnlineSong.Model.Track;
import Erik.OnlineSong.Model.User;
import Erik.OnlineSong.Repository.FavoriteRepository;
import Erik.OnlineSong.Repository.TrackRepository;
import Erik.OnlineSong.Repository.UserRepository;
import io.jsonwebtoken.lang.Collections;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import com.amazonaws.services.s3.AmazonS3;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final TrackRepository trackRepository;
    private final AmazonS3 s3Client;

    public FavoriteService(FavoriteRepository favoriteRepository, UserRepository userRepository,
            TrackRepository trackRepository, AmazonS3 s3Client) {
        this.favoriteRepository = favoriteRepository;
        this.userRepository = userRepository;
        this.trackRepository = trackRepository;
        this.s3Client = s3Client;
    }

    public boolean updateFavoriteStatus(Integer userId, Integer songId, boolean isFavorited) {
        log.info("Updating favorite status: userId={}, songId={}, isFavorited={}", userId, songId, isFavorited);

        try {
            // Fetch User and Track from the database
            Optional<User> userOptional = userRepository.findById(userId);
            Optional<Track> trackOptional = trackRepository.findById(songId);

            if (userOptional.isEmpty() || trackOptional.isEmpty()) {
                log.error("User or Track not found: userId={}, songId={}", userId, songId);
                return false;
            }

            User user = userOptional.get();
            Track track = trackOptional.get();

            // Check if the Favorite entry exists
            Optional<Favorite> favoriteOptional = favoriteRepository.findByUserAndTrack(user, track);
            log.info("Adding or removing favorited item");
            if (!favoriteOptional.isPresent()) {
                // Add track to favorites if it's not already there
                Favorite favorite = Favorite.builder()
                        .track(track)
                        .user(user)
                        .build();
                favoriteRepository.save(favorite);
                log.info("Track added to favorites: userId={}, songId={}", userId, songId);
            } else {
                // Remove track from favorites if it's currently there
                Favorite favorite = favoriteOptional.get();
                favoriteRepository.delete(favorite);
                log.info("Track removed from favorites: userId={}, songId={}", userId, songId);
            }

            return true;

        } catch (Exception e) {
            log.error("Error updating favorite status: userId={}, songId={}, isFavorited={}, error={}", userId, songId,
                    isFavorited, e.getMessage(), e);
            return false;
        }
    }

    public List<Track> getFavoriteTracks(Integer userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            List<Favorite> favorites = favoriteRepository.findByUser(user);

            return favorites.stream()
                    .map(favorite -> {
                        Track track = favorite.getTrack();
                        track.encodeImageToBase64(s3Client);
                        return track;
                    })
                    .collect(Collectors.toList());

        }
        return Collections.emptyList();
    }

    public List<Integer> getSavedTracks(Integer userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            List<Favorite> favorites = favoriteRepository.findByUser(user);

            return favorites.stream()
                    .map(favorite -> {
                        Track track = favorite.getTrack();
                        return track.getId();
                    })
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
