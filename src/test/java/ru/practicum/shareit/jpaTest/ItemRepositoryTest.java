package ru.practicum.shareit.jpaTest;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
public class ItemRepositoryTest {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRepository itemRepository;

    private Item item;
    private Pageable pageable;

    @BeforeEach
    void beforeEach() {
        User owner = User.builder()
                .name("User")
                .email("user@email.com")
                .build();

        User owner2 = User.builder()
                .name("User")
                .email("user2@email.com")
                .build();

        userRepository.save(owner);
        userRepository.save(owner2);

        item = Item.builder()
                .name("Item")
                .description("Item Description")
                .available(true)
                .owner(owner)
                .build();

        Item item2 = Item.builder()
                .name("Item2")
                .description("Item2 Descr")
                .available(true)
                .owner(owner)
                .build();

        Item item3 = Item.builder()
                .name("Item3")
                .description("Item3 Descr")
                .available(true)
                .owner(owner2)
                .build();

        itemRepository.save(item);
        itemRepository.save(item2);
        itemRepository.save(item3);

        pageable = PageRequest.of(0, 10);
    }

    @Test
    void shouldSearchByText() {
        List<Item> items = itemRepository.getItemsSearch("description", pageable);

        assertEquals(1, items.size());
        assertEquals(item.getName(), items.get(0).getName());
        assertEquals(item.getDescription(), items.get(0).getDescription());
    }
}
