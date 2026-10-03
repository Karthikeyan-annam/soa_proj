package com.cinepass.user;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DemoUserSeed {
    @Bean
    CommandLineRunner seedDemoUser(UserRepository users, PasswordEncoder encoder) {
        return args -> {
            if (users.existsByEmail("anna@cinepass.test")) {
                return;
            }
            UserAccount user = new UserAccount();
            user.setFullName("Anna");
            user.setEmail("anna@cinepass.test");
            user.setPasswordHash(encoder.encode("cinepass123"));
            users.save(user);
        };
    }
}
