package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Slf4j
public final class ItemController {

    private final ItemService itemService;

    @PostMapping
    public ItemDto postItem(@Valid @RequestBody ItemDto itemDto, @RequestHeader(value = "X-Sharer-User-Id") long userId) {
        log.info("postItem");
        return itemService.addItem(itemDto, userId);
    }

    @PatchMapping("/{itemId}")
    public ItemDto patchItem(@RequestBody ItemDto itemDto, @RequestHeader("X-Sharer-User-Id") long userId,
                             @PathVariable long itemId) {
        log.info("patchItem");
        return itemService.updateItem(itemDto, userId, itemId);
    }

    @GetMapping("/{itemId}")
    public ItemDto getItemById(@PathVariable long itemId) {
        log.info("getItemById");
        return itemService.getItemById(itemId);
    }

    @GetMapping
    public List<ItemDto> getItems(@RequestHeader("X-Sharer-User-Id") long userId) {
        log.info("getItems");
        return itemService.getItems(userId);
    }

    @GetMapping("/search")
    public List<ItemDto> getItemsSearch(@RequestParam(defaultValue = "null") String text) {
        log.info("getItemsSearch");
        return itemService.getItemsSearch(text);
    }

}