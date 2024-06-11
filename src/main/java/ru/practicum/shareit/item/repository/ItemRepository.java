package ru.practicum.shareit.item.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    List<Item> findAllByOwnerId(Long ownerId, Pageable pageable);

    @Query("SELECT it " +
            "FROM Item AS it " +
            "WHERE (LOWER(it.name) LIKE LOWER(CONCAT('%', ?1, '%'))" +
            "OR LOWER(it.description) LIKE LOWER(CONCAT('%', ?1, '%')))" +
            "AND it.available = TRUE")
    List<Item> getItemsSearch(String text, Pageable pageable);

    List<Item> findAllByRequestId(Long requestId);

    List<Item> findAllByRequestIn(List<ItemRequest> requests);
}
