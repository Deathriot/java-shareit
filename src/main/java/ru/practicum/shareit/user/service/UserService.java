package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserService {
    UserDto addUser(UserDto user);

    UserDto updateUser(UserDto user, Long userId);

    void deleteUserById(long userId);

    UserDto getUserById(long userId);

    List<UserDto> getUsers();
}
