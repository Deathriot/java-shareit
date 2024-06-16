package ru.practicum.shareit.integrationTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.comment.CommentRequestDto;
import ru.practicum.shareit.item.dto.comment.CommentResponseDto;
import ru.practicum.shareit.item.dto.item.ItemRequestDto;
import ru.practicum.shareit.item.dto.item.ItemResponseDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.dto.RequestDto;
import ru.practicum.shareit.request.dto.RequestResponseDto;
import ru.practicum.shareit.request.service.RequestService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ItemServiceIntegrationTest {
    @Autowired
    private ItemService itemService;
    @Autowired
    private UserService userService;
    @Autowired
    private BookingService bookingService;
    @Autowired
    private RequestService itemRequestService;

    private UserDto userDto;
    private UserDto user2Dto;
    private ItemRequestDto itemDto;
    private RequestDto itemRequest;
    private ItemRequestDto item2Dto;
    private ItemRequestDto itemDtoToupdate;
    private CommentRequestDto commentDto;
    private BookingRequestDto lastBooking;
    private BookingRequestDto nextBooking;

    @BeforeEach
    void beforeEach() {

        userDto = UserDto.builder()
                .id(1L)
                .name("User")
                .email("user@email.com")
                .build();

        user2Dto = UserDto.builder()
                .id(2L)
                .name("SecondUser")
                .email("secondUser@email.com")
                .build();

        itemDto = ItemRequestDto.builder()
                .name("Item")
                .description("ItemDescription")
                .available(true)
                .build();

        itemRequest = RequestDto.builder()
                .description("ItemRequestDescription")
                .build();

        item2Dto = ItemRequestDto.builder()
                .name("Item2")
                .description("Item2Description")
                .available(true)
                .build();

        lastBooking = BookingRequestDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().minusHours(2))
                .end(LocalDateTime.now().minusHours(1))
                .build();

        nextBooking = BookingRequestDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusHours(2))
                .build();

        commentDto = CommentRequestDto.builder()
                .text("commentText")
                .build();
    }

    @Test
    void create_shouldCreateItemWithRequestId() {
        userService.addUser(userDto);
        UserDto savedRequester = userService.addUser(user2Dto);
        RequestResponseDto savedRequest = itemRequestService.createRequest(itemRequest, savedRequester.getId());
        itemDto.setRequestId(savedRequest.getId());

        ItemResponseDto createdItemDto = itemService.addItem(itemDto, 1L);

        assertEquals(itemDto.getName(), createdItemDto.getName());
        assertEquals(itemDto.getDescription(), createdItemDto.getDescription());
        assertEquals(itemDto.getAvailable(), createdItemDto.getAvailable());
        assertEquals(itemDto.getRequestId(), createdItemDto.getRequestId());
    }

    @Test
    void create_shouldCreateItemWithoutRequestId() {
        userService.addUser(userDto);

        ItemResponseDto createdItemDto = itemService.addItem(itemDto, 1L);

        assertEquals(itemDto.getName(), createdItemDto.getName());
        assertEquals(itemDto.getDescription(), createdItemDto.getDescription());
        assertEquals(itemDto.getAvailable(), createdItemDto.getAvailable());
        assertNull(createdItemDto.getRequestId());
    }

    @Test
    void create_shouldThrowDataNotFoundException_WhenUserNotExist() {
        NotFoundException dataNotFoundException = assertThrows(NotFoundException.class,
                () -> itemService.addItem(itemDto, 999L));

        assertEquals("Пользователь с id = 999", dataNotFoundException.getMessage());
    }

    @Test
    void getItemById_shouldThrowDataNotFoundException_WhenUserNotFound() {
        userService.addUser(userDto);
        itemService.addItem(itemDto, 1L);

        NotFoundException dataNotFoundException = assertThrows(NotFoundException.class,
                () -> itemService.getItemById(1L, 319L));

        assertEquals("Пользователь с id = 319", dataNotFoundException.getMessage());
    }

    @Test
    void getItemById_shouldThrowDataNotFoundException_WhenItemNotFound() {
        userService.addUser(userDto);

        NotFoundException dataNotFoundException = assertThrows(NotFoundException.class,
                () -> itemService.getItemById(1999L, 1L));

        assertEquals("Item id = 1999", dataNotFoundException.getMessage());
    }

    @Test
    void getItemById_shouldReturnItemWithBookings_WhenUserIsOwner() {
        UserDto savedOwner = userService.addUser(userDto);
        UserDto savedBooker = userService.addUser(user2Dto);
        itemService.addItem(itemDto, 1L);
        BookingResponseDto last = bookingService.createBooking(lastBooking, savedBooker.getId());
        bookingService.approveBooking(last.getId(), savedOwner.getId(), true);
        BookingResponseDto next = bookingService.createBooking(nextBooking, savedBooker.getId());
        bookingService.approveBooking(next.getId(), savedOwner.getId(), true);

        ItemResponseDto returnedItem = itemService.getItemById(1L, 1L);

        assertEquals(1L, returnedItem.getId());
        assertEquals(itemDto.getName(), returnedItem.getName());
        assertEquals(itemDto.getAvailable(), returnedItem.getAvailable());
        assertEquals(1L, returnedItem.getLastBooking().getId());
        assertEquals(2L, returnedItem.getLastBooking().getBookerId());
        assertEquals(2L, returnedItem.getNextBooking().getId());
        assertEquals(2L, returnedItem.getNextBooking().getBookerId());
    }

    @Test
    void getItemById_shouldReturnItemWithoutBookings_WhenUserIsNotOwner() {
        UserDto savedOwner = userService.addUser(userDto);
        UserDto savedBooker = userService.addUser(user2Dto);
        itemService.addItem(itemDto, 1L);
        BookingResponseDto last = bookingService.createBooking(lastBooking, savedBooker.getId());
        bookingService.approveBooking(last.getId(), savedOwner.getId(), true);
        BookingResponseDto next = bookingService.createBooking(nextBooking, savedBooker.getId());
        bookingService.approveBooking(next.getId(), savedOwner.getId(), true);

        ItemResponseDto returnedItem = itemService.getItemById(1L, 2L);

        assertEquals(1L, returnedItem.getId());
        assertEquals(itemDto.getName(), returnedItem.getName());
        assertEquals(itemDto.getAvailable(), returnedItem.getAvailable());
        assertNull(returnedItem.getLastBooking());
        assertNull(returnedItem.getNextBooking());
    }

    @Test
    void getItems_shouldReturnItems() {
        UserDto savedOwner = userService.addUser(userDto);
        UserDto savedBooker = userService.addUser(user2Dto);
        ItemResponseDto createdItemDto = itemService.addItem(itemDto, 1L);
        BookingResponseDto last = bookingService.createBooking(lastBooking, savedBooker.getId());
        bookingService.approveBooking(last.getId(), savedOwner.getId(), true);
        BookingResponseDto next = bookingService.createBooking(nextBooking, savedBooker.getId());
        bookingService.approveBooking(next.getId(), savedOwner.getId(), true);

        List<ItemResponseDto> returnedItems = itemService.getItems(1L, 0, 10);

        assertEquals(1, returnedItems.size());

        assertEquals(createdItemDto.getId(), returnedItems.get(0).getId());
        assertEquals(itemDto.getName(), returnedItems.get(0).getName());
        assertEquals(itemDto.getDescription(), returnedItems.get(0).getDescription());
        assertEquals(itemDto.getAvailable(), returnedItems.get(0).getAvailable());
        assertEquals(1L, returnedItems.get(0).getLastBooking().getId());
        assertEquals(2L, returnedItems.get(0).getLastBooking().getBookerId());
        assertEquals(2L, returnedItems.get(0).getNextBooking().getId());
        assertEquals(2L, returnedItems.get(0).getNextBooking().getBookerId());
    }

    @Test
    void update_shouldThrowWrongIdException_WhenItemNotFound() {
        userService.addUser(userDto);
        userService.addUser(user2Dto);
        itemService.addItem(itemDto, 1L);

        AccessDeniedException wrongIdException = assertThrows(AccessDeniedException.class,
                () -> itemService.updateItem(itemDtoToupdate, 2L, 1L));

        assertEquals("Редактирование Пользователем id = 2, вещи id = 1", wrongIdException.getMessage());
    }

    @Test
    void update_shouldUpdateItem() {
        userService.addUser(userDto);
        userService.addUser(user2Dto);
        itemService.addItem(itemDto, 1L);
        itemDtoToupdate = ItemRequestDto.builder()
                .name("UpdatedItem")
                .description("UpdatedItemDescription")
                .available(false)
                .build();

        ItemResponseDto updatedItem = itemService.updateItem(itemDtoToupdate, 1L, 1L);

        assertEquals(1L, updatedItem.getId());
        assertEquals(itemDtoToupdate.getName(), updatedItem.getName());
        assertEquals(itemDtoToupdate.getDescription(), updatedItem.getDescription());
        assertEquals(itemDtoToupdate.getAvailable(), updatedItem.getAvailable());
    }

    @Test
    void getItemByText_shouldGetItem() {
        userService.addUser(userDto);
        userService.addUser(user2Dto);
        ItemResponseDto createdItemDto = itemService.addItem(itemDto, 1L);
        ItemResponseDto createdItem2Dto = itemService.addItem(item2Dto, 1L);

        List<ItemResponseDto> returnedItems = itemService.getItemsSearch("descr", 0, 10);

        assertEquals(2, returnedItems.size());
        assertEquals(createdItemDto.getId(), returnedItems.get(0).getId());
        assertEquals(createdItemDto.getName(), returnedItems.get(0).getName());
        assertEquals(createdItem2Dto.getId(), returnedItems.get(1).getId());
        assertEquals(createdItem2Dto.getName(), returnedItems.get(1).getName());
    }

    @Test
    void getItemByText_shouldGetEmptyList_WhenTextIsBlank() {
        List<ItemResponseDto> returnedItems = itemService.getItemsSearch(" ", 0, 10);

        assertEquals(0, returnedItems.size());
    }

    @Test
    void createCommentShouldCreateComment() {
        UserDto savedOwner = userService.addUser(userDto);
        UserDto savedBooker = userService.addUser(user2Dto);
        itemService.addItem(itemDto, 1L);
        BookingResponseDto createdBooking = bookingService.createBooking(lastBooking, savedBooker.getId());
        bookingService.approveBooking(createdBooking.getId(), savedOwner.getId(), true);

        CommentResponseDto returnedComment = itemService.createComment(commentDto, 2L, 1L);

        assertEquals(savedBooker.getName(), returnedComment.getAuthorName());
        assertEquals(1L, returnedComment.getId());
        assertEquals(commentDto.getText(), returnedComment.getText());
    }

    @Test
    void createCommentShouldThrowDataNotFoundException_WhenUserNotFound() {
        NotFoundException dataNotFoundException = assertThrows(NotFoundException.class,
                () -> itemService.createComment(commentDto, 1L, 1L));

        assertEquals("Пользователь с id = 1", dataNotFoundException.getMessage());
    }

    @Test
    void createCommentShouldThrowBadRequestException_WhenItemNotBookered() {
        userService.addUser(userDto);
        userService.addUser(user2Dto);
        itemService.addItem(itemDto, 1L);

        BadRequestException itemAvailabilityException = assertThrows(BadRequestException.class,
                () -> itemService.createComment(commentDto, 1L, 1L));

        assertEquals("Вы не можете писать комментарий к вещи, которой не пользовались",
                itemAvailabilityException.getMessage());
    }
}
