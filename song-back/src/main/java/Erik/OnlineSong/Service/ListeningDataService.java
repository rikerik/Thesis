package Erik.OnlineSong.Service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import Erik.OnlineSong.DTO.ListeningDataDTO;
import Erik.OnlineSong.Model.ListeningData;
import Erik.OnlineSong.Repository.ListeningDataRepository;

@Service
public class ListeningDataService {

    private final ListeningDataRepository listeningDataRepository;

    public ListeningDataService(ListeningDataRepository listeningDataRepository) {
        this.listeningDataRepository = listeningDataRepository;
    }

    public void processListeningData(ListeningDataDTO listeningDataDTO) {
        // Create a new ListeningData entity from the DTO
        System.out.println(listeningDataDTO);

        ListeningData listeningData = ListeningData.builder()
                .userId(listeningDataDTO.getUserId())
                .artist(listeningDataDTO.getArtist())
                .duration(listeningDataDTO.getDuration())
                .timestamp(LocalDateTime.now())
                .build();

        listeningDataRepository.save(listeningData);

        System.out.println("Saved listening data: " + listeningData);
    }

    public int getTotalListeningMinutesForWeek(Integer userId) {
        // Get the start of the week (7 days ago)
        LocalDateTime startOfWeek = LocalDateTime.now().minusDays(7);

        // Fetch listening data for the last week
        List<ListeningData> listeningDataList = listeningDataRepository.findListeningDataForLastWeek(userId,
                startOfWeek);

        // Calculate the total duration in seconds
        int totalDurationInSeconds = listeningDataList.stream()
                .mapToInt(ListeningData::getDuration)
                .sum();

        // Convert the total duration to minutes

        return totalDurationInSeconds / 60;
    }

    public List<Object[]> getFavoriteArtistsByUserId(Integer userId) {
        return listeningDataRepository.findFavoriteArtistsByUserId(userId);
    }
}