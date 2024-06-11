package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.comment.CommentRequestDto;
import ru.practicum.shareit.item.dto.comment.CommentResponseDto;
import ru.practicum.shareit.item.dto.item.ItemRequestDto;
import ru.practicum.shareit.item.dto.item.ItemResponseDto;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemController {

    private final ItemService itemService;
    private static final String SHARER_USER_ID = "X-Sharer-User-Id";

    @PostMapping
    public ItemResponseDto postItem(@Valid @RequestBody ItemRequestDto itemDto,
                                    @RequestHeader(value = SHARER_USER_ID) long userId) {
        log.info("postItem");
        return itemService.addItem(itemDto, userId);
    }

    @PatchMapping("/{itemId}")
    public ItemResponseDto patchItem(@RequestBody ItemRequestDto itemDto, @RequestHeader(SHARER_USER_ID) long userId,
                                     @PathVariable long itemId) {
        log.info("patchItem");
        return itemService.updateItem(itemDto, userId, itemId);
    }

    @GetMapping("/{itemId}")
    public ItemResponseDto getItemById(@RequestHeader(SHARER_USER_ID) long userId,
                                       @PathVariable long itemId) {
        log.info("getItemById");
        return itemService.getItemById(itemId, userId);
    }

    @GetMapping
    public List<ItemResponseDto> getItems(@RequestHeader("X-Sharer-User-Id") long userId,
                                          @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
                                          @RequestParam(defaultValue = "10") @Positive Integer size) {
        log.info("getItems");
        return itemService.getItems(userId, from, size);
    }

    @GetMapping("/search")
    public List<ItemResponseDto> getItemsSearch(@RequestParam(defaultValue = "") String text,
                                                @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
                                                @RequestParam(defaultValue = "10") @Positive Integer size) {
        log.info("getItemsSearch");
        return itemService.getItemsSearch(text, from, size);
    }

    @PostMapping("/{itemId}/comment")
    public CommentResponseDto createComment(
            @RequestBody @Valid CommentRequestDto commentDto,
            @PathVariable Long itemId,
            @RequestHeader(SHARER_USER_ID) Long userId) {

        log.info("createComment");
        return itemService.createComment(commentDto, userId, itemId);
    }
}