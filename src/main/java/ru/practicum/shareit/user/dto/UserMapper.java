package ru.practicum.shareit.user.dto;

import ru.practicum.shareit.user.model.User;

public final class UserMapper {
    private UserMapper() {

    }

    public static UserDto toUserDto(User user) {
        return UserDto.builder()
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    public static User toUser(UserDto userDto, long id) {
        return User.builder()
                .id(id)
                .email(userDto.getEmail())
                .name(userDto.getName())
                .build();
    }
}