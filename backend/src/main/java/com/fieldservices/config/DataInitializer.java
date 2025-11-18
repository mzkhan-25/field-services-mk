package com.fieldservices.config;

import com.fieldservices.model.User;
import com.fieldservices.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            logger.info("Initializing default users...");

            // Create dispatcher user
            User dispatcher = new User();
            dispatcher.setUsername("dispatcher");
            dispatcher.setPassword(passwordEncoder.encode("password123"));
            dispatcher.setEmail("dispatcher@fieldservices.com");
            dispatcher.setRole(User.Role.DISPATCHER);
            dispatcher.setActive(true);
            userRepository.save(dispatcher);
            logger.info("Created dispatcher user");

            // Create technician user
            User technician = new User();
            technician.setUsername("technician");
            technician.setPassword(passwordEncoder.encode("password123"));
            technician.setEmail("technician@fieldservices.com");
            technician.setRole(User.Role.TECHNICIAN);
            technician.setActive(true);
            userRepository.save(technician);
            logger.info("Created technician user");

            // Create supervisor user
            User supervisor = new User();
            supervisor.setUsername("supervisor");
            supervisor.setPassword(passwordEncoder.encode("password123"));
            supervisor.setEmail("supervisor@fieldservices.com");
            supervisor.setRole(User.Role.SUPERVISOR);
            supervisor.setActive(true);
            userRepository.save(supervisor);
            logger.info("Created supervisor user");

            logger.info("Default users initialized successfully");
        }
    }
}
