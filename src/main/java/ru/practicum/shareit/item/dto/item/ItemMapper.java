package ru.practicum.shareit.item.dto.item;

import ru.practicum.shareit.booking.dto.BookingShortResponseDto;
import ru.practicum.shareit.item.dto.comment.CommentResponseDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public final class ItemMapper {
    private ItemMapper() {

    }

    // метод для возврата при создании или обновлении вещи
    public static ItemResponseDto toItemDto(Item item) {
        return ItemResponseDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .build();
    }

    // Метод для возврата для получения вещи с комментариями и букингами
    public static ItemResponseDto toItemResponseDto(Item item, BookingShortResponseDto last,
                                                    BookingShortResponseDto next, List<CommentResponseDto> comments) {
        return ItemResponseDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .lastBooking(last)
                .nextBooking(next)
                .comments(comments)
                .build();
    }

    // Метод для создания вещи из поступившего объекта в теле запроса
    public static Item toItem(ItemRequestDto itemDto, long itemId, User user) {
        return Item.builder()
                .id(itemId)
                .owner(user)
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable())
                .build();
    }

    // Метод для обновления вещи
    public static Item toUpdatedItem(Item item, ItemRequestDto itemDto) {
        return Item.builder()
                .id(item.getId())
                .owner(item.getOwner())
                .name(itemDto.getName() == null ? item.getName() : itemDto.getName())
                .description(itemDto.getDescription() == null ? item.getDescription() : itemDto.getDescription())
                .available(itemDto.getAvailable() == null ? item.getAvailable() : itemDto.getAvailable())
                .build();
    }

    // Метод для создания вещи, которая будет прикреплена к реквесту
    public static ItemResponseDto toItemDtoWithRequest(Item item) {
        return ItemResponseDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .requestId(item.getRequest() == null ? null : item.getRequest().getId())
                .build();
    }
}