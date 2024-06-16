package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.State;

import java.util.List;

public interface BookingService {
    BookingResponseDto createBooking(BookingRequestDto bookingDto, Long userId);

    BookingResponseDto approveBooking(Long bookingId, Long userId, Boolean available);

    BookingResponseDto getBooking(Long bookingId, Long userId);

    List<BookingResponseDto> getBookingByBooker(Long userId, State state, Integer from, Integer size);

    List<BookingResponseDto> getBookingItemsByOwner(Long userId, State state, Integer from, Integer size);
}
