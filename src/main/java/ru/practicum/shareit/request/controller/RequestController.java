package ru.practicum.shareit.request.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.RequestDto;
import ru.practicum.shareit.request.dto.RequestResponseDto;
import ru.practicum.shareit.request.service.RequestService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequestMapping("/requests")
@AllArgsConstructor
@Slf4j
@Validated
public class RequestController {
    private final RequestService requestService;
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @PostMapping
    public RequestResponseDto createRequest(
            @RequestHeader(USER_ID_HEADER) Long ownerId,
            @Valid @RequestBody RequestDto requestDto) {

        log.info("createRequest");
        return requestService.createRequest(requestDto, ownerId);
    }

    @GetMapping("/{requestId}")
    public RequestResponseDto findRequestsById(
            @RequestHeader(USER_ID_HEADER) Long userId, @PathVariable Long requestId) {

        log.info("findRequestsById");
        return requestService.getRequest(requestId, userId);
    }

    @GetMapping
    public List<RequestResponseDto> findAllRequestsByOwnerId(@RequestHeader(USER_ID_HEADER) Long ownerId) {
        log.info("findAllRequestsByOwnerId");
        return requestService.getAllByOwner(ownerId);
    }

    @GetMapping("/all")
    public List<RequestResponseDto> findAllRequests(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive Integer size) {
        log.info("findAllRequests");
        return requestService.getAll(userId, from, size);
    }
}