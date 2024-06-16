package ru.practicum.shareit.request.dto;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.item.dto.item.ItemResponseDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

@UtilityClass
public class RequestMapper {

    public ItemRequest toItemRequest(RequestDto requestDto, User user) {
        return ItemRequest.builder()
                .description(requestDto.getDescription())
                .created(LocalDateTime.now())
                .user(user)
                .build();
    }

    public RequestResponseDto toRequestDto(ItemRequest request, List<ItemResponseDto> items) {
        return RequestResponseDto.builder()
                .id(request.getId())
                .created(request.getCreated())
                .description(request.getDescription())
                .items(items)
                .build();
    }
}