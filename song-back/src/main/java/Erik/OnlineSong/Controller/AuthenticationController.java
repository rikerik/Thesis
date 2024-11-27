package Erik.OnlineSong.Controller;

import org.springframework.web.bind.annotation.RestController;

import Erik.OnlineSong.Model.AuthenticationResponse;
import Erik.OnlineSong.Model.Role;
import Erik.OnlineSong.Model.User;
import Erik.OnlineSong.Service.AuthenticationService;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
public class AuthenticationController {

    private final AuthenticationService service;

    public AuthenticationController(AuthenticationService service) {
        this.service = service;
    }

    // Endpoint for user registration
    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @RequestBody User request) throws FileNotFoundException, IOException {
        request.setRole(Role.USER); // set default role to USER
        return ResponseEntity.ok(service.register(request));
    }

    // Endpoint for user login
    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(
            @RequestBody User request) {
        return ResponseEntity.ok(service.authenticate(request));
    }

}
