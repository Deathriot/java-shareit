package ru.practicum.shareit.jpaTest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    private User user1;
    private User user2;

    @BeforeEach
    void beforeEach() {
        user1 = User.builder()
                .name("name1")
                .email("name1@mail.com")
                .build();

        user2 = User.builder()
                .name("name2")
                .email("name2@mail.com")
                .build();

        userRepository.save(user1);
        userRepository.save(user2);
    }

    @AfterEach
    void afterEach() {
        userRepository.deleteAll();
    }

    @Test
    void shouldReturnUserById() {
        User user = userRepository.findById(user1.getId()).orElse(null);

        assertNotNull(user);
        assertEquals(user.getId(), user1.getId());
        assertEquals(user.getName(), user1.getName());
    }

    @Test
    void shouldReturnAllUsers() {
        List<User> users = userRepository.findAll();

        assertEquals(2, users.size());
        assertEquals(users, List.of(user1, user2));
    }
}
