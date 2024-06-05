package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.State;

import java.util.List;

public interface BookingService {
    BookingResponseDto createBooking(BookingRequestDto bookingDto, long userId);

    BookingResponseDto approveBooking(long bookingId, long userId, boolean available);

    BookingResponseDto getBooking(long bookingId, long userId);

    List<BookingResponseDto> getBookingByBooker(long userId, State state);

    List<BookingResponseDto> getBookingItemsByOwner(long userId, State state);
}
