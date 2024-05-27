package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    ItemDto addItem(ItemDto itemDto, long userId);

    ItemDto updateItem(ItemDto itemDto, long userId, long itemId);

    ItemDto getItemById(long itemId);

    List<ItemDto> getItems(long userId);

    List<ItemDto> getItemsSearch(String text);
}
