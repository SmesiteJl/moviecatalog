package com.example.moviecatalog.component;

import com.example.moviecatalog.entity.User;
import com.example.moviecatalog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds the single administrator account the admin panel needs to be reachable at all.
 *
 * <p>Credentials come from configuration so that a deployment can set its own; the shipped
 * default exists to make the demo runnable and is logged as a warning when left in place.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private static final String DEMO_PASSWORD = "admin123";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.username}")
    private String adminUsername;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Override
    public void run(ApplicationArguments args) {
        if (DEMO_PASSWORD.equals(adminPassword)) {
            log.warn("Administrator '{}' is using the demo password. Set ADMIN_PASSWORD before exposing "
                    + "this instance to anyone else.", adminUsername);
        }
        if (userRepository.existsByUsername(adminUsername)) {
            return;
        }
        User admin = new User();
        admin.setUsername(adminUsername);
        admin.setEmail(adminEmail);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setRole("ADMIN");
        admin.setIsBlocked(false);
        userRepository.save(admin);
        log.info("Administrator account '{}' created.", adminUsername);
    }
}
