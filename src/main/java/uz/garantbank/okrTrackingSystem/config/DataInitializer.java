package uz.garantbank.okrTrackingSystem.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import uz.garantbank.okrTrackingSystem.entity.Role;
import uz.garantbank.okrTrackingSystem.entity.User;
import uz.garantbank.okrTrackingSystem.repository.UserRepository;

/**
 * Initializes default data on application startup.
 * Creates an admin user if no users exist in the database.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Create admin user if no users exist
        if (userRepository.count() == 0) {
            System.out.println("=".repeat(80));
            System.out.println("NO USERS FOUND - CREATING DEFAULT ADMIN USER");
            System.out.println("=".repeat(80));

            String encodedPassword = passwordEncoder.encode("admin123");
            log.info("Encoded password for admin: {}", encodedPassword);

            User admin = User.builder()
                    .username("admin")
                    .email("admin@okr-tracker.com")
                    .password(encodedPassword)
                    .fullName("System Administrator")
                    .role(Role.ADMIN)
                    .isActive(true)
                    .build();

            admin = userRepository.save(admin);

            log.info("Admin user created with ID: {}, isActive: {}", admin.getId(), admin.isActive());

            System.out.println("\nDefault Admin User Created:");
            System.out.println("  Username: admin");
            System.out.println("  Password: admin123");
            System.out.println("\nYou can now log in and click 'Demo' button to load sample data.");
            System.out.println("=".repeat(80));
        } else {
            log.info("Users already exist in database, count: {}", userRepository.count());
        }
    }
}
