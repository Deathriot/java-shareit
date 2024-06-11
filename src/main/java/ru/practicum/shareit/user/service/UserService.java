package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserService {
    UserDto addUser(UserDto user);

    UserDto updateUser(UserDto user, Long userId);

    void deleteUserById(Long userId);

    UserDto getUserById(Long userId);

    List<UserDto> getUsers();
}
