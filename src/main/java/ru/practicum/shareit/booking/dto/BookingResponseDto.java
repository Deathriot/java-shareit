package ru.practicum.shareit.booking.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

// ДТО для ответа
@Data
@Builder
public class BookingResponseDto {
    private long id;

    private LocalDateTime start;

    private LocalDateTime end;

    private Status status;

    private User booker;

    private Item item;
}
