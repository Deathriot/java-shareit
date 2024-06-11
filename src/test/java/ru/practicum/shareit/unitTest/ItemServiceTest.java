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
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.comment.CommentMapper;
import ru.practicum.shareit.item.dto.comment.CommentRequestDto;
import ru.practicum.shareit.item.dto.comment.CommentResponseDto;
import ru.practicum.shareit.item.dto.item.ItemMapper;
import ru.practicum.shareit.item.dto.item.ItemRequestDto;
import ru.practicum.shareit.item.dto.item.ItemResponseDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.RequestRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class ItemServiceTest {
    @InjectMocks
    private ItemServiceImpl itemService;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RequestRepository itemRequestRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private CommentRepository commentRepository;

    private User user;
    private User user2;
    private Item item;
    private ItemRequestDto itemDto;
    private ItemRequest itemRequest;
    private Item item2;
    private ItemRequestDto itemDtoToUpdate;
    private CommentRequestDto commentDto;
    private Pageable pageable;
    private List<Booking> bookings;
    private List<Item> items;

    @BeforeEach
    void beforeEach() {
        UserDto userDto = UserDto.builder()
                .id(1L)
                .name("User")
                .email("user@email.com")
                .build();
        user = UserMapper.toUser(userDto);

        UserDto user2Dto = UserDto.builder()
                .id(2L)
                .name("SecondUser")
                .email("secondUser@email.com")
                .build();
        user2 = UserMapper.toUser(user2Dto);

        itemDto = ItemRequestDto.builder()
                .name("Item")
                .description("ItemDescription")
                .available(true)
                .requestId(1L)
                .build();

        itemRequest = ItemRequest.builder()
                .id(1L)
                .description("ItemRequestDescription")
                .created(LocalDateTime.now())
                .build();

        item = ItemMapper.toItem(itemDto, 1L, user);
        item.setRequest(itemRequest);

        ItemRequestDto item2Dto = ItemRequestDto.builder()
                .name("Item2")
                .description("Item2Description")
                .available(true)
                .requestId(2L)
                .build();

        ItemRequest item2Request = ItemRequest.builder()
                .id(2L)
                .description("Item2RequestDescription")
                .created(LocalDateTime.now())
                .build();

        item2 = ItemMapper.toItem(item2Dto, 2L, user);
        item2.setRequest(item2Request);


        Booking nextBooking = Booking.builder()
                .id(2L)
                .booker(user2)
                .item(item)
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusHours(2))
                .status(Status.APPROVED)
                .build();


        bookings = List.of(nextBooking);

        pageable = PageRequest.of(0, 10);

        items = List.of(item, item2);

        commentDto = CommentRequestDto.builder()
                .text("commentText")
                .build();
    }

    @Test
    void createShouldCreateItemWithRequestId() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Mockito.when(itemRepository.save(Mockito.any())).thenReturn(item);
        Mockito.when(itemRequestRepository.findById(1L)).thenReturn(Optional.of(itemRequest));

        ItemResponseDto createdItemDto = itemService.addItem(itemDto, 1L);

        Mockito.verify(userRepository).findById(1L);
        Mockito.verify(itemRequestRepository).findById(1L);
        Mockito.verify(itemRepository).save(Mockito.any());

        assertEquals(itemDto.getName(), createdItemDto.getName());
        assertEquals(itemDto.getDescription(), createdItemDto.getDescription());
        assertEquals(itemDto.getAvailable(), createdItemDto.getAvailable());
        assertEquals(itemDto.getRequestId(), createdItemDto.getRequestId());
    }

    @Test
    void createShouldCreateItemWithoutRequestId() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Mockito.when(itemRepository.save(Mockito.any())).thenReturn(item);

        itemDto.setRequestId(null);
        ItemResponseDto createdItemDto = itemService.addItem(itemDto, 1L);

        Mockito.verify(userRepository).findById(1L);
        Mockito.verify(itemRepository).save(Mockito.any());

        assertEquals(itemDto.getName(), createdItemDto.getName());
        assertEquals(itemDto.getDescription(), createdItemDto.getDescription());
        assertEquals(itemDto.getAvailable(), createdItemDto.getAvailable());
    }

    @Test
    void createShouldThrowDataNotFoundException_WhenUserNotExist() {
        NotFoundException dataNotFoundException = assertThrows(NotFoundException.class,
                () -> itemService.addItem(itemDto, 999L));

        assertEquals("Пользователь с id = 999", dataNotFoundException.getMessage());
    }

    @Test
    void getItemByIdShouldThrowDataNotFoundException_WhenUserNotFound() {
        Mockito.when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        NotFoundException dataNotFoundException = assertThrows(NotFoundException.class,
                () -> itemService.getItemById(1L, 1L));

        assertEquals("Пользователь с id = 1", dataNotFoundException.getMessage());
    }

    @Test
    void getItemByIdShouldThrowDataNotFoundException_WhenItemNotFound() {
        Mockito.when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException dataNotFoundException = assertThrows(NotFoundException.class,
                () -> itemService.getItemById(999L, 1L));

        assertEquals("Item id = 999", dataNotFoundException.getMessage());
    }

    @Test
    void getItemByIdShouldReturnItemWithBookings_WhenUserIsOwner() {
        item.setOwner(user);

        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Mockito.when(bookingRepository.findAllByItemId(1L)).thenReturn(bookings);
        Mockito.when(commentRepository.findAllByItemId(1L)).thenReturn(List.of());
        Mockito.when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        ItemResponseDto returnedItem = itemService.getItemById(1L, 1L);

        Mockito.verify(itemRepository).findById(1L);

        assertEquals(1L, returnedItem.getId());
        assertEquals(itemDto.getName(), returnedItem.getName());
        assertEquals(itemDto.getAvailable(), returnedItem.getAvailable());
        assertEquals(2L, returnedItem.getNextBooking().getId());
        assertEquals(2L, returnedItem.getNextBooking().getBookerId());
    }

    @Test
    void getItemByIdShouldReturnItemWithoutBookings_WhenUserIsNotOwner() {
        item.setOwner(user2);

        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Mockito.when(commentRepository.findAllByItemId(1L)).thenReturn(List.of());
        Mockito.when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        ItemResponseDto returnedItem = itemService.getItemById(1L, 1L);

        Mockito.verify(itemRepository).findById(1L);

        assertEquals(1L, returnedItem.getId());
        assertEquals(itemDto.getName(), returnedItem.getName());
        assertEquals(itemDto.getAvailable(), returnedItem.getAvailable());
        assertNull(returnedItem.getLastBooking());
        assertNull(returnedItem.getNextBooking());
    }

    @Test
    void getItemsShouldThrowDataNotFoundException_WhenUserNotFound() {
        NotFoundException dataNotFoundException = assertThrows(NotFoundException.class,
                () -> itemService.getItems(999L, 1, 10));

        assertEquals("Пользователь с id = 999", dataNotFoundException.getMessage());
    }

    @Test
    void getItemsShouldReturnItems() {
        item.setOwner(user);
        item2.setOwner(user2);

        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Mockito.when(itemRepository.findAllByOwnerId(1L, pageable)).thenReturn(items);

        List<ItemResponseDto> returnedItems = itemService.getItems(1L, 0, 10);

        Mockito.verify(itemRepository).findAllByOwnerId(1L, pageable);

        assertEquals(items.size(), returnedItems.size());

        assertEquals(items.get(0).getId(), returnedItems.get(0).getId());
        assertEquals(items.get(0).getName(), returnedItems.get(0).getName());
        assertEquals(items.get(0).getDescription(), returnedItems.get(0).getDescription());
        assertEquals(items.get(0).getAvailable(), returnedItems.get(0).getAvailable());

        assertEquals(items.get(1).getId(), returnedItems.get(1).getId());
        assertEquals(items.get(1).getName(), returnedItems.get(1).getName());
        assertEquals(items.get(1).getDescription(), returnedItems.get(1).getDescription());
        assertEquals(items.get(1).getAvailable(), returnedItems.get(1).getAvailable());
    }

    @Test
    void updateShouldThrowDataNotFoundException_WhenUserNotFound() {
        NotFoundException dataNotFoundException = assertThrows(NotFoundException.class,
                () -> itemService.updateItem(itemDtoToUpdate, 999L, 1L));

        assertEquals("Пользователь с id = 999", dataNotFoundException.getMessage());
    }

    @Test
    void updateShouldThrowDataNotFoundException_WhenItemNotFound() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Mockito.when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException dataNotFoundException = assertThrows(NotFoundException.class,
                () -> itemService.updateItem(itemDtoToUpdate, 1L, 999L));

        assertEquals("Item id = 999", dataNotFoundException.getMessage());
    }

    @Test
    void updateShouldUpdateItem() {
        item.setOwner(user);
        itemDtoToUpdate = ItemRequestDto.builder()
                .name("UpdatedItem")
                .description("UpdatedItemDescription")
                .available(false)
                .build();

        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Mockito.when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        Mockito.when(itemRepository.save(Mockito.any())).thenReturn(ItemMapper.toUpdatedItem(item, itemDtoToUpdate));

        ItemResponseDto updatedItem = itemService.updateItem(itemDtoToUpdate, 1L, 1L);

        Mockito.verify(itemRepository).save(Mockito.any());

        assertEquals(1L, updatedItem.getId());
        assertEquals(itemDtoToUpdate.getName(), updatedItem.getName());
        assertEquals(itemDtoToUpdate.getDescription(), updatedItem.getDescription());
        assertEquals(itemDtoToUpdate.getAvailable(), updatedItem.getAvailable());
    }

    @Test
    void getItemByTextShouldGetItem() {
        Mockito.when(itemRepository.getItemsSearch("descr", pageable)).thenReturn(items);

        List<ItemResponseDto> returnedItems = itemService.getItemsSearch("descr", 0, 10);

        Mockito.verify(itemRepository).getItemsSearch("descr", pageable);

        assertEquals(2, returnedItems.size());
        assertEquals(items.get(0).getId(), returnedItems.get(0).getId());
        assertEquals(items.get(0).getName(), returnedItems.get(0).getName());
        assertEquals(items.get(1).getId(), returnedItems.get(1).getId());
        assertEquals(items.get(1).getName(), returnedItems.get(1).getName());
    }

    @Test
    void getItemByTextShouldGetEmptyList_WhenTextIsBlank() {
        List<ItemResponseDto> returnedItems = itemService.getItemsSearch(" ", 0, 10);

        assertEquals(0, returnedItems.size());
    }

    @Test
    void createCommentShouldCreateComment() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Mockito.when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        Mockito.when(commentRepository.save(Mockito.any()))
                .thenReturn((CommentMapper.toComment(commentDto, user, item)));

        Mockito.when(bookingRepository.findAllByBookerIdAndItemIdAndStatusAndEndBefore(Mockito.anyLong(),
                Mockito.anyLong(), Mockito.any(), Mockito.any())).thenReturn(bookings);

        CommentResponseDto returnedComment = itemService.createComment(commentDto, 1L, 1L);

        Mockito.verify(commentRepository).save(Mockito.any());

        assertEquals(user.getName(), returnedComment.getAuthorName());
        assertEquals(commentDto.getText(), returnedComment.getText());
    }

    @Test
    void createCommentShouldThrowDataNotFoundException_WhenUserNotFound() {
        NotFoundException dataNotFoundException = assertThrows(NotFoundException.class,
                () -> itemService.createComment(commentDto, 999L, 1L));

        assertEquals("Пользователь с id = 999", dataNotFoundException.getMessage());
    }

    @Test
    void createCommentShouldThrowItemAvailabilityException_WhenItemNotBookered() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemService.createComment(commentDto, 1L, 1L));

        assertEquals("Item id = 1", exception.getMessage());
    }
}
