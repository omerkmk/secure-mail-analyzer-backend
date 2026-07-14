package com.securemail.analyzer.config;

import com.securemail.analyzer.entity.User;
import com.securemail.analyzer.enums.Role;
import com.securemail.analyzer.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Uygulama ayağa kalkınca admin yoksa varsayılan bir admin oluşturur.
 * Staj/dev ortamı için; production'da güçlü şifre ve env ile yönetilmeli.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminUserInitializer implements CommandLineRunner {

    private static final String ADMIN_EMAIL = "admin@securemail.com";
    private static final String ADMIN_PASSWORD = "Admin123!";
    private static final String ADMIN_FULL_NAME = "System Admin";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.existsByRole(Role.ADMIN)) {
            return;
        }

        User admin = User.builder()
                .fullName(ADMIN_FULL_NAME)
                .email(ADMIN_EMAIL)
                .password(passwordEncoder.encode(ADMIN_PASSWORD))
                .role(Role.ADMIN)
                .active(true)
                .build();

        userRepository.save(admin);
        log.info("Varsayılan admin oluşturuldu: {}", ADMIN_EMAIL);
    }
}
