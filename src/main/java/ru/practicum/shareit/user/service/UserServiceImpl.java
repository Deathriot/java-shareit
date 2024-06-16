package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {
    private final UserRepository repository;

    @Override
    public UserDto addUser(UserDto user) {
        return UserMapper.toUserDto(repository.save(UserMapper.toUser(user)));
    }

    @Override
    public UserDto updateUser(UserDto userDto, Long userId) {
        User user = repository.findById(userId)
                .orElseThrow(() -> new NotFoundException("user"));

        return UserMapper.toUserDto(repository.save(UserMapper.toUpdatedUser(user, userDto)));
    }

    @Override
    public void deleteUserById(Long userId) {
        repository.deleteById(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getUserById(Long userId) {
        return UserMapper.toUserDto(repository.findById(userId)
                .orElseThrow(() -> new NotFoundException("user")));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getUsers() {
        return repository.findAll().stream().map(UserMapper::toUserDto).collect(Collectors.toList());
    }
}
