package ru.practicum.shareit.user.dto;

import ru.practicum.shareit.user.model.User;

public final class UserMapper {
    private UserMapper() {

    }

    public static UserDto toUserDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    public static User toUser(UserDto userDto) {
        return User.builder()
                .id(userDto.getId())
                .email(userDto.getEmail())
                .name(userDto.getName())
                .build();
    }

    // Дополнительный метод маппера для упрощения обновления пользователя
    public static User toUpdatedUser(User user, UserDto userDto) {
        return User.builder()
                .id(user.getId())
                .name(userDto.getName() == null ? user.getName() : userDto.getName())
                .email(userDto.getEmail() == null ? user.getEmail() : userDto.getEmail())
                .build();
    }
}