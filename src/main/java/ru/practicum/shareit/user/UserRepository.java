package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserRepository {
    User addUser(UserDto user);

    User updateUser(User user);

    void deleteUserById(long userId);

    User getUserById(long userId);

    List<User> getUsers();

    // Так норм?
    boolean isEmailAlreadyExist(String email, long userId);
}
