package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    public BookingResponseDto createBooking(BookingRequestDto bookingDto, Long userId) {
        bookingTimeValidation(bookingDto);

        User user = getUser(userId);
        Item item = getItem(bookingDto.getItemId());

        if (!item.getAvailable()) {
            throw new BadRequestException("Нельзя забронировать недоступную вещь");
        }

        if (item.getOwner().getId() == userId) {
            throw new NotFoundException("Зачем хозяину вещи делать запрос на ее бронирование?");
        }

        Booking booking = repository.save(BookingMapper.toBooking(bookingDto, user, item));

        return BookingMapper.toBookingResponseDto(booking);
    }

    @Override
    @Transactional
    public BookingResponseDto approveBooking(Long bookingId, Long userId, Boolean approved) {
        Booking booking = repository.findById(bookingId).orElseThrow(() -> new NotFoundException("booking"));
        getUser(userId);
        Item item = booking.getItem();

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
    public BookingResponseDto getBooking(Long bookingId, Long userId) {
        Booking booking = repository.findById(bookingId).orElseThrow(() -> new NotFoundException("booking"));
        getUser(userId);

        if (booking.getBooker().getId() != userId && booking.getItem().getOwner().getId() != userId) {
            throw new NotFoundException("просмотр брони не ее владельцем или владельцем вещи, для остальных ее нет");
        }

        return BookingMapper.toBookingResponseDto(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponseDto> getBookingByBooker(Long userId, State state, Integer from, Integer size) {
        // Проверяем существует ли пользователь
        getUser(userId);

        Sort sort = Sort.by(Sort.Direction.DESC, "start");
        Pageable pageable = PageRequest.of(from == 0 ? 0 : from / size, size, sort);
        List<Booking> bookings = repository.findAllByBookerId(userId, pageable);

        return filterBookingsByState(bookings, state);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponseDto> getBookingItemsByOwner(Long userId, State state, Integer from, Integer size) {
        getUser(userId);

        Sort sort = Sort.by(Sort.Direction.DESC, "start");
        Pageable pageable = PageRequest.of(from == 0 ? 0 : from / size, size, sort);
        List<Booking> bookings = repository.findAllByItemOwnerId(userId, pageable);

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
                throw new IllegalArgumentException("Unknown state: " + state);
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

        if (start.isEqual(end) || start.equals(end)) {
            throw new BadRequestException("Время начала не может равным времени конца бронирования");
        }

        if (start.isAfter(end)) {
            throw new BadRequestException("Время конца не может быть до времени начала бронирования");
        }
    }
}
