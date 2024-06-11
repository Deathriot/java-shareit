package ru.practicum.shareit.integrationTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.State;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.item.dto.item.ItemRequestDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class BookingServiceIntegrationTest {
    @Autowired
    private BookingService bookingService;
    @Autowired
    private ItemService itemService;
    @Autowired
    private UserService userService;

    private UserDto userDto;
    private UserDto user2Dto;
    private ItemRequestDto itemDto;
    private BookingRequestDto bookingDtoInput;
    private BookingRequestDto previousBookingDto;
    private BookingRequestDto futureBookingDto;

    @BeforeEach
    void beforeEach() {
        bookingDtoInput = BookingRequestDto.builder()
                .start(LocalDateTime.now())
                .end(LocalDateTime.now().plusHours(1))
                .itemId(1L)
                .build();

        previousBookingDto = BookingRequestDto.builder()
                .start(LocalDateTime.of(2024, 3, 1, 10, 0))
                .end(LocalDateTime.of(2024, 3, 1, 11, 0))
                .itemId(1L)
                .build();

        futureBookingDto = BookingRequestDto.builder()
                .start(LocalDateTime.now().plusHours(2))
                .end(LocalDateTime.now().plusHours(3))
                .itemId(1L)
                .build();

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

        itemDto = ItemRequestDto.builder()
                .name("ItemName")
                .description("ItemDescription")
                .available(true)
                .build();
    }

    @Test
    void create_shouldCreateBooking() {
        UserDto savedUser = userService.addUser(userDto);
        itemService.addItem(itemDto, savedUser.getId());
        UserDto savedBooker = userService.addUser(user2Dto);

        BookingResponseDto createdBooking = bookingService.createBooking(bookingDtoInput, savedBooker.getId());

        assertEquals(1, createdBooking.getId());
        assertEquals(bookingDtoInput.getStart(), createdBooking.getStart());
        assertEquals(bookingDtoInput.getEnd(), createdBooking.getEnd());
        assertEquals(1, createdBooking.getItem().getId());
        assertEquals(itemDto.getName(), createdBooking.getItem().getName());
        assertEquals(2, createdBooking.getBooker().getId());
        assertEquals(user2Dto.getName(), createdBooking.getBooker().getName());
    }

    @Test
    void create_shouldThrowDataNotFoundException_WhenUserNotExist() {
        NotFoundException dataNotFoundException = assertThrows(NotFoundException.class,
                () -> bookingService.createBooking(bookingDtoInput, 999L));

        assertEquals("user", dataNotFoundException.getMessage());
    }

    @Test
    void create_shouldThrowDataNotFoundException_WhenItemNotExist() {
        userService.addUser(userDto);
        userService.addUser(user2Dto);

        NotFoundException dataNotFoundException = assertThrows(NotFoundException.class,
                () -> bookingService.createBooking(bookingDtoInput, 2L));

        assertEquals("item", dataNotFoundException.getMessage());
    }

    @Test
    void create_shouldThrowDataNotFoundException_WhenBookerIsOwner() {
        userService.addUser(userDto);
        userService.addUser(user2Dto);
        itemService.addItem(itemDto, 1L);

        NotFoundException dataNotFoundException = assertThrows(NotFoundException.class,
                () -> bookingService.createBooking(bookingDtoInput, 1L));

        assertEquals("Зачем хозяину вещи делать запрос на ее бронирование?"
                , dataNotFoundException.getMessage());
    }

    @Test
    void create_shouldThrowAvailableException_WhenItemNotAvailable() {
        itemDto.setAvailable(false);
        userService.addUser(userDto);
        userService.addUser(user2Dto);
        itemService.addItem(itemDto, 1L);

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
                () -> bookingService.createBooking(bookingDtoInput, 2L));

        assertEquals("Нельзя забронировать недоступную вещь", badRequestException.getMessage());
    }

    @Test
    void updateStatus_shouldThrowDataNotFoundException_WhenUserNotExist() {
        userService.addUser(userDto);
        userService.addUser(user2Dto);
        itemService.addItem(itemDto, 1L);
        bookingService.createBooking(bookingDtoInput, 2L);

        NotFoundException dataNotFoundException = assertThrows(NotFoundException.class,
                () -> bookingService.approveBooking(1L, 1000L, true));

        assertEquals("user", dataNotFoundException.getMessage());
    }

    @Test
    void updateStatus_shouldThrowDataNotFoundException_WhenBookingNotExist() {
        userService.addUser(userDto);
        userService.addUser(user2Dto);
        itemService.addItem(itemDto, 1L);

        NotFoundException dataNotFoundException = assertThrows(NotFoundException.class,
                () -> bookingService.approveBooking(10000L, 2L, true));

        assertEquals("booking", dataNotFoundException.getMessage());
    }

    @Test
    void updateStatus_shouldThrowWrongAccessException_WhenNotOwner() {
        UserDto savedOwner = userService.addUser(userDto);
        itemService.addItem(itemDto, savedOwner.getId());
        UserDto savedBooker = userService.addUser(user2Dto);
        BookingResponseDto createdBooking = bookingService.createBooking(bookingDtoInput, savedBooker.getId());

        NotFoundException notFoundException = assertThrows(NotFoundException.class,
                () -> bookingService.approveBooking(createdBooking.getId(), savedBooker.getId(), true));

        assertEquals("Редактирование брони не владельцем вещи, поэтому ее как бы нет",
                notFoundException.getMessage());
    }

    @Test
    void updateStatus_shouldUpdateStatus() {
        UserDto savedOwner = userService.addUser(userDto);
        itemService.addItem(itemDto, savedOwner.getId());
        UserDto savedBooker = userService.addUser(user2Dto);
        BookingResponseDto createdBooking = bookingService.createBooking(bookingDtoInput, savedBooker.getId());

        BookingResponseDto returnedBooking = bookingService.approveBooking(createdBooking.getId(), savedOwner.getId(),
                true);

        assertEquals(Status.APPROVED, returnedBooking.getStatus());
        assertEquals(1, returnedBooking.getId());
    }

    @Test
    void getBooking_shouldThrowDataNotFoundException_WhenUserNotExist() {
        userService.addUser(userDto);
        userService.addUser(user2Dto);
        itemService.addItem(itemDto, 1L);
        bookingService.createBooking(bookingDtoInput, 2L);

        NotFoundException notFoundException = assertThrows(NotFoundException.class,
                () -> bookingService.getBooking(1L, 819L));

        assertEquals("user", notFoundException.getMessage());
    }

    @Test
    void getBooking_shouldThrowDataNotFoundException_WhenBookingNotFindByUser() {
        UserDto savedOwner = userService.addUser(userDto);
        itemService.addItem(itemDto, savedOwner.getId());
        userService.addUser(user2Dto);
        UserDto user3Dto = UserDto.builder()
                .id(3L)
                .name("ThirdUser")
                .email("thirdUser@email.com")
                .build();
        UserDto anotherBooker = userService.addUser(user3Dto);
        bookingService.createBooking(bookingDtoInput, anotherBooker.getId());

        NotFoundException dataNotFoundException = assertThrows(NotFoundException.class,
                () -> bookingService.getBooking(2L, 1L));

        assertEquals("booking", dataNotFoundException.getMessage());
    }

    @Test
    void getBooking_shouldThrowDataNotFoundException_WhenBookingNotExist() {
        userService.addUser(userDto);
        userService.addUser(user2Dto);

        NotFoundException dataNotFoundException = assertThrows(NotFoundException.class,
                () -> bookingService.getBooking(10000L, 2L));

        assertEquals("booking", dataNotFoundException.getMessage());
    }

    @Test
    void getBooking_shouldGetBooking() {
        UserDto savedOwner = userService.addUser(userDto);
        itemService.addItem(itemDto, savedOwner.getId());
        UserDto savedBooker = userService.addUser(user2Dto);
        BookingResponseDto createdBooking = bookingService.createBooking(bookingDtoInput, savedBooker.getId());

        BookingResponseDto returnedBooking = bookingService.getBooking(1L, 2L);

        assertEquals(createdBooking.getId(), returnedBooking.getId());
        assertEquals(createdBooking.getItem().getId(), returnedBooking.getItem().getId());
        assertEquals(createdBooking.getBooker().getId(), returnedBooking.getBooker().getId());
    }

    @Test
    void getAllBookerBookings_shouldThrowDataNotFoundException_WhenUserNotExist() {
        NotFoundException dataNotFoundException = assertThrows(NotFoundException.class,
                () -> bookingService.getBookingByBooker(999L, State.ALL, 0, 10));

        assertEquals("user", dataNotFoundException.getMessage());
    }

    @Test
    void getAllBookerBookings_shouldThrowWrongStatusException_WhenWrongState() {
        userService.addUser(userDto);
        IllegalArgumentException wrongStatusException = assertThrows(IllegalArgumentException.class,
                () -> bookingService.getBookingByBooker(1L, State.UNSUPPORTED_STATUS, 0, 10));

        assertEquals("Unknown state: UNSUPPORTED_STATUS", wrongStatusException.getMessage());
    }

    @Test
    void getAllBookerBookings_WhenStateALL() {
        UserDto savedOwner = userService.addUser(userDto);
        itemService.addItem(itemDto, savedOwner.getId());
        UserDto savedBooker = userService.addUser(user2Dto);
        BookingResponseDto currentBooking = bookingService.createBooking(bookingDtoInput, savedBooker.getId());
        BookingResponseDto previousBooking = bookingService.createBooking(previousBookingDto, savedBooker.getId());
        BookingResponseDto futureBooking = bookingService.createBooking(futureBookingDto, savedBooker.getId());

        List<BookingResponseDto> returnedBookings = bookingService.getBookingByBooker(2L,
                State.ALL, 0, 10);

        assertEquals(3, returnedBookings.size());
        assertEquals(futureBooking.getId(), returnedBookings.get(0).getId());
        assertEquals(currentBooking.getId(), returnedBookings.get(1).getId());
        assertEquals(previousBooking.getId(), returnedBookings.get(2).getId());
    }

    @Test
    void getAllBookerBookings_WhenStateCURRENT() {
        UserDto savedOwner = userService.addUser(userDto);
        itemService.addItem(itemDto, savedOwner.getId());
        UserDto savedBooker = userService.addUser(user2Dto);
        BookingResponseDto currentBooking = bookingService.createBooking(bookingDtoInput, savedBooker.getId());
        bookingService.createBooking(previousBookingDto, savedBooker.getId());
        bookingService.createBooking(futureBookingDto, savedBooker.getId());

        List<BookingResponseDto> returnedBookings = bookingService.getBookingByBooker(2L, State.CURRENT,
                0, 10);

        assertEquals(1, returnedBookings.size());
        assertEquals(currentBooking.getId(), returnedBookings.get(0).getId());
    }

    @Test
    void getAllBookerBookings_WhenStateWAITING_or_REJECTED() {
        UserDto savedOwner = userService.addUser(userDto);
        itemService.addItem(itemDto, savedOwner.getId());
        UserDto savedBooker = userService.addUser(user2Dto);
        BookingResponseDto currentBooking = bookingService.createBooking(bookingDtoInput, savedBooker.getId());
        BookingResponseDto previousBooking = bookingService.createBooking(previousBookingDto, savedBooker.getId());
        BookingResponseDto futureBooking = bookingService.createBooking(futureBookingDto, savedBooker.getId());

        List<BookingResponseDto> returnedBookings = bookingService.getBookingByBooker(2L,
                State.WAITING, 0, 10);

        assertEquals(3, returnedBookings.size());
        assertEquals(futureBooking.getId(), returnedBookings.get(0).getId());
        assertEquals(currentBooking.getId(), returnedBookings.get(1).getId());
        assertEquals(previousBooking.getId(), returnedBookings.get(2).getId());
    }

    @Test
    void getAllOwnerItemBookings_shouldThrowDataNotFoundException_WhenUserNotExist() {
        NotFoundException dataNotFoundException = assertThrows(NotFoundException.class,
                () -> bookingService.getBookingByBooker(999L, State.ALL, 0, 10));

        assertEquals("user", dataNotFoundException.getMessage());
    }

    @Test
    void getAllOwnerItemBookings_WhenStateALL() {
        UserDto savedOwner = userService.addUser(userDto);
        itemService.addItem(itemDto, savedOwner.getId());
        UserDto savedBooker = userService.addUser(user2Dto);
        BookingResponseDto currentBooking = bookingService.createBooking(bookingDtoInput, savedBooker.getId());
        BookingResponseDto previousBooking = bookingService.createBooking(previousBookingDto, savedBooker.getId());
        BookingResponseDto futureBooking = bookingService.createBooking(futureBookingDto, savedBooker.getId());

        List<BookingResponseDto> returnedBookings = bookingService.getBookingByBooker(2L, State.ALL, 0, 10);

        assertEquals(3, returnedBookings.size());
        assertEquals(futureBooking.getId(), returnedBookings.get(0).getId());
        assertEquals(currentBooking.getId(), returnedBookings.get(1).getId());
        assertEquals(previousBooking.getId(), returnedBookings.get(2).getId());
    }

    @Test
    void getAllOwnerItemBookings_WhenStateCURRENT() {
        UserDto savedOwner = userService.addUser(userDto);
        itemService.addItem(itemDto, savedOwner.getId());
        UserDto savedBooker = userService.addUser(user2Dto);
        BookingResponseDto currentBooking = bookingService.createBooking(bookingDtoInput, savedBooker.getId());
        bookingService.createBooking(previousBookingDto, savedBooker.getId());
        bookingService.createBooking(futureBookingDto, savedBooker.getId());

        List<BookingResponseDto> returnedBookings = bookingService.getBookingByBooker(2L,
                State.CURRENT, 0, 10);

        assertEquals(1, returnedBookings.size());
        assertEquals(currentBooking.getId(), returnedBookings.get(0).getId());
    }
}