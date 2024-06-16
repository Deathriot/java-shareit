package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.item.ItemMapper;
import ru.practicum.shareit.item.dto.item.ItemResponseDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.RequestDto;
import ru.practicum.shareit.request.dto.RequestMapper;
import ru.practicum.shareit.request.dto.RequestResponseDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.RequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.util.PageBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RequestServiceImpl implements RequestService {
    private final RequestRepository repository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    public RequestResponseDto createRequest(RequestDto requestDto, Long userId) {
        User user = getUser(userId);
        ItemRequest itemRequest = RequestMapper.toItemRequest(requestDto, user);

        return RequestMapper.toRequestDto(repository.save(itemRequest), null);
    }

    @Override
    @Transactional(readOnly = true)
    public RequestResponseDto getRequest(Long requestId, Long userid) {
        getUser(userid);
        ItemRequest request = repository.findById(requestId).orElseThrow(() -> new NotFoundException("request"));

        List<Item> items = itemRepository.findAllByRequestId(requestId);
        List<ItemResponseDto> itemsDto = items.stream().map(ItemMapper::toItemDtoWithRequest)
                .collect(Collectors.toList());

        return RequestMapper.toRequestDto(request, itemsDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RequestResponseDto> getAllByOwner(Long userId) {
        getUser(userId);
        List<ItemRequest> requests = repository.findAllByUserId(userId);
        List<Item> items = itemRepository.findAllByRequestIn(requests);

        Map<Long, List<ItemResponseDto>> itemsByRequest = itemsToItemsByRequest(items);

        return requests
                .stream()
                .map(request -> RequestMapper.toRequestDto(request,
                        itemsByRequest.getOrDefault(request.getId(), new ArrayList<>())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RequestResponseDto> getAll(Long userId, int from, int size) {
        getUser(userId);

        Sort sort = Sort.by(Sort.Direction.DESC, "created");
        Pageable pageable = PageBuilder.getPageable(from, size, sort);

        List<ItemRequest> requests = repository.findAllByUserIdNot(userId, pageable);
        List<Item> items = itemRepository.findAllByRequestIn(requests);

        Map<Long, List<ItemResponseDto>> itemsByRequest = itemsToItemsByRequest(items);

        return requests
                .stream()
                .map(request -> RequestMapper.toRequestDto(request,
                        itemsByRequest.getOrDefault(request.getId(), new ArrayList<>())))
                .collect(Collectors.toList());
    }

    private User getUser(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("user"));
    }

    private Map<Long, List<ItemResponseDto>> itemsToItemsByRequest(List<Item> items) {
        final Map<Long, List<ItemResponseDto>> itemsMap = new HashMap<>();

        List<ItemResponseDto> itemsDto = items.stream()
                .map(ItemMapper::toItemDtoWithRequest).collect(Collectors.toList());

        for (ItemResponseDto item : itemsDto) {
            Long requestId = item.getRequestId();
            List<ItemResponseDto> itemsRequest = itemsMap.getOrDefault(requestId, new ArrayList<>());

            itemsRequest.add(item);
            itemsMap.put(requestId, itemsRequest);
        }

        return itemsMap;
    }
}
