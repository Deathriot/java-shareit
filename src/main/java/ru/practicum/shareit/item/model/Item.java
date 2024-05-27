package ru.practicum.shareit.item.model;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.request.model.ItemRequest;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@Builder
public class Item {
    private Long id;
    @NotBlank
    @Size(max = 32)
    private String name;
    @NotBlank
    @Size(max = 256)
    private String description;
    @NotNull
    private Boolean available;
    @NotNull
    private Long owner;
}