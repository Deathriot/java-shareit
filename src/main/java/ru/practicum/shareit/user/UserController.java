package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/users")
@Slf4j
public class UserController {

    private final UserService userService;

    @PostMapping
    public User addUser(@Valid @RequestBody UserDto user) {
        return userService.addUser(user);
    }

    @PatchMapping("/{id}")
    public User updateUser(@RequestBody UserDto user, @PathVariable long id) {
        log.info("updateUser");
        return userService.updateUser(user, id);
    }

    @DeleteMapping("/{id}")
    public void deleteUserById(@PathVariable long id) {
        log.info("deleteUserById");
        userService.deleteUserById(id);
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable long id) {
        log.info("getUserById");
        return userService.getUserById(id);
    }

    @GetMapping
    public List<User> getUsers() {
        log.info("getUsers");
        return userService.getUsers();
    }
}