package com.readyrecipe.backend.config;

import com.readyrecipe.backend.entity.User;
import com.readyrecipe.backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

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
        };
    }
}
