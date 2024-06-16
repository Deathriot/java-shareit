package ru.practicum.shareit.booking.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Future;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

import static ru.practicum.shareit.util.JsonPattern.DATE_PATTERN;

// ДТО для реквестов
@Data
@Builder
public class BookingRequestDto {
    @FutureOrPresent
    @NotNull
    @JsonFormat(pattern = DATE_PATTERN)
    private LocalDateTime start;

    @Future
    @NotNull
    @JsonFormat(pattern = DATE_PATTERN)
    private LocalDateTime end;

    @NotNull
    private Long itemId;
}
