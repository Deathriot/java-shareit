package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.comment.CommentRequestDto;
import ru.practicum.shareit.item.dto.comment.CommentResponseDto;
import ru.practicum.shareit.item.dto.item.ItemRequestDto;
import ru.practicum.shareit.item.dto.item.ItemResponseDto;

import java.util.List;

public interface ItemService {
    ItemResponseDto addItem(ItemRequestDto itemDto, Long userId);

    ItemResponseDto updateItem(ItemRequestDto itemDto, Long userId, Long itemId);

    ItemResponseDto getItemById(Long itemId, Long userId);

    List<ItemResponseDto> getItems(Long userId, Integer from, Integer size);

    List<ItemResponseDto> getItemsSearch(String text, Integer from, Integer size);

    CommentResponseDto createComment(CommentRequestDto commentDto, Long userId, Long itemId);
}
