package ru.practicum.shareit.controllerTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.controller.ItemController;
import ru.practicum.shareit.item.dto.comment.CommentRequestDto;
import ru.practicum.shareit.item.dto.comment.CommentResponseDto;
import ru.practicum.shareit.item.dto.item.ItemRequestDto;
import ru.practicum.shareit.item.dto.item.ItemResponseDto;
import ru.practicum.shareit.item.service.ItemService;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ItemController.class)
public class ItemControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private ItemService itemService;

    private ItemRequestDto itemDto;
    private ItemResponseDto itemResponseDto;
    private CommentRequestDto commentDto;
    private static final String USER_ID = "X-Sharer-User-Id";

    @BeforeEach
    void beforeEach() {
        itemDto = ItemRequestDto.builder()
                .name("Item")
                .description("ItemDescription")
                .available(true)
                .requestId(1L)
                .build();

        itemResponseDto = ItemResponseDto.builder()
                .id(1L)
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable())
                .requestId(itemDto.getRequestId())
                .name(itemDto.getName())
                .build();
    }

    @Test
    @SneakyThrows
    void create_Status200AndReturnedItem_WhenAllOk() {
        Mockito.when(itemService.addItem(itemDto, 1L))
                .thenReturn(itemResponseDto);

        String result = mockMvc.perform(post("/items")
                        .header(USER_ID, 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Mockito.verify(itemService).addItem(Mockito.any(), Mockito.anyLong());

        assertEquals(objectMapper.writeValueAsString(itemResponseDto), result);
    }

    @Test
    @SneakyThrows
    void create_Status400_WhenWrongName() {
        itemDto.setName(null);

        Mockito.when(itemService.addItem(itemDto, 1L))
                .thenReturn(itemResponseDto);

        mockMvc.perform(post("/items")
                        .header(USER_ID, 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isBadRequest());

        Mockito.verify(itemService, Mockito.never()).addItem(Mockito.any(), Mockito.anyLong());
    }

    @Test
    @SneakyThrows
    void create_Status400_WhenWrongDescription() {
        itemDto.setDescription(null);

        Mockito.when(itemService.addItem(itemDto, 1L))
                .thenReturn(itemResponseDto);

        mockMvc.perform(post("/items")
                        .header(USER_ID, 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isBadRequest());

        Mockito.verify(itemService, Mockito.never()).addItem(Mockito.any(), Mockito.anyLong());
    }

    @Test
    @SneakyThrows
    void create_Status400_WhenAvailableNull() {
        itemDto.setAvailable(null);

        Mockito.when(itemService.addItem(itemDto, 1L))
                .thenReturn(itemResponseDto);

        mockMvc.perform(post("/items")
                        .header(USER_ID, 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isBadRequest());

        Mockito.verify(itemService, Mockito.never()).addItem(Mockito.any(), Mockito.anyLong());
    }

    @Test
    @SneakyThrows
    void getItemById_Status200() {
        Mockito.when(itemService.getItemById(1L, 1L)).thenReturn(itemResponseDto);

        mockMvc.perform(get("/items/{id}", 1L)
                        .header(USER_ID, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.description").value("ItemDescription"));

        Mockito.verify(itemService).getItemById(1L, 1L);
    }

    @Test
    @SneakyThrows
    void getItems_Status200() {
        Mockito.when(itemService.getItems(1L, 0, 10))
                .thenReturn(List.of(itemResponseDto));

        mockMvc.perform(get("/items")
                        .header(USER_ID, 1L))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper
                        .writeValueAsString(List.of(itemDto))));

        Mockito.verify(itemService).getItems(1L, 0, 10);
    }

    @Test
    @SneakyThrows
    void getItemByText_Status200() {
        Mockito.when(itemService.getItemsSearch("descr", 0, 10))
                .thenReturn(List.of(itemResponseDto));

        mockMvc.perform(get("/items/search")
                        .header(USER_ID, 1L)
                        .param("text", "descr"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper
                        .writeValueAsString(List.of(itemDto))));

        Mockito.verify(itemService).getItemsSearch("descr", 0, 10);
    }

    @Test
    @SneakyThrows
    void update_Status200() {
        ItemRequestDto itemDtoToUpdate = ItemRequestDto.builder()
                .name("UpdatedItem")
                .description("UpdatedItemDescription")
                .available(false)
                .build();

        ItemResponseDto itemResponseUpdate = ItemResponseDto.builder()
                .id(1L)
                .description(itemDtoToUpdate.getDescription())
                .available(itemDtoToUpdate.getAvailable())
                .requestId(itemDtoToUpdate.getRequestId())
                .name(itemDtoToUpdate.getName())
                .build();

        Mockito.when(itemService.updateItem(itemDtoToUpdate, 1L, 1L)).thenReturn(itemResponseUpdate);

        String result = mockMvc.perform(patch("/items/{id}", 1L)
                        .header(USER_ID, 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(itemDtoToUpdate)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Mockito.verify(itemService).updateItem(Mockito.any(), Mockito.anyLong(), Mockito.any());

        assertEquals(objectMapper.writeValueAsString(itemResponseUpdate), result);
    }

    @Test
    @SneakyThrows
    void createComment_Status200_WhenAllOk() {
        commentDto = CommentRequestDto.builder()
                .text("Comment")
                .build();

        CommentResponseDto commentResponseDto = CommentResponseDto.builder()
                .id(1L)
                .text(commentDto.getText())
                .created(LocalDateTime.now())
                .build();

        Mockito.when(itemService.createComment(commentDto, 1L, 1L)).thenReturn(commentResponseDto);

        String result = mockMvc.perform(post("/items/{itemId}/comment", 1L)
                        .header(USER_ID, 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Mockito.verify(itemService).createComment(commentDto, 1L, 1L);

        assertEquals(objectMapper.writeValueAsString(commentResponseDto), result);
    }

    @Test
    @SneakyThrows
    void createComment_Status400_WhenTextNull() {
        commentDto = CommentRequestDto.builder()
                .text(null)
                .build();

        mockMvc.perform(post("/items/{itemId}/comment", 1L)
                        .header(USER_ID, 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isBadRequest());

        Mockito.verify(itemService, Mockito.never()).createComment(commentDto, 1L, 1L);
    }

    @Test
    @SneakyThrows
    void addItem_Status404_WhenUserNotFound() {
        Mockito.when(itemService.addItem(itemDto, 100L)).thenThrow(new NotFoundException("user"));

        mockMvc.perform(post("/items")
                        .header(USER_ID, 100L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @SneakyThrows
    void updateItem_Status403_WhenUserIsNotItemCreator() {
        Mockito.when(itemService.updateItem(itemDto, 1L, 1L))
                .thenThrow(new AccessDeniedException("У вас нет допуска к этому действию"));

        mockMvc.perform(patch("/items/{id}", 1L)
                        .header(USER_ID, 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isForbidden());
    }
}