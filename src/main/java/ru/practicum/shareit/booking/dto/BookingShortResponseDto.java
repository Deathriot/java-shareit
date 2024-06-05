package ru.practicum.shareit.booking.dto;

import lombok.Builder;
import lombok.Data;

//Дто специально для предмета, когда нужно указать его ближайшие букинги
@Data
@Builder
public class BookingShortResponseDto {
    private Long id;

    private Long bookerId;
}
