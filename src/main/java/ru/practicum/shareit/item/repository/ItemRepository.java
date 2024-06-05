package ru.practicum.shareit.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    List<Item> findAllByOwnerId(Long ownerId);

    @Query("SELECT it " +
            "FROM Item AS it " +
            "WHERE (LOWER(it.name) LIKE LOWER(CONCAT('%', ?1, '%'))" +
            "OR LOWER(it.description) LIKE LOWER(CONCAT('%', ?1, '%')))" +
            "AND it.available = TRUE")
    List<Item> getItemsSearch(String text);
}
