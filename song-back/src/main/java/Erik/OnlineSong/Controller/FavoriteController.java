package Erik.OnlineSong.Controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import Erik.OnlineSong.DTO.FavoriteRequestDTO;
import Erik.OnlineSong.Model.Track;

import Erik.OnlineSong.Service.FavoriteService;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/favorite")
@Slf4j
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    // Endpoint to update the favorite status of a track for a user
    @PostMapping("/favorites")
    public ResponseEntity<String> updateFavoriteStatus(
            @RequestBody FavoriteRequestDTO favoriteRequest,
            @RequestParam("userId") Integer userId) {
        try {
            log.info("FavoriteRequest: " + favoriteRequest);
            log.info("UserId: " + userId);

            // Call the service to update favorite status and capture the result
            boolean updateSuccess = favoriteService.updateFavoriteStatus(userId, favoriteRequest.getSongId(),
                    favoriteRequest.isFavorited());

            // Check if the update was successful and return appropriate response
            if (updateSuccess) {
                return ResponseEntity.ok("Favorite status updated successfully.");
            } else {
                return ResponseEntity.badRequest().body("Failed to update favorite status.");
            }

        } catch (Exception e) {
            log.error("Error updating favorite status: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while updating the favorite status.");
        }
    }

    // Endpoint to get favorite tracks for a user
    @GetMapping("/favorites/{userId}")
    public ResponseEntity<List<Track>> getFavoriteTracks(@PathVariable Integer userId) {
        List<Track> favoriteTracks = favoriteService.getFavoriteTracks(userId);
        if (!favoriteTracks.isEmpty()) {
            return ResponseEntity.ok(favoriteTracks);
        } else {
            return ResponseEntity.noContent().build();
        }
    }

    // Endpoint to get saved track IDs for a user
    // The saved tracks are the ones which displays heart on the cards
    @GetMapping("/favorites/saved/{userId}")
    public ResponseEntity<List<Integer>> getSaved(@PathVariable Integer userId) {
        return ResponseEntity.ok(favoriteService.getSavedTracks(userId));
    }

}
