package Erik.OnlineSong.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ListeningDataDTO {
    private Integer userId;
    private String artist;
    private Integer duration;
}
