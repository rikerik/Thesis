package Erik.OnlineSong.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import Erik.OnlineSong.Service.UserProfileService;

@RestController
@RequestMapping("/profile")
public class ProfileController {

    private UserProfileService profileService;

    public ProfileController(UserProfileService profileService) {
        this.profileService = profileService;
    }

    // Endpoint to update user profile
    @PutMapping("/update/{userId}")
    public ResponseEntity<?> updateProfile(@PathVariable Integer userId,
            @RequestParam(value = "firstName", required = false) String firstName,
            @RequestParam(value = "userName", required = false) String userName,
            @RequestParam(value = "lastName", required = false) String lastName,
            @RequestParam(value = "password", required = false) String password,
            @RequestParam(value = "email", required = false) String email) {
        profileService.updateProfile(userId, firstName, userName, lastName, password, email);
        return ResponseEntity.ok("Profile updated successfully");

    }

}
