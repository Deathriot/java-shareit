package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingShortResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.comment.CommentMapper;
import ru.practicum.shareit.item.dto.comment.CommentRequestDto;
import ru.practicum.shareit.item.dto.comment.CommentResponseDto;
import ru.practicum.shareit.item.dto.item.ItemMapper;
import ru.practicum.shareit.item.dto.item.ItemRequestDto;
import ru.practicum.shareit.item.dto.item.ItemResponseDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.RequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository repository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final RequestRepository requestRepository;

    @Override
    @Transactional
    public ItemResponseDto addItem(ItemRequestDto itemDto, Long userId) {
        User user = userIdValidation(userId);

        Item item = ItemMapper.toItem(itemDto, 0, user);

        if (itemDto.getRequestId() != null) {
            ItemRequest request = requestRepository.findById(itemDto.getRequestId())
                    .orElseThrow(() -> new NotFoundException("request"));

            item.setRequest(request);
        }

        return ItemMapper.toItemDtoWithRequest(repository.save(item));
    }

    @Override
    @Transactional
    public ItemResponseDto updateItem(ItemRequestDto itemDto, Long userId, Long itemId) {
        userIdValidation(userId);
        Item item = repository.findById(itemId).orElseThrow(() -> new NotFoundException("Item id = " + itemId));

        if (!item.getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("Редактирование Пользователем id = " + userId + ", вещи id = " + itemId);
        }

        return ItemMapper.toItemDto(repository.save(ItemMapper.toUpdatedItem(item, itemDto)));
    }

    @Override
    @Transactional(readOnly = true)
    public ItemResponseDto getItemById(Long itemId, Long userId) {
        Item item = repository.findById(itemId).orElseThrow(() -> new NotFoundException("Item id = " + itemId));
        userIdValidation(userId);

        List<CommentResponseDto> comments = commentRepository.findAllByItemId(itemId)
                .stream().map(CommentMapper::toCommentDto).collect(Collectors.toList());

        // Если вещь просматривает не ее владелец - скрываем ближайшие брони
        if (!item.getOwner().getId().equals(userId)) {
            return ItemMapper.toItemResponseDto(item, null, null, comments);
        }

        List<Booking> bookingsOfItem = bookingRepository.findAllByItemId(itemId);
        List<BookingShortResponseDto> closestBooking = findClosestBookings(bookingsOfItem).get(itemId);

        if (closestBooking == null) {
            return ItemMapper.toItemResponseDto(item, null, null, comments);
        }

        return ItemMapper.toItemResponseDto(item, closestBooking.get(0), closestBooking.get(1), comments);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemResponseDto> getItems(Long userId, Integer from, Integer size) {
        userIdValidation(userId);

        Pageable pageable = PageRequest.of(from == 0 ? 0 : from / size, size);
        // Получаем список вещей пользователя
        List<Item> ownersItems = repository.findAllByOwnerId(userId, pageable);
        // Получаем все бронирования для всех этих вещей
        List<Booking> bookingsByItems = bookingRepository.findAllByItemIn(ownersItems);
        // Получаем мапу, где ключ - это айди вещи, а значение - пара из самых ближайших ее бронирований
        // Весь этот ужас сделан, чтобы избежать N + 1
        Map<Long, List<BookingShortResponseDto>> closestBookings = findClosestBookings(bookingsByItems);

        // То же самое проделываем с комментариями
        List<Comment> comments = commentRepository.findAll();
        Map<Long, List<CommentResponseDto>> commentsByItem = listCommentstoMap(comments);

        return ownersItems.stream()
                .map(item -> ItemMapper.toItemResponseDto(item,
                        closestBookings.get(item.getId()) == null ? null : closestBookings.get(item.getId()).get(0),
                        closestBookings.get(item.getId()) == null ? null : closestBookings.get(item.getId()).get(1),
                        commentsByItem.getOrDefault(item.getId(), null)))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemResponseDto> getItemsSearch(String text, Integer from, Integer size) {
        if (text.isEmpty()) {
            return new ArrayList<>();
        }

        Pageable pageable = PageRequest.of(from == 0 ? 0 : from / size, size);
        return repository.getItemsSearch(text, pageable).stream()
                .map(ItemMapper::toItemDto).collect(Collectors.toList());
    }

    @Override
    public CommentResponseDto createComment(CommentRequestDto commentDto, Long userId, Long itemId) {
        User user = userIdValidation(userId);
        Item item = repository.findById(itemId).orElseThrow(() -> new NotFoundException("Item id = " + itemId));

        List<Booking> bookings = bookingRepository.findAllByBookerIdAndItemIdAndStatusAndEndBefore(userId, itemId,
                Status.APPROVED, LocalDateTime.now());

        if (bookings.isEmpty()) {
            throw new BadRequestException("Вы не можете писать комментарий к вещи, которой не пользовались");
        }

        Comment comment = CommentMapper.toComment(commentDto, user, item);
        return CommentMapper.toCommentDto(commentRepository.save(comment));
    }

    // товарищ ревьюер, мне кажется это можно сделать куда элегантнее, подскажите пожалуйста
    private Map<Long, List<BookingShortResponseDto>> findClosestBookings(List<Booking> bookingsByItems) {
        final LocalDateTime now = LocalDateTime.now();
        final Map<Long, List<Booking>> bookingMap = listItemsBookingToMap(bookingsByItems);
        System.out.println(bookingMap);
        final Map<Long, List<BookingShortResponseDto>> closestBookingToItems = new HashMap<>();

        for (Long itemId : bookingMap.keySet()) {
            List<Booking> bookings = bookingMap.get(itemId);

            Booking closestPastBooking = null;
            Booking closestFutureBooking = null;

            for (Booking booking : bookings) {
                if (booking.getStatus().equals(Status.REJECTED)) {
                    continue;
                }

                LocalDateTime start = booking.getStart();
                LocalDateTime end = booking.getEnd();

                if (closestFutureBooking == null && start.isAfter(now)) {
                    closestFutureBooking = booking;
                    continue;
                }

                if (closestPastBooking == null && start.isBefore(now)) {
                    closestPastBooking = booking;
                    continue;
                }

                if (closestFutureBooking != null
                        && start.isBefore(closestFutureBooking.getStart())
                        && end.isAfter(now)) {
                    closestFutureBooking = booking;
                    continue;
                }

                if (closestPastBooking != null
                        && end.isAfter(closestPastBooking.getEnd())
                        && start.isBefore(now)) {
                    closestPastBooking = booking;
                }
            }

            BookingShortResponseDto pastBooking =
                    closestPastBooking == null ? null : BookingMapper.toShortBooking(closestPastBooking);

            BookingShortResponseDto futureBooking =
                    closestFutureBooking == null ? null : BookingMapper.toShortBooking(closestFutureBooking);

            List<BookingShortResponseDto> closestBookings = new ArrayList<>(2);
            closestBookings.add(pastBooking);
            closestBookings.add(futureBooking);
            closestBookingToItems.put(itemId, closestBookings);
        }

        return closestBookingToItems;
    }

    // Лямбда отказывается работать
    private Map<Long, List<Booking>> listItemsBookingToMap(List<Booking> bookingsByItems) {
        Map<Long, List<Booking>> bookingMap = new HashMap<>();

        for (Booking booking : bookingsByItems) {
            Long itemId = booking.getItem().getId();

            List<Booking> itemsBooking = bookingMap.getOrDefault(itemId, new ArrayList<>());
            itemsBooking.add(booking);

            bookingMap.put(itemId, itemsBooking);
        }

        return bookingMap;
    }

    private Map<Long, List<CommentResponseDto>> listCommentstoMap(List<Comment> comments) {
        Map<Long, List<CommentResponseDto>> commentsMap = new HashMap<>();

        for (Comment comment : comments) {
            CommentResponseDto commentDto = CommentMapper.toCommentDto(comment);
            Long itemId = comment.getItem().getId();

            List<CommentResponseDto> commentDtoByItem = commentsMap.getOrDefault(itemId, new ArrayList<>());
            commentDtoByItem.add(commentDto);

            commentsMap.put(itemId, commentDtoByItem);
        }

        return commentsMap;
    }

    private User userIdValidation(long userId) {
        if (userId == 0) {
            throw new BadRequestException("X-Sharer-User-Id отсутствует");
        }

        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId));
    }
}
