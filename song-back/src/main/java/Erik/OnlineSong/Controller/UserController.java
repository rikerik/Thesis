package Erik.OnlineSong.Controller;

import org.springframework.web.bind.annotation.RestController;

import Erik.OnlineSong.DTO.UserDTO;
import Erik.OnlineSong.Model.User;
import Erik.OnlineSong.Repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
public class UserController {

    private final UserRepository repository;

    public UserController(UserRepository repository) {
        this.repository = repository;
    }

    // Endpoint to get all users
    @GetMapping("/users")
    public List<UserDTO> getAllUsers() {
        List<User> users = repository.findAll();
        return users.stream() // Stream the list of users
                .map(user -> new UserDTO( // Map each User to UserDTO
                        user.getId(),
                        user.getUsername(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getRole()))
                .collect(Collectors.toList()); // Collect the UserDTOs into a list
    }

    // Endpoint to get a user by ID
    @GetMapping("/users/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable("id") Integer id) {
        return repository.findById(id)
                .map(user -> new UserDTO(user.getId(), user.getUsername(), user.getFirstName(), user.getLastName(),
                        user.getRole()))
                .map(ResponseEntity::ok)// If found, return UserDTO in response
                .orElseGet(() -> ResponseEntity.notFound().build()); // If not found, return 404 Not Found
    }

}
