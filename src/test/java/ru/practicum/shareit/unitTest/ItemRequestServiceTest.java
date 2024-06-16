package ru.practicum.shareit.unitTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.RequestDto;
import ru.practicum.shareit.request.dto.RequestMapper;
import ru.practicum.shareit.request.dto.RequestResponseDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.RequestRepository;
import ru.practicum.shareit.request.service.RequestServiceImpl;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class ItemRequestServiceTest {
    @InjectMocks
    private RequestServiceImpl itemRequestService;
    @Mock
    private RequestRepository itemRequestRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;

    private User user;
    private Item item;
    private List<Item> items;
    private RequestDto itemRequestDto;
    private ItemRequest itemRequest;
    private ItemRequest itemRequest2;
    private Pageable pageable;

    @BeforeEach
    void beforeEach() {
        user = User.builder()
                .id(1L)
                .name("User")
                .email("user@email.com")
                .build();

        User user2 = User.builder()
                .id(2L)
                .name("AnotherUser")
                .email("AnotherUser@email.com")
                .build();

        itemRequestDto = RequestDto.builder()
                .description("RequestDescription")
                .build();

        RequestDto itemRequest2Dto = RequestDto.builder()
                .description("SecondRequestDescription")
                .build();

        itemRequest = RequestMapper.toItemRequest(itemRequestDto, user);
        itemRequest2 = RequestMapper.toItemRequest(itemRequest2Dto, user2);

        item = Item.builder()
                .id(1L)
                .description("ItemDescription")
                .available(true)
                .request(itemRequest)
                .build();

        items = List.of(item);

        pageable = PageRequest.of(0, 10, Sort.by("created").descending());
    }

    @Test
    void createShouldCreateItemRequest() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Mockito.when(itemRequestRepository.save(Mockito.any()))
                .thenReturn(RequestMapper.toItemRequest(itemRequestDto, user));

        RequestResponseDto createdRequest = itemRequestService.createRequest(itemRequestDto, 1L);

        Mockito.verify(itemRequestRepository).save(Mockito.any());

        assertEquals(itemRequestDto.getDescription(), createdRequest.getDescription());
    }

    @Test
    void createShouldThrowDataNotFoundException_WhenUserNotExist() {
        Mockito.when(userRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException dataNotFoundException = assertThrows(NotFoundException.class,
                () -> itemRequestService.createRequest(itemRequestDto, 999L));

        assertEquals("user", dataNotFoundException.getMessage());
    }

    @Test
    void getRequestsShouldThrowDataNotFoundException_WhenUserNotExist() {
        Mockito.when(userRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException dataNotFoundException = assertThrows(NotFoundException.class,
                () -> itemRequestService.getAll(999L, 0, 10));

        assertEquals("user", dataNotFoundException.getMessage());
    }

    @Test
    void getRequestsShouldGetRequests() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Mockito.when(itemRequestRepository.findAllByUserId(1L)).thenReturn(List.of(itemRequest));

        List<RequestResponseDto> returnedRequests = itemRequestService.getAllByOwner(1L);

        Mockito.verify(itemRequestRepository).findAllByUserId(1L);

        assertEquals(1, returnedRequests.size());
        assertEquals(itemRequest.getId(), returnedRequests.get(0).getId());
        assertEquals(itemRequest.getDescription(), returnedRequests.get(0).getDescription());
    }

    @Test
    void getRequestByIdShouldThrowDataNotFoundException_WhenUserNotExist() {
        NotFoundException dataNotFoundException = assertThrows(NotFoundException.class,
                () -> itemRequestService.getRequest(1L, 1L));

        assertEquals("user", dataNotFoundException.getMessage());
    }

    @Test
    void getRequestByIdShouldThrowDataNotFoundException_WhenRequestNotExist() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        NotFoundException dataNotFoundException = assertThrows(NotFoundException.class,
                () -> itemRequestService.getRequest(99L, 1L));

        assertEquals("request", dataNotFoundException.getMessage());
    }

    @Test
    void getRequestByIdShouldGetRequestById() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Mockito.when(itemRequestRepository.findById(1L)).thenReturn(Optional.of(itemRequest));
        Mockito.when(itemRepository.findAllByRequestId(1L)).thenReturn(items);

        RequestResponseDto returnedRequest = itemRequestService.getRequest(1L, 1L);

        Mockito.verify(itemRequestRepository).findById(1L);

        assertEquals(itemRequest.getId(), returnedRequest.getId());
        assertEquals(itemRequest.getDescription(), returnedRequest.getDescription());
        assertEquals(1, returnedRequest.getItems().size());
        assertEquals(item.getId(), returnedRequest.getItems().get(0).getId());
    }

    @Test
    void getRequestsByPageShouldThrowDataNotFoundException_WhenUserNotExist() {
        NotFoundException dataNotFoundException = assertThrows(NotFoundException.class,
                () -> itemRequestService.getAll(999L, 0, 10));

        assertEquals("user", dataNotFoundException.getMessage());
    }

    @Test
    void getRequestsByPageShouldGetRequestsByPage() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Mockito.when(itemRequestRepository.findAllByUserIdNot(1L, pageable)).thenReturn(List.of(itemRequest2));

        List<RequestResponseDto> returnedRequests = itemRequestService.getAll(1L, 0, 10);

        Mockito.verify(itemRequestRepository).findAllByUserIdNot(1L, pageable);

        assertEquals(1, returnedRequests.size());
        assertEquals(itemRequest2.getId(), returnedRequests.get(0).getId());
        assertEquals(itemRequest2.getDescription(), returnedRequests.get(0).getDescription());
    }
}
