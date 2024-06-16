package ru.practicum.shareit.unitTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @InjectMocks
    private UserServiceImpl userService;
    @Mock
    private UserRepository userRepository;
    private User user;
    private UserDto userDto;

    @BeforeEach
    void beforeEach() {
        userDto = UserDto.builder()
                .id(1L)
                .name("name")
                .email("name@mail.ru")
                .build();
        user = UserMapper.toUser(userDto);
    }

    @Test
    void createUser() {
        Mockito.when(userRepository.save(Mockito.any())).thenReturn(user);
        UserDto savedUser = userService.addUser(userDto);
        Mockito.verify(userRepository).save(Mockito.any());

        assertEquals(userDto.getId(), savedUser.getId());
        assertEquals(userDto.getName(), savedUser.getName());
        assertEquals(userDto.getEmail(), savedUser.getEmail());
    }

    @Test
    void updateUser() {
        UserDto userDtoUpdate = UserDto.builder()
                .id(1L)
                .name("UpdatedName")
                .email("updatedName@mail.ru")
                .build();
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Mockito.when(userRepository.save(Mockito.any())).thenReturn(UserMapper.toUser(userDtoUpdate));
        UserDto updatedUser = userService.updateUser(userDtoUpdate, userDtoUpdate.getId());

        Mockito.verify(userRepository).findById(userDtoUpdate.getId());
        Mockito.verify(userRepository).save(Mockito.any());

        assertEquals(userDtoUpdate.getEmail(), updatedUser.getEmail());
        assertEquals(userDtoUpdate.getName(), updatedUser.getName());
    }

    @Test
    void getUser() {
        Mockito.when((userRepository.findById(1L))).thenReturn(Optional.of(user));
        UserDto savedUser = userService.getUserById(1L);

        Mockito.verify(userRepository).findById(user.getId());

        assertEquals(userDto.getId(), savedUser.getId());
        assertEquals(userDto.getName(), savedUser.getName());
        assertEquals(userDto.getEmail(), savedUser.getEmail());
    }

    @Test
    void getAllUsers() {
        List<User> users = List.of(user);

        Mockito.when(userRepository.findAll()).thenReturn(users);
        List<UserDto> userDtos = userService.getUsers();

        Mockito.verify(userRepository).findAll();

        assertEquals(userDto.getId(), userDtos.get(0).getId());
        assertEquals(userDto.getName(), userDtos.get(0).getName());
        assertEquals(userDto.getEmail(), userDtos.get(0).getEmail());
        assertEquals(users.size(), userDtos.size());
    }

    @Test
    void dataNotFoundExcForGetUser() {
        Mockito.when(userRepository.findById(100L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> userService.getUserById(100L));

        assertEquals("user", exception.getMessage());
    }

    @Test
    void dataNotFoundExcForUpdateUser() {
        Mockito.when(userRepository.findById(100L)).thenReturn(Optional.empty());

        UserDto userDtoUpdate = UserDto.builder()
                .id(1L)
                .name("UpdatedName")
                .email("updatedName@mail.ru")
                .build();

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userService.updateUser(userDtoUpdate, 100L));

        assertEquals("user", exception.getMessage());
    }
}
