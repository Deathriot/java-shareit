package ru.practicum.shareit.util;

import lombok.experimental.UtilityClass;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@UtilityClass
public class PageBuilder{
    public Pageable getPageable(int from, int size){
        return PageRequest.of(from / size, size);
    }

    public Pageable getPageable(int from, int size, Sort sort){
        return PageRequest.of(from / size, size, sort);
    }
}
