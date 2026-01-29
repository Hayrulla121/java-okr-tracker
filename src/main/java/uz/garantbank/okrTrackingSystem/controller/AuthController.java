package uz.garantbank.okrTrackingSystem.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import uz.garantbank.okrTrackingSystem.dto.LoginRequest;
import uz.garantbank.okrTrackingSystem.dto.LoginResponse;
import uz.garantbank.okrTrackingSystem.dto.RegisterRequest;
import uz.garantbank.okrTrackingSystem.dto.UserDTO;
import uz.garantbank.okrTrackingSystem.security.JwtTokenProvider;
import uz.garantbank.okrTrackingSystem.security.UserDetailsImpl;
import uz.garantbank.okrTrackingSystem.service.UserService;

/**
 * Controller for authentication endpoints
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
@Slf4j
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;

    /**
     * Login endpoint
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        log.info("Login attempt for user: {}", request.getUsername());
        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        log.info("Authentication successful for user: {}", request.getUsername());

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Generate JWT token
        String jwt = jwtTokenProvider.generateToken(authentication);
        // Get user details
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // Update last login timestamp
        userService.updateLastLogin(userDetails.getId());

        UserDTO userDTO = userService.getUserById(userDetails.getId());

        log.info("Login completed for user: {}", request.getUsername());

        return ResponseEntity.ok(new LoginResponse(jwt, userDTO));

    }

    /**
     * Register new user (admin only)
     */
    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> register(@RequestBody RegisterRequest request) {
        UserDTO user = userService.registerUser(request);
        return ResponseEntity.ok(user);
    }

    /**
     * Get current user info
     */
    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        UserDTO userDTO = userService.getUserById(userDetails.getId());
        return ResponseEntity.ok(userDTO);
    }

}
