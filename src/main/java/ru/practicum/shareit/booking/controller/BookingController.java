package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.State;
import ru.practicum.shareit.booking.service.BookingService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {
    private final BookingService service;
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @PostMapping
    public BookingResponseDto createBooking(@RequestHeader(USER_ID_HEADER) Long userId,
                                            @Valid @RequestBody BookingRequestDto bookingDto) {
        log.info("createBooking");
        return service.createBooking(bookingDto, userId);
    }

    @PatchMapping("/{bookingId}")
    public BookingResponseDto approveBooking(@RequestHeader(USER_ID_HEADER) Long userId,
                                             @PathVariable Long bookingId,
                                             @RequestParam boolean approved) {
        log.info("approveBooking");
        return service.approveBooking(bookingId, userId, approved);
    }


    @GetMapping("/{bookingId}")
    public BookingResponseDto findBookingById(@RequestHeader(USER_ID_HEADER) Long userId,
                                              @PathVariable Long bookingId) {
        log.info("findBookingById");
        return service.getBooking(bookingId, userId);
    }

    @GetMapping
    public List<BookingResponseDto> findUserBookings(@RequestHeader(USER_ID_HEADER) Long userId,
                                                     @RequestParam(defaultValue = "ALL") State state,
                                                     @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
                                                     @RequestParam(defaultValue = "10") @Positive Integer size) {
        log.info("findUserBookings");
        return service.getBookingByBooker(userId, state, from, size);
    }

    @GetMapping("/owner")
    public List<BookingResponseDto> findByItemOwnerBookings(@RequestHeader(USER_ID_HEADER) Long userId,
                                                            @RequestParam(defaultValue = "ALL") State state,
                                                            @RequestParam(defaultValue = "0")
                                                            @PositiveOrZero Integer from,
                                                            @RequestParam(defaultValue = "10") @Positive Integer size) {
        log.info("findByItemOwnerBookings");
        return service.getBookingItemsByOwner(userId, state, from, size);
    }
}