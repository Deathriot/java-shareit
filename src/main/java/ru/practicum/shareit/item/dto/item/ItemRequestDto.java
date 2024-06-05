package ru.practicum.shareit.item.dto.item;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

// Дто для реквеста
@Data
@Builder
public class ItemRequestDto {
    @NotBlank
    @Size(max = 32)
    private String name;
    @NotBlank
    @Size(max = 256)
    private String description;

    @NotNull
    private Boolean available;
}
