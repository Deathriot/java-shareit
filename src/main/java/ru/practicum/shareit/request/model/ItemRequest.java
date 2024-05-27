package ru.practicum.shareit.request.model;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.user.model.User;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
public class ItemRequest {
    private Long id;
    @NotBlank
    @Size(max = 256)
    private String description;
    @NotNull
    private User requester;
    @PastOrPresent
    private LocalDateTime created;
}