package Erik.OnlineSong.Service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Properties;

import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import Erik.OnlineSong.Model.Transcript;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class LyricsService {

    private static final String KEY; // API key for AssembylAI
    private static final String TRANSCRIPT_API_URL = "https://api.assemblyai.com/v2/transcript";

    // Static block to load the API key from the config.properties file
    static {
        Properties p = new Properties();
        try (InputStream input = LyricsService.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                throw new FileNotFoundException("Config file not found");
            }
            p.load(input);
            KEY = p.getProperty("api.key"); // Get the API key
            if (KEY == null || KEY.isEmpty()) {
                throw new IllegalStateException("API key not found in config file");
            }
        } catch (IOException e) {
            log.error("Error while loading config file: ", e);
            throw new RuntimeException(e);
        }
    }

    // Retrieves lyrics from AssemblyAI for a given audio file URL
    public String getLyrics(String trackUrl) throws Exception {
        // Create a new Transcript object and set the audio URL
        Transcript transcript = new Transcript();
        transcript.setAudio_url(trackUrl);
        // Convert the transcript object to JSON using Gson
        Gson gson = new Gson();
        String jsonRequest = gson.toJson(transcript);

        log.info("Request payload: {}", jsonRequest);
        // Build the POST request to send the audio URL to the AssemblyAI API
        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(new URI(TRANSCRIPT_API_URL))
                .header("Authorization", KEY)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
                .build();

        HttpClient httpClient = HttpClient.newHttpClient();
        // Send the POST request and get the response
        HttpResponse<String> postResponse = httpClient.send(postRequest, HttpResponse.BodyHandlers.ofString());

        log.info("Post request status code: {}", postResponse.statusCode());
        log.info("Post response body: {}", postResponse.body());
        // If the response status code is not 200, log and throw an error
        if (postResponse.statusCode() != 200) {
            log.error("Failed to post request: Status code {} - Response body: {}", postResponse.statusCode(),
                    postResponse.body());
            throw new IOException("Failed to get transcript ID");
        }
        // Parse the response to get the transcript ID
        try {
            transcript = gson.fromJson(postResponse.body(), Transcript.class);
        } catch (JsonSyntaxException e) {
            log.error("Failed to parse JSON response: ", e);
            throw new IOException("Failed to parse response from AssemblyAI");
        }
        log.info(postResponse.body());
        // Build the GET request to check the transcript status
        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(new URI(TRANSCRIPT_API_URL + "/" + transcript.getId()))
                .header("Authorization", KEY)
                .build();
        // Poll the API until the transcription is completed or an error occurs
        while (true) {
            HttpResponse<String> getResponse = httpClient.send(getRequest, HttpResponse.BodyHandlers.ofString());
            log.info(transcript.getStatus());
            // If the GET request fails, log and throw an error
            if (getResponse.statusCode() != 200) {
                log.error("Failed to get transcript status: Status code {} - Response body: {}",
                        getResponse.statusCode(), getResponse.body());
                throw new IOException("Failed to get transcript status");
            }
            // Parse the response to update the transcript status
            try {
                transcript = gson.fromJson(getResponse.body(), Transcript.class);
            } catch (JsonSyntaxException e) {
                log.error("Failed to parse JSON response: ", e);
                throw new IOException("Failed to parse response from AssemblyAI");
            }
            // Break the loop if transcription is completed or an error occurred
            if ("completed".equals(transcript.getStatus()) || "error".equals(transcript.getStatus())) {
                break;
            }
            Thread.sleep(1000);
        }

        log.info("Transcription completed");
        log.info(transcript.getText());
        // Return the transcribed text
        return transcript.getText();
    }

}
