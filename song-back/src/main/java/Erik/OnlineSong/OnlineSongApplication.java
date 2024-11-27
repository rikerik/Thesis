package Erik.OnlineSong;

import Erik.OnlineSong.Service.LyricsService;
import Erik.OnlineSong.Service.PlaylistLoaderService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class OnlineSongApplication implements CommandLineRunner {

	private PlaylistLoaderService playlistLoaderService;

	public OnlineSongApplication(PlaylistLoaderService playlistLoaderService) {
		this.playlistLoaderService = playlistLoaderService;
	}

	public static void main(String[] args) {
		SpringApplication.run(OnlineSongApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		// playlistLoaderService.loadPlaylists();
		// Skipping loading file from bucket
	}

}
