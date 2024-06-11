package ru.practicum.shareit.jpaTest;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.RequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
public class ItemRepositoryTest {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private RequestRepository requestRepository;

    private Item item;
    private Pageable pageable;
    private ItemRequest request;
    private User owner;
    private User owner2;

    @BeforeEach
    void beforeEach() {
        owner = User.builder()
                .name("User")
                .email("user@email.com")
                .build();

        owner2 = User.builder()
                .name("User")
                .email("user2@email.com")
                .build();

        owner = userRepository.save(owner);
        owner2 = userRepository.save(owner2);

        request = ItemRequest.builder()
                .user(owner)
                .created(LocalDateTime.now())
                .description("testRequest")
                .build();

        requestRepository.save(request);

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
                .request(request)
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

    @AfterEach
    void afterEach() {
        requestRepository.deleteAll();
        itemRepository.deleteAll();
        userRepository.deleteAll();
        userRepository.flush();
    }

    @Test
    void shouldSearchByText() {
        List<Item> items = itemRepository.getItemsSearch("description", pageable);

        assertEquals(1, items.size());
        assertEquals(item.getName(), items.get(0).getName());
        assertEquals(item.getDescription(), items.get(0).getDescription());
    }

    @Test
    void findAllByOwnerId() {
        List<Item> itemsOne = itemRepository.findAllByOwnerId(owner2.getId(), pageable);

        assertEquals(1, itemsOne.size());
        assertEquals("Item3", itemsOne.get(0).getName());

        List<Item> itemsTwo = itemRepository.findAllByOwnerId(owner.getId(), pageable);
        assertEquals(2, itemsTwo.size());
    }

    @Test
    void findAllByRequestIn() {
        List<Item> items = itemRepository.findAllByRequestIn(List.of(request));

        assertEquals(1, items.size());
        assertEquals("Item2", items.get(0).getName());
    }
}
