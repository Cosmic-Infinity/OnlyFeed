package com.onlyfeed.api.config;

import com.onlyfeed.api.entity.UserAccount;
import com.onlyfeed.api.entity.UserRole;
import com.onlyfeed.api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AdminBootstrap {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String bootstrapUsername;
    private final String bootstrapPassword;

    public AdminBootstrap(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.admin.bootstrap-username:}") String bootstrapUsername,
            @Value("${app.admin.bootstrap-password:}") String bootstrapPassword) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.bootstrapUsername = bootstrapUsername;
        this.bootstrapPassword = bootstrapPassword;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void ensureAdminExists() {
        if (bootstrapUsername == null || bootstrapUsername.isBlank()
                || bootstrapPassword == null || bootstrapPassword.isBlank()) {
            return;
        }

        UserAccount account = userRepository.findByUsernameIgnoreCase(bootstrapUsername.trim())
                .orElseGet(UserAccount::new);

        account.setUsername(bootstrapUsername.trim());
        account.setRole(UserRole.ADMIN);
        account.setPasswordHash(passwordEncoder.encode(bootstrapPassword));
        userRepository.save(account);
    }
}