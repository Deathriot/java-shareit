package ru.practicum.shareit.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/users")
@Slf4j
public class UserController {

    private final UserService userService;

    @PostMapping
    public UserDto addUser(@Valid @RequestBody UserDto user) {
        log.info("addUser");
        return userService.addUser(user);
    }

    @PatchMapping("/{id}")
    public UserDto updateUser(@RequestBody UserDto user, @PathVariable long id) {
        log.info("updateUser");
        return userService.updateUser(user, id);
    }

    @DeleteMapping("/{id}")
    public void deleteUserById(@PathVariable long id) {
        log.info("deleteUserById");
        userService.deleteUserById(id);
    }

    @GetMapping("/{id}")
    public UserDto getUserById(@PathVariable long id) {
        log.info("getUserById");
        return userService.getUserById(id);
    }

    @GetMapping
    public List<UserDto> getUsers() {
        log.info("getUsers");
        return userService.getUsers();
    }
}