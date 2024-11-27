package Erik.OnlineSong.Model;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "track")
// Ignores Hibernate-specific properties to prevent serialization issues.
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class Track {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "title")
    private String title;

    @Column(name = "artist")
    private String artist;

    @Column(name = "length")
    private Double length;

    @Column(name = "location")
    private String location;

    @Column(name = "image")
    private String image;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "playlist_id", nullable = false)
    @JsonBackReference // Prevent infinite recursion
    private Playlist playlist;

    @Transient
    private String base64Image;

    public void encodeImageToBase64(AmazonS3 s3Client) {
        if (image != null && !image.isEmpty()) {
            try {
                // Retrieve the image from S3
                S3Object s3Object = s3Client.getObject(new GetObjectRequest("tunewave", image));
                InputStream inputStream = s3Object.getObjectContent();
                byte[] imageBytes = inputStream.readAllBytes();
                base64Image = "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(imageBytes);
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}