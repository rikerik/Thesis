package Erik.OnlineSong.Controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import Erik.OnlineSong.DTO.ListeningDataDTO;
import Erik.OnlineSong.Service.ListeningDataService;

@RestController
@RequestMapping("/listening")
public class ListeningDataController {

    private ListeningDataService listeningDataService;

    public ListeningDataController(ListeningDataService listeningDataService) {
        this.listeningDataService = listeningDataService;
    }

    // Endpoint to send listening data
    @PostMapping("/statistics")
    public ResponseEntity<Void> sendListeningData(@RequestBody ListeningDataDTO listeningDataDTO) {
        // Call the service to handle the listening data
        listeningDataService.processListeningData(listeningDataDTO);
        return ResponseEntity.ok().build();
    }

    // Endpoint to get total listening time for a user for the current week
    @GetMapping("/users/{userId}/listening-time/week")
    public int getTotalListeningTimeForWeek(@PathVariable Integer userId) {
        return listeningDataService.getTotalListeningMinutesForWeek(userId);
    }

    // Endpoint to get favorite artists for a user
    @GetMapping("/users/{userId}/favorite-artists")
    public List<Object[]> getFavoriteArtists(@PathVariable Integer userId) {
        return listeningDataService.getFavoriteArtistsByUserId(userId);
    }

}
