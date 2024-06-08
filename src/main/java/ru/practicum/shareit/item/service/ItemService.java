package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.comment.CommentRequestDto;
import ru.practicum.shareit.item.dto.comment.CommentResponseDto;
import ru.practicum.shareit.item.dto.item.ItemRequestDto;
import ru.practicum.shareit.item.dto.item.ItemResponseDto;

import java.util.List;

public interface ItemService {
    ItemResponseDto addItem(ItemRequestDto itemDto, long userId);

    ItemResponseDto updateItem(ItemRequestDto itemDto, long userId, long itemId);

    ItemResponseDto getItemById(long itemId, long userId);

    List<ItemResponseDto> getItems(long userId);

    List<ItemResponseDto> getItemsSearch(String text);

    CommentResponseDto createComment(CommentRequestDto commentDto, long userId, long itemId);
}
