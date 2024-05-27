package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.util.*;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {
    private long nextId = 1;

    private final Map<Long, User> users;

    //Для валидации
    private final Map<Long, String> emails;

    @Override
    public User addUser(UserDto userDto) {
        User user = UserMapper.toUser(userDto, nextId);
        users.put(user.getId(), user);
        nextId++;

        emails.put(user.getId(), user.getEmail());

        return user;
    }

    @Override
    public User updateUser(User user) {
        users.put(user.getId(), user);

        emails.put(user.getId(), user.getEmail());

        return user;
    }

    @Override
    public void deleteUserById(long userId) {
        users.remove(userId);

        emails.remove(userId);
    }

    @Override
    public User getUserById(long userId) {
        return users.get(userId);
    }

    @Override
    public List<User> getUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public Map<Long, String> getEmails() {
        return new HashMap<>(emails);
    }
}
