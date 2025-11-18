package com.fieldservices.repository;

import com.fieldservices.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testSaveUser() {
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("password");
        user.setEmail("test@example.com");
        user.setRole(User.Role.TECHNICIAN);
        user.setActive(true);

        User savedUser = userRepository.save(user);

        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getUsername()).isEqualTo("testuser");
    }

    @Test
    public void testFindByUsername() {
        User user = new User();
        user.setUsername("findme");
        user.setPassword("password");
        user.setEmail("findme@example.com");
        user.setRole(User.Role.DISPATCHER);
        user.setActive(true);

        userRepository.save(user);

        Optional<User> foundUser = userRepository.findByUsername("findme");

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("findme@example.com");
    }

    @Test
    public void testFindByEmail() {
        User user = new User();
        user.setUsername("emailtest");
        user.setPassword("password");
        user.setEmail("emailtest@example.com");
        user.setRole(User.Role.SUPERVISOR);
        user.setActive(true);

        userRepository.save(user);

        Optional<User> foundUser = userRepository.findByEmail("emailtest@example.com");

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo("emailtest");
    }

    @Test
    public void testExistsByUsername() {
        User user = new User();
        user.setUsername("exists");
        user.setPassword("password");
        user.setEmail("exists@example.com");
        user.setRole(User.Role.TECHNICIAN);
        user.setActive(true);

        userRepository.save(user);

        Boolean exists = userRepository.existsByUsername("exists");

        assertThat(exists).isTrue();
    }

    @Test
    public void testExistsByEmail() {
        User user = new User();
        user.setUsername("emailexists");
        user.setPassword("password");
        user.setEmail("emailexists@example.com");
        user.setRole(User.Role.TECHNICIAN);
        user.setActive(true);

        userRepository.save(user);

        Boolean exists = userRepository.existsByEmail("emailexists@example.com");

        assertThat(exists).isTrue();
    }

    @Test
    public void testUserNotFound() {
        Optional<User> foundUser = userRepository.findByUsername("nonexistent");

        assertThat(foundUser).isEmpty();
    }
}
