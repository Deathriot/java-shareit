package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ItemRepositoryImpl implements ItemRepository {
    private long nextId = 1;
    private final Map<Long, Item> items;
    private final Map<Long, List<Item>> userItems;

    @Override
    public Item addItem(Item item) {
        item.setId(nextId);
        nextId++;

        items.put(item.getId(), item);
        List<Item> allUserItems = userItems.getOrDefault(item.getOwner(), new ArrayList<>());
        allUserItems.add(item);
        userItems.put(item.getOwner(), allUserItems);

        return item;
    }

    @Override
    public Item updateItem(Item item) {
        Item updatedItem = items.get(item.getId());

        if (item.getName() != null) {
            updatedItem.setName(item.getName());
        }

        if (item.getDescription() != null) {
            updatedItem.setDescription(item.getDescription());
        }

        if (item.getAvailable() != null) {
            updatedItem.setAvailable(item.getAvailable());
        }

        return updatedItem;
    }

    @Override
    public Item getItemById(long itemId) {
        return items.get(itemId);
    }

    @Override
    public List<Item> getItems(long userId) {
        return userItems.get(userId);
    }

    @Override
    public List<Item> getItemsSearch(String text) {
        if (text.isEmpty()) {
            return new ArrayList<>();
        }

        String lowerText = text.toLowerCase();

        return items.values().stream()
                .filter(Item::getAvailable)
                .filter(item -> item.getName().toLowerCase().contains(lowerText)
                        || item.getDescription().toLowerCase().contains(lowerText))
                .collect(Collectors.toList());
    }
}
