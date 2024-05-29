package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserService {
    User addUser(UserDto user);

    User updateUser(UserDto user, long userId);

    void deleteUserById(long userId);

    User getUserById(long userId);

    List<User> getUsers();
}
