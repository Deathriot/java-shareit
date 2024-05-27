package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface UserRepository {
    User addUser(UserDto user);

    User updateUser(User user);

    void deleteUserById(long userId);

    User getUserById(long userId);

    List<User> getUsers();

    // Необходимо для валидации имейла на уровне сервиса
    Map<Long, String> getEmails();
}
