package com.readyrecipe.backend.config;

import com.readyrecipe.backend.entity.User;
import com.readyrecipe.backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.UUID;

@Configuration
public class DataSeeder {

    @Bean
    public CommandLineRunner seedUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.findByEmail("user1@example.com").isEmpty()) {
                User u1 = new User("user1@example.com", passwordEncoder.encode("password1"));
                userRepository.save(u1);
            }
            if (userRepository.findByEmail("user2@example.com").isEmpty()) {
                User u2 = new User("user2@example.com", passwordEncoder.encode("password2"));
                userRepository.save(u2);
            }

            // Additional test users referenced by seed SQL
            if (userRepository.findByEmail("test1@example.com").isEmpty()) {
                User t1 = new User("test1@example.com", passwordEncoder.encode("testpass"));
                // set known UUID used by seed data so pantry items map correctly
                t1.setId(UUID.fromString("64a0c288-49e7-4931-a940-d3d0d0326be3"));
                userRepository.save(t1);
            }

            if (userRepository.findByEmail("test2@example.com").isEmpty()) {
                User t2 = new User("test2@example.com", passwordEncoder.encode("test2pass"));
                t2.setId(UUID.fromString("fd7fa497-a3a7-4622-a0a3-1d0e9cb4f8ef"));
                userRepository.save(t2);
            }
        };
    }
}
