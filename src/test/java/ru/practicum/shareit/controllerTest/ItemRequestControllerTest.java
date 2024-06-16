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
import ru.practicum.shareit.request.controller.RequestController;
import ru.practicum.shareit.request.dto.RequestDto;
import ru.practicum.shareit.request.dto.RequestResponseDto;
import ru.practicum.shareit.request.service.RequestService;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = RequestController.class)
public class ItemRequestControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private RequestService itemRequestService;

    private RequestDto itemRequestDto;
    private RequestResponseDto requestResponseDto;
    private static final String USER_ID = "X-Sharer-User-Id";

    @BeforeEach
    void beforeEach() {
        itemRequestDto = RequestDto.builder()
                .description("ItemRequestDescription")
                .build();

        requestResponseDto = RequestResponseDto.builder()
                .id(1L)
                .description("ItemRequestDescription")
                .created(LocalDateTime.now())
                .items(List.of())
                .build();
    }

    @Test
    @SneakyThrows
    void create_Status200AndReturnedItemRequest_WhenAllOk() {
        Mockito.when(itemRequestService.createRequest(itemRequestDto, 1L))
                .thenReturn(requestResponseDto);

        String result = mockMvc.perform(post("/requests")
                        .header(USER_ID, 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(itemRequestDto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Mockito.verify(itemRequestService).createRequest(Mockito.any(), Mockito.anyLong());

        assertEquals(objectMapper.writeValueAsString(requestResponseDto), result);
    }

    @Test
    @SneakyThrows
    void create_Status400_WhenEmptyDescription() {
        itemRequestDto.setDescription(null);

        Mockito.when(itemRequestService.createRequest(itemRequestDto, 1L))
                .thenReturn(requestResponseDto);

        mockMvc.perform(post("/requests")
                        .header(USER_ID, 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(itemRequestDto)))
                .andExpect(status().isBadRequest());

        Mockito.verify(itemRequestService, Mockito.never()).createRequest(Mockito.any(), Mockito.anyLong());
    }

    @Test
    @SneakyThrows
    void getRequestById_Status200() {
        Mockito.when(itemRequestService.getRequest(1L, 1L)).thenReturn(requestResponseDto);

        mockMvc.perform(get("/requests/{id}", 1L)
                        .header(USER_ID, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.description").value("ItemRequestDescription"));

        Mockito.verify(itemRequestService).getRequest(1L, 1L);
    }

    @Test
    @SneakyThrows
    void getRequestsByPage_Status200() {
        Mockito.when(itemRequestService.getAll(1L, 0, 10))
                .thenReturn(List.of(requestResponseDto));

        mockMvc.perform(get("/requests/all")
                        .header(USER_ID, 1L))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper
                        .writeValueAsString(List.of(requestResponseDto))));

        Mockito.verify(itemRequestService).getAll(1L, 0, 10);
    }

    @Test
    @SneakyThrows
    void getRequests_Status200() {
        Mockito.when(itemRequestService.getAllByOwner(1L))
                .thenReturn(List.of(requestResponseDto));

        mockMvc.perform(get("/requests")
                        .header(USER_ID, 1L))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper
                        .writeValueAsString(List.of(requestResponseDto))));

        Mockito.verify(itemRequestService).getAllByOwner(1L);
    }
}