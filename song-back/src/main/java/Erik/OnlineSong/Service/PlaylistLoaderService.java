package Erik.OnlineSong.Service;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.TagException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import Erik.OnlineSong.Model.Playlist;
import Erik.OnlineSong.Model.Track;
import Erik.OnlineSong.Repository.PlaylistRepository;
import Erik.OnlineSong.Repository.TrackRepository;
import lombok.extern.slf4j.Slf4j;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3Object;

@Slf4j
@Service
public class PlaylistLoaderService {

    private final PlaylistRepository playlistRepository;
    private final TrackRepository trackRepository;
    private final AmazonS3 s3Client;

    // Providing the bucket name and the subfolder where tracks and other files are
    // stored
    @Value("${aws.s3.bucket.name}")
    private String bucketName;

    @Value("${aws.s3.bucket.music-folder}")
    private String musicFolderPath;

    public PlaylistLoaderService(PlaylistRepository playlistRepository, TrackRepository trackRepository,
            AmazonS3 s3Client) {
        this.playlistRepository = playlistRepository;
        this.trackRepository = trackRepository;
        this.s3Client = s3Client;
    }

    public void loadPlaylists() throws IOException, TagException, ReadOnlyFileException, InvalidAudioFrameException {
        log.info("Loading playlists from bucket: {}", bucketName);

        // Create a request to list all directories (playlists) in the S3 bucket
        ListObjectsV2Request listObjectsRequest = new ListObjectsV2Request()
                .withBucketName(bucketName)
                .withPrefix(musicFolderPath + "/")
                .withDelimiter("/");

        // Execute the request and retrieve the result
        ListObjectsV2Result result = s3Client.listObjectsV2(listObjectsRequest);
        // Extract the common prefixes (directories) from the result
        List<String> playlistFolders = result.getCommonPrefixes().stream()
                .collect(Collectors.toList());

        // If no playlist directories are found, log a warning and exit
        if (playlistFolders.isEmpty()) {
            log.warn("No playlist directories found in bucket path: {}", musicFolderPath);
            return;
        }

        // Iterate over each playlist directory
        for (String playlistFolder : playlistFolders) {
            log.info("Processing playlist folder: {}", playlistFolder);

            // Ensure the playlistFolder ends with a slash before processing
            if (!playlistFolder.endsWith("/")) {
                playlistFolder += "/";
            }

            // Final variable to use inside the lambda
            final String finalPlaylistFolder = playlistFolder;

            // Extract the playlist title (folder name)
            String playlistTitle = playlistFolder.substring(
                    playlistFolder.lastIndexOf('/', playlistFolder.length() - 2) + 1, playlistFolder.length() - 1);

            // Construct the correct key for the playlist info file
            String playlistInfoFile = playlistFolder + playlistTitle + ".txt";
            playlistInfoFile = playlistInfoFile.replaceAll("//+", "/").replaceFirst("^/+", ""); // Normalize the path

            log.debug("Trying to access playlist info file: {}", playlistInfoFile);

            try {
                // Retrieve the playlist info file from S3
                S3Object playlistInfoObject = s3Client.getObject(new GetObjectRequest(bucketName, playlistInfoFile));
                // Read the content of the playlist info file
                List<String> lines = new BufferedReader(new InputStreamReader(playlistInfoObject.getObjectContent()))
                        .lines().collect(Collectors.toList());

                // Check if the playlist info file is complete
                if (lines.size() < 2) {
                    log.warn("Playlist info file is incomplete: {}", playlistInfoFile);
                    continue;
                }

                String description = lines.get(0);
                String imageUrl = lines.get(1);

                // Save playlist with the correct title
                Playlist playlist = Playlist.builder()
                        .title(playlistTitle)
                        .description(description)
                        .image(imageUrl)
                        .build();

                playlist = playlistRepository.save(playlist);
                log.info("Saved playlist: {} with ID: {}", playlist.getTitle(), playlist.getId());

                // Load tracks in this playlist folder
                ListObjectsV2Request trackListRequest = new ListObjectsV2Request()
                        .withBucketName(bucketName)
                        .withPrefix(playlistFolder);
                ListObjectsV2Result trackResult = s3Client.listObjectsV2(trackListRequest);

                // Extract track files (MP3 files) from the result
                List<String> trackFiles = trackResult.getObjectSummaries().stream()
                        .map(s -> s.getKey())
                        .filter(key -> key.endsWith(".mp3"))
                        .collect(Collectors.toList());

                // If no track files are found, log a warning and continue to the next playlist
                // folder
                if (trackFiles.isEmpty()) {
                    log.warn("No tracks found in playlist folder: {}", playlistFolder);
                    continue;
                }

                // Iterate over each track file
                for (String trackFile : trackFiles) {
                    // Retrieve the track file from S3
                    S3Object trackS3Object = s3Client.getObject(new GetObjectRequest(bucketName, trackFile));
                    InputStream trackInputStream = trackS3Object.getObjectContent();

                    // Extract track name and artist from the file name
                    String fileNameWithoutExtension = trackFile.substring(trackFile.lastIndexOf('/') + 1,
                            trackFile.lastIndexOf('.'));
                    String[] parts = fileNameWithoutExtension.split(" - ", 2);

                    // Create a temporary file to store the track
                    Path tempFilePath = Files.createTempFile("track-", ".mp3");
                    try (BufferedInputStream bufferedInputStream = new BufferedInputStream(trackInputStream);
                            FileOutputStream fileOutputStream = new FileOutputStream(tempFilePath.toFile());
                            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream)) {

                        // Create a temporary file to store the track
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = bufferedInputStream.read(buffer)) != -1) {
                            bufferedOutputStream.write(buffer, 0, bytesRead);
                        }
                    }

                    // Ensure the file name format is correct
                    if (parts.length < 2) {
                        log.warn("File name format is incorrect: {}", fileNameWithoutExtension);
                        continue;
                    }

                    String artist = parts[0];
                    String title = parts[1];

                    // Search for an image file with the same name
                    String imageLocation = null;
                    List<String> imageFiles = s3Client.listObjectsV2(new ListObjectsV2Request()
                            .withBucketName(bucketName)
                            .withPrefix(finalPlaylistFolder))
                            .getObjectSummaries().stream()
                            .map(s -> s.getKey())
                            .filter(key -> key.equals(finalPlaylistFolder + fileNameWithoutExtension + ".jpg") ||
                                    key.equals(finalPlaylistFolder + fileNameWithoutExtension + ".png"))
                            .collect(Collectors.toList());

                    if (!imageFiles.isEmpty()) {
                        imageLocation = imageFiles.get(0); // Get the first matching image
                        log.info("Found image for track: {} at {}", trackFile, imageLocation);
                    } else {
                        log.info("No image found for track: {}", trackFile);
                    }

                    log.info("Processing track file: {}", trackFile);

                    // Using jaudiotagger to get length
                    MP3File mp3File = new MP3File(tempFilePath.toFile());
                    Track track = Track.builder()
                            .title(title)
                            .artist(artist)
                            .location(trackFile)
                            .playlist(playlist)
                            .image(imageLocation)
                            .length((double) mp3File.getAudioHeader().getTrackLength())
                            .build();

                    trackRepository.save(track);
                    log.info("Saved track: {} by artist: {} for playlist: {}", track.getTitle(), track.getArtist(),
                            playlist.getTitle());
                }
            } catch (AmazonS3Exception e) {
                log.error("Error accessing S3 object: {}", e.getMessage());
            }
        }
        log.info("Completed loading playlists from bucket path: {}", musicFolderPath);
    }
}
