package ru.practicum.shareit.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.item.model.Item;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findAllByItemId(Long itemId);

    List<Booking> findAllByItemIn(List<Item> items);

    List<Booking> findAllByBookerIdOrderByStartDesc(Long bookerId);

    List<Booking> findAllByItemOwnerIdOrderByStartDesc(Long itemOwnerId);

    // Получаем список букингов, которые были у пользователя, с условием, что он пользовался этой вещью
    List<Booking> findAllByBookerIdAndItemIdAndStatusAndEndBefore(Long bookerId, Long itemId,
                                                                  Status status, LocalDateTime time);
}
