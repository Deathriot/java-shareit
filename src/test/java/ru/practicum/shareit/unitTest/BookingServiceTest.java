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
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.State;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {
    @InjectMocks
    private BookingServiceImpl bookingService;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;

    private Booking booking;
    private Booking updatedBooking;
    private BookingRequestDto bookingDtoInput;
    private Booking booking1;
    private Booking booking2;
    private User user;
    private User user2;
    private Item item;
    private Pageable pageable;

    @BeforeEach
    void beforeEach() {
        bookingDtoInput = BookingRequestDto.builder()
                .start(LocalDateTime.now())
                .end(LocalDateTime.now().plusHours(1))
                .itemId(1L)
                .build();

        BookingRequestDto previousBooking = BookingRequestDto.builder()
                .start(LocalDateTime.of(2024, 3, 1, 10, 0))
                .end(LocalDateTime.of(2024, 3, 1, 11, 0))
                .itemId(1L)
                .build();

        BookingRequestDto futureBooking = BookingRequestDto.builder()
                .start(LocalDateTime.now().plusHours(2))
                .end(LocalDateTime.now().plusHours(3))
                .itemId(1L)
                .build();

        user = User.builder()
                .id(1L)
                .name("User")
                .email("user@email.com")
                .build();

        user2 = User.builder()
                .id(2L)
                .name("AnotherUser")
                .email("AnotherUser@email.com")
                .build();

        item = Item.builder()
                .id(1L)
                .description("ItemDescription")
                .available(true)
                .owner(user)
                .build();

        booking = BookingMapper.toBooking(bookingDtoInput, user2, item);
        booking.setId(1L);
        booking1 = BookingMapper.toBooking(previousBooking, user2, item);
        booking1.setId(2L);
        booking2 = BookingMapper.toBooking(futureBooking, user2, item);
        booking2.setId(3L);
        updatedBooking = BookingMapper.toBooking(bookingDtoInput, user2, item);
        updatedBooking.setId(1L);

        Sort sort = Sort.by(Sort.Direction.DESC, "start");
        pageable = PageRequest.of(0, 10, sort);
    }

    @Test
    void createShouldCreateBooking() {
        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        Mockito.when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        Mockito.when(bookingRepository.save(Mockito.any())).thenReturn(booking);

        BookingResponseDto createdBooking = bookingService.createBooking(bookingDtoInput, 2L);

        Mockito.verify(bookingRepository).save(Mockito.any());

        assertEquals(1L, createdBooking.getId());
        assertEquals(bookingDtoInput.getStart(), createdBooking.getStart());
        assertEquals(bookingDtoInput.getEnd(), createdBooking.getEnd());
        assertEquals(item.getId(), createdBooking.getItem().getId());
        assertEquals(item.getName(), createdBooking.getItem().getName());
        assertEquals(user2.getId(), createdBooking.getBooker().getId());
        assertEquals(user2.getName(), createdBooking.getBooker().getName());
    }

    @Test
    void createShouldThrowDataNotFoundExceptionWhenUserNotExist() {
        Mockito.when(userRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> bookingService.createBooking(bookingDtoInput, 999L));

        assertEquals("user", exception.getMessage());
    }

    @Test
    void createShouldThrowDataNotFoundExceptionWhenItemNotExist() {
        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        Mockito.when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException dataNotFoundException = assertThrows(NotFoundException.class,
                () -> bookingService.createBooking(bookingDtoInput, 2L));

        assertEquals("item", dataNotFoundException.getMessage());
    }

    @Test
    void createShouldThrowDataNotFoundExceptionWhenBookerIsOwner() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Mockito.when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        NotFoundException dataNotFoundException = assertThrows(NotFoundException.class,
                () -> bookingService.createBooking(bookingDtoInput, 1L));

        assertEquals("Зачем хозяину вещи делать запрос на ее бронирование?",
                dataNotFoundException.getMessage());
    }

    @Test
    void createShouldThrowAvailableExceptionWhenItemNotAvailable() {
        item.setAvailable(false);
        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        Mockito.when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> bookingService.createBooking(bookingDtoInput, 2L));

        assertEquals("Нельзя забронировать недоступную вещь", exception.getMessage());
    }

    @Test
    void updateStatusShouldThrowDataNotFoundExceptionWhenBookingNotExist() {
        Mockito.when(bookingRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException dataNotFoundException = assertThrows(NotFoundException.class,
                () -> bookingService.approveBooking(999L, 2L, true));

        assertEquals("booking", dataNotFoundException.getMessage());
    }

    @Test
    void updateStatusShouldThrowWrongStatusException_WhenStatusNotWaiting() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Mockito.when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        booking.setStatus(Status.APPROVED);

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> bookingService.approveBooking(1L, 1L, true));

        assertEquals("Нельзя менять статус брони, после того как ее одобрили", exception.getMessage());
    }

    @Test
    void updateStatusShouldUpdateStatus() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Mockito.when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        Mockito.when(bookingRepository.save(Mockito.any())).thenReturn(updatedBooking);

        BookingResponseDto returnedBooking = bookingService.approveBooking(1L, 1L, true);

        Mockito.verify(bookingRepository).save(Mockito.any());

        assertEquals(Status.APPROVED, returnedBooking.getStatus());
        assertEquals(booking.getId(), returnedBooking.getId());
    }

    @Test
    void getBookingShouldThrowDataNotFoundException_WhenUserNotExist() {
        Mockito.when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        NotFoundException dataNotFoundException = assertThrows(NotFoundException.class,
                () -> bookingService.getBooking(1L, 999L));

        assertEquals("user", dataNotFoundException.getMessage());
    }

    @Test
    void getBookingShouldThrowDataNotFoundException_WhenBookingNotFindByUser() {
        booking.setBooker(user);
        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        Mockito.when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        NotFoundException dataNotFoundException = assertThrows(NotFoundException.class,
                () -> bookingService.getBooking(1L, 2L));

        assertEquals("просмотр брони не ее владельцем или владельцем вещи, для остальных ее нет",
                dataNotFoundException.getMessage());
    }

    @Test
    void getBookingShouldGetBooking() {
        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        Mockito.when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        BookingResponseDto returnedBooking = bookingService.getBooking(1L, 2L);

        assertEquals(booking.getId(), returnedBooking.getId());
        assertEquals(booking.getItem().getId(), returnedBooking.getItem().getId());
        assertEquals(booking.getBooker().getId(), returnedBooking.getBooker().getId());
    }

    @Test
    void getAllBookerBookingsShouldThrowDataNotFoundExceptionWhenUserNotExist() {
        Mockito.when(userRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException dataNotFoundException = assertThrows(NotFoundException.class,
                () -> bookingService.getBookingByBooker(999L, State.ALL, 0, 10));

        assertEquals("user", dataNotFoundException.getMessage());
    }

    @Test
    void getAllBookerBookingsWhenStateALL() {
        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        Mockito.when(bookingRepository.findAllByBookerId(2L, pageable))
                .thenReturn(List.of(booking, booking1, booking2));

        List<BookingResponseDto> returnedBookings = bookingService.getBookingByBooker(2L,
                State.ALL, 0, 10);

        assertEquals(3, returnedBookings.size());
        assertEquals(booking.getId(), returnedBookings.get(0).getId());
        assertEquals(booking1.getId(), returnedBookings.get(1).getId());
        assertEquals(booking2.getId(), returnedBookings.get(2).getId());
    }

    @Test
    void getAllOwnerItemBookingsWhenStateALL() {
        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        Mockito.when(bookingRepository.findAllByItemOwnerId(2L, pageable))
                .thenReturn(List.of(booking, booking1, booking2));

        List<BookingResponseDto> returnedBookings = bookingService.getBookingItemsByOwner(2L,
                State.ALL, 0, 10);

        assertEquals(3, returnedBookings.size());
        assertEquals(booking.getId(), returnedBookings.get(0).getId());
        assertEquals(booking1.getId(), returnedBookings.get(1).getId());
        assertEquals(booking2.getId(), returnedBookings.get(2).getId());
    }

    @Test
    void createBookingShouldReturnBadRequestExceptionWhenStartEqualsEnd() {
        LocalDateTime start = LocalDateTime.now();

        BookingRequestDto wrongBooking = BookingRequestDto.builder()
                .start(start)
                .end(start)
                .itemId(1L)
                .build();

        BadRequestException ex =
                assertThrows(BadRequestException.class, () -> bookingService.createBooking(wrongBooking, 2L));
        assertEquals("Время начала не может равным времени конца бронирования", ex.getMessage());
    }

    @Test
    void createBookingShouldReturnBadRequestExceptionWhenStartAfterEnd() {
        LocalDateTime start = LocalDateTime.now();

        BookingRequestDto wrongBooking = BookingRequestDto.builder()
                .start(start.plusHours(1))
                .end(start)
                .itemId(1L)
                .build();

        BadRequestException ex =
                assertThrows(BadRequestException.class, () -> bookingService.createBooking(wrongBooking, 2L));
        assertEquals("Время конца не может быть до времени начала бронирования", ex.getMessage());
    }
}
