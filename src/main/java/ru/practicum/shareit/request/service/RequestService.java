package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.RequestDto;
import ru.practicum.shareit.request.dto.RequestResponseDto;

import java.util.List;

public interface RequestService {
    RequestResponseDto createRequest(RequestDto requestDto, Long userId);

    RequestResponseDto getRequest(Long requestId, Long userid);

    List<RequestResponseDto> getAllByOwner(Long userId);

    List<RequestResponseDto> getAll(Long userId, int from, int size);
}
