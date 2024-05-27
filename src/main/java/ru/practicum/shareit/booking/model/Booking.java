package ru.practicum.shareit.booking.model;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
public class Booking {
    private Long id;
    @Future
    private LocalDateTime start;
    @Future
    private LocalDateTime end;
    @NotNull
    private Item item;
    @NotNull
    private User booker;
    //@NotNull
    // private StatusBooking status;
}