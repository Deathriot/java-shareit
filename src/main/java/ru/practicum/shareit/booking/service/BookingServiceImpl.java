package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.State;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.UnsupportedStateException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository repository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public BookingResponseDto createBooking(BookingRequestDto bookingDto, long userId) {
        bookingTimeValidation(bookingDto);

        Item item = getItem(bookingDto.getItemId());

        if (!item.getAvailable()) {
            throw new BadRequestException("Нельзя забронировать недоступную вещь");
        }

        User user = getUser(userId);

        if (item.getOwner().getId() == userId) {
            throw new NotFoundException("Зачем хозяину вещи делать запрос на ее бронирование?");
        }

        Booking booking = repository.save(BookingMapper.toBooking(bookingDto, user, item));

        return BookingMapper.toBookingResponseDto(booking);
    }

    @Override
    @Transactional
    public BookingResponseDto approveBooking(long bookingId, long userId, boolean approved) {
        Booking booking = repository.findById(bookingId).orElseThrow(() -> new NotFoundException("booking"));
        Item item = booking.getItem();
        getUser(userId);

        if (item.getOwner().getId() != userId) {
            throw new NotFoundException("Редактирование брони не владельцем вещи, поэтому ее как бы нет");
        }

        if (booking.getStatus().equals(Status.APPROVED)) {
            throw new BadRequestException("Нельзя менять статус брони, после того как ее одобрили");
        }

        booking.setStatus(approved ? Status.APPROVED : Status.REJECTED);
        repository.save(booking);
        return BookingMapper.toBookingResponseDto(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponseDto getBooking(long bookingId, long userId) {
        Booking booking = repository.findById(bookingId).orElseThrow(() -> new NotFoundException("booking"));
        getUser(userId);

        if (booking.getBooker().getId() != userId && booking.getItem().getOwner().getId() != userId) {
            throw new NotFoundException("просмотр брони не ее владельцем или владельцем вещи, для остальных ее нет");
        }

        return BookingMapper.toBookingResponseDto(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponseDto> getBookingByBooker(long userId, State state) {
        // Проверяем существует ли пользователь
        getUser(userId);

        List<Booking> bookings = repository.findAllByBookerIdOrderByStartDesc(userId);

        return filterBookingsByState(bookings, state);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponseDto> getBookingItemsByOwner(long userId, State state) {
        getUser(userId);

        List<Booking> bookings = repository.findAllByItemOwnerIdOrderByStartDesc(userId);

        return filterBookingsByState(bookings, state);
    }


    private List<BookingResponseDto> filterBookingsByState(List<Booking> bookings, State state) {
        LocalDateTime now = LocalDateTime.now();

        List<BookingResponseDto> response = bookings
                .stream()
                .map(BookingMapper::toBookingResponseDto)
                .collect(Collectors.toList());

        switch (state) {
            case ALL:
                return response;
            case PAST:
                return response.stream()
                        .filter(booking -> booking.getEnd().isBefore(now)).collect(Collectors.toList());
            case FUTURE:
                return response.stream()
                        .filter(booking -> booking.getStart().isAfter(now)).collect(Collectors.toList());
            case CURRENT:
                return response.stream()
                        .filter(booking -> booking.getStart().isBefore(now) && booking.getEnd().isAfter(now))
                        .collect(Collectors.toList());
            case WAITING:
                return response.stream().filter(booking -> booking.getStatus().equals(Status.WAITING))
                        .collect(Collectors.toList());
            case REJECTED:
                return response.stream().filter(booking -> booking.getStatus().equals(Status.REJECTED))
                        .collect(Collectors.toList());
            default:
                // У нас тут тесты резко захотели проверять сообщение об ошибке, будем выкручиваться
                throw new UnsupportedStateException("Unknown state: " + String.valueOf(state));
        }
    }

    private User getUser(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("user"));
    }

    private Item getItem(long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("item"));
    }

    private void bookingTimeValidation(BookingRequestDto booking) {
        final LocalDateTime start = booking.getStart();
        final LocalDateTime end = booking.getEnd();

        if (start.isEqual(end)) {
            throw new BadRequestException("Время начала не может равным времени конца бронирования");
        }

        if (start.isAfter(end)) {
            throw new BadRequestException("Время конца не может быть до времени начала бронирования");
        }
    }
}
