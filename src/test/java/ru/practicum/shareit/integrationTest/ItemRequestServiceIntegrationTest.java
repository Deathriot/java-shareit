package ru.practicum.shareit.integrationTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.dto.RequestDto;
import ru.practicum.shareit.request.dto.RequestResponseDto;
import ru.practicum.shareit.request.service.RequestService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ItemRequestServiceIntegrationTest {
    @Autowired
    private RequestService itemRequestService;
    @Autowired
    private UserService userService;
    @Autowired
    private ItemService itemService;

    private UserDto userDto;
    private UserDto user2Dto;
    private RequestDto itemRequestDto;

    @BeforeEach
    void beforeEach() {

        userDto = UserDto.builder()
                .id(1L)
                .name("User")
                .email("user@email.com")
                .build();

        user2Dto = UserDto.builder()
                .id(2L)
                .name("AnotherUser")
                .email("AnotherUser@email.com")
                .build();

        itemRequestDto = RequestDto.builder()
                .description("RequestDescription")
                .build();
    }

    @Test
    void create_shouldCreateItemRequest() {
        userService.addUser(userDto);

        RequestResponseDto createdRequest = itemRequestService.createRequest(itemRequestDto, 1L);

        assertEquals(createdRequest.getId(), 1L);
        assertEquals(createdRequest.getDescription(), itemRequestDto.getDescription());
    }

    @Test
    void create_shouldThrowDataNotFoundException_WhenUserNotExist() {
        NotFoundException dataNotFoundException = assertThrows(NotFoundException.class,
                () -> itemRequestService.createRequest(itemRequestDto, 999L));

        assertEquals("user", dataNotFoundException.getMessage());
    }

    @Test
    void getRequests_shouldGetRequests() {
        userService.addUser(userDto);
        userService.addUser(user2Dto);
        itemRequestService.createRequest(itemRequestDto, 1L);

        List<RequestResponseDto> returnedRequests = itemRequestService.getAll(2L, 0, 10);

        assertEquals(1, returnedRequests.size());
        assertEquals(returnedRequests.get(0).getId(), 1L);
        assertEquals(returnedRequests.get(0).getDescription(), itemRequestDto.getDescription());
    }

    @Test
    void getRequestById_shouldThrowDataNotFoundException_WhenRequestNotExist() {
        userService.addUser(userDto);

        NotFoundException dataNotFoundException = assertThrows(NotFoundException.class,
                () -> itemRequestService.getRequest(1L, 1L));

        assertEquals("request", dataNotFoundException.getMessage());
    }

    @Test
    void getRequestById_shouldGetRequestById() {
        userService.addUser(userDto);
        itemRequestService.createRequest(itemRequestDto, 1L);

        RequestResponseDto returnedRequest = itemRequestService.getRequest(1L, 1L);

        assertEquals(returnedRequest.getId(), 1L);
        assertEquals(returnedRequest.getDescription(), itemRequestDto.getDescription());
    }

    @Test
    void getRequestsByPage_shouldGetRequestsByPage() {
        userService.addUser(userDto);
        userService.addUser(user2Dto);
        itemRequestService.createRequest(itemRequestDto, 2L);

        List<RequestResponseDto> returnedRequests = itemRequestService.getAll(1L, 0, 10);

        assertEquals(1, returnedRequests.size());
        assertEquals(1L, returnedRequests.get(0).getId());
        assertEquals(itemRequestDto.getDescription(), returnedRequests.get(0).getDescription());
    }
}