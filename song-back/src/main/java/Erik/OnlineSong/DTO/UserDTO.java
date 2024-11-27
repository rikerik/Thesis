package Erik.OnlineSong.DTO;

import Erik.OnlineSong.Model.Role;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class UserDTO {
    private Integer id;

    private String firstName;

    private String lastName;

    private String username;

    private Role role;
}
