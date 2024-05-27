package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.AlreadyExistException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.util.*;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository repository;

    @Override
    public User addUser(UserDto user) {
        emailValidation(user.getEmail());

        return repository.addUser(user);
    }

    @Override
    public User updateUser(UserDto userDto, long userId) {
        idValidation(userId);
        emailValidation(userDto.getEmail(), userId);

        User user = UserMapper.toUser(userDto, userId);
        return repository.updateUser(user);
    }

    @Override
    public void deleteUserById(long userId) {
        idValidation(userId);

        repository.deleteUserById(userId);
    }

    @Override
    public User getUserById(long userId) {
        idValidation(userId);
        return repository.getUserById(userId);
    }

    @Override
    public List<User> getUsers() {
        return repository.getUsers();
    }

    private void emailValidation(String email, long userId) {
        Map<Long, String> emails = repository.getEmails();

        if (emails.containsValue(email) && !Objects.equals(email, emails.get(userId))) {
            throw new AlreadyExistException("email");
        }
    }

    private void emailValidation(String email) {
        Set<String> emails = new HashSet<>(repository.getEmails().values());

        if (emails.contains(email)) {
            throw new AlreadyExistException("email");
        }
    }

    private void idValidation(long id) {
        User user = repository.getUserById(id);

        if (user == null) {
            throw new NotFoundException("User");
        }
    }
}
