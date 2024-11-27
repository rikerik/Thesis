package Erik.OnlineSong.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FavoriteRequestDTO {
    private Integer songId;
    private boolean isFavorited;

}
