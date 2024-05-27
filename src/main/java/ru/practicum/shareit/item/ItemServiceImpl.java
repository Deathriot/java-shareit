package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository repository;
    private final UserRepository userRepository;

    @Override
    public ItemDto addItem(ItemDto itemDto, long userId) {
        userIdValidation(userId);

        Item item = ItemMapper.toItem(itemDto, userId);
        return ItemMapper.toItemDto(repository.addItem(item));
    }

    @Override
    public ItemDto updateItem(ItemDto itemDto, long userId, long itemId) {
        userIdValidation(userId);
        isItemExist(itemId);

        Item item = repository.getItemById(itemId);

        if (item.getOwner() != userId) {
            throw new AccessDeniedException("Редактирование Пользователем id = " + userId + ", предмета id = " + itemId);
        }

        return ItemMapper.toItemDto(repository.updateItem(item));
    }

    @Override
    public ItemDto getItemById(long itemId) {
        isItemExist(itemId);

        return ItemMapper.toItemDto(repository.getItemById(itemId));
    }

    @Override
    public List<ItemDto> getItems(long userId) {
        userIdValidation(userId);

       return repository.getItems(userId).stream().map((ItemMapper::toItemDto)).collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> getItemsSearch(String text) {
        return repository.getItemsSearch(text).stream().map((ItemMapper::toItemDto)).collect(Collectors.toList());
    }

    private void userIdValidation(long userId) {
        if (userId == 0) {
            throw new BadRequestException("X-Sharer-User-Id отсутствует");
        }

        if (userRepository.getUserById(userId) == null) {
            throw new NotFoundException("Пользователь с id = " + userId);
        }
    }

    private void isItemExist(long itemId){
        Item item = repository.getItemById(itemId);

        if(item == null){
            throw new NotFoundException("Item");
        }
    }
}
