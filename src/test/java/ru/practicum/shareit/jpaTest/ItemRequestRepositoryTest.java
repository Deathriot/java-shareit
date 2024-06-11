package ru.practicum.shareit.jpaTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
public class ItemRequestRepositoryTest {

    @Autowired
    private RequestRepository itemRequestRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRepository itemRepository;
    private Item item;
    private Item item2;
    private User owner;
    private User requester;
    private ItemRequest itemRequest;

    @BeforeEach
    void beforeEach() {
        owner = User.builder()
                .name("User")
                .email("user@email.com")
                .build();

        item = Item.builder()
                .name("Item")
                .description("Item Description")
                .available(true)
                .owner(owner)
                .build();

        item2 = Item.builder()
                .name("Item2")
                .description("Item2 Descr")
                .available(true)
                .owner(owner)
                .build();

        requester = User.builder()
                .name("requester")
                .email("requestor@mail.ru")
                .build();

        itemRequest = ItemRequest.builder()
                .description("description")
                .created(LocalDateTime.now())
                .user(requester)
                .build();
    }

    @Test
    void shouldReturnAllRequestForRequester() {
        Pageable pageable = PageRequest.of(0, 10);
        userRepository.save(owner);
        userRepository.save(requester);
        itemRepository.save(item);
        itemRepository.save(item2);
        itemRequestRepository.save(itemRequest);
        List<ItemRequest> requests = itemRequestRepository.findAllByUserIdNot(1L, pageable);

        assertEquals(1, requests.size());
        assertEquals(requests, List.of(itemRequest));
        assertEquals(itemRequest.getDescription(), requests.get(0).getDescription());
    }
}
