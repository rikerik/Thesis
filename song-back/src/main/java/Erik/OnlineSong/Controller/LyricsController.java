package Erik.OnlineSong.Controller;

import org.springframework.web.bind.annotation.RestController;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;

import Erik.OnlineSong.Model.Track;
import Erik.OnlineSong.Repository.TrackRepository;
import Erik.OnlineSong.Service.LyricsService;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.util.Date;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@RestController
@RequestMapping("/lyrics")
public class LyricsController {

    private final LyricsService lService;
    private final AmazonS3 s3Client;
    private final TrackRepository repository;

    public LyricsController(LyricsService lService, AmazonS3 s3Client, TrackRepository repository) {
        this.lService = lService;
        this.s3Client = s3Client;
        this.repository = repository;
    }

    // Endpoint to get lyrics for a given track ID
    @GetMapping("/getLyrics")
    public ResponseEntity<String> getLyrics(@RequestParam String id) {
        // Check if the track ID is provided
        if (id == null || id.isEmpty()) {
            return ResponseEntity.badRequest().body("Track ID is missing");
        }

        try {
            // Fetch the track from the repository
            Optional<Track> optionalTrack = repository.findById(Integer.valueOf(id));

            if (optionalTrack.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Track track = optionalTrack.get();

            // Set the expiration time for the presigned URL
            Date expiration = new Date();
            long expTimeMillis = expiration.getTime();
            expTimeMillis += 1000 * 60; // Add 1 minute
            expiration.setTime(expTimeMillis);

            // Generate presigned URL request for the track location in S3
            GeneratePresignedUrlRequest urlRequest = new GeneratePresignedUrlRequest("tunewave", track.getLocation())
                    .withMethod(HttpMethod.GET)
                    .withExpiration(expiration); // Set expiration time for the URL

            // Generate the presigned URL
            URL url = s3Client.generatePresignedUrl(urlRequest);

            // Fetch lyrics using the generated presigned URL
            String lyrics = lService.getLyrics(url.toString());

            return ResponseEntity.ok().body(lyrics);
        } catch (Exception e) {
            log.error("Error while fetching lyrics: ", e);
            return ResponseEntity.internalServerError().body("Error while fetching lyrics");
        }
    }

}
