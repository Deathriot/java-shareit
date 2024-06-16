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
import ru.practicum.shareit.booking.controller.BookingController;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.service.BookingService;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BookingController.class)
public class BookingControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private BookingService bookingService;

    private BookingRequestDto bookingRequestDto;
    private BookingResponseDto bookingResponseDto;
    private static final String USER_ID = "X-Sharer-User-Id";

    @BeforeEach
    void beforeEach() {
        bookingRequestDto = BookingRequestDto.builder()
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusHours(2))
                .itemId(1L)
                .build();

        bookingResponseDto = BookingResponseDto.builder()
                .id(1L)
                .start(bookingRequestDto.getStart())
                .end(bookingRequestDto.getEnd())
                .build();
    }

    @Test
    @SneakyThrows
    void create_Status200_WhenAllOk() {
        Mockito.when(bookingService.createBooking(Mockito.any(), Mockito.anyLong())).thenReturn(bookingResponseDto);

        String result = mockMvc.perform(post("/bookings")
                        .header(USER_ID, 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(bookingRequestDto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Mockito.verify(bookingService).createBooking(Mockito.any(), Mockito.anyLong());

        assertEquals(objectMapper.writeValueAsString(bookingResponseDto), result);
    }

    @Test
    @SneakyThrows
    void create_Status400_whenEndNull() {
        bookingRequestDto.setEnd(null);

        mockMvc.perform(post("/bookings")
                        .header(USER_ID, 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(bookingRequestDto)))
                .andExpect(status().isBadRequest());

        Mockito.verify(bookingService, Mockito.never()).createBooking(Mockito.any(), Mockito.anyLong());
    }

    @Test
    @SneakyThrows
    void create_Status400_whenStartNull() {
        bookingRequestDto.setStart(null);

        mockMvc.perform(post("/bookings")
                        .header(USER_ID, 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(bookingRequestDto)))
                .andExpect(status().isBadRequest());

        Mockito.verify(bookingService, Mockito.never()).createBooking(Mockito.any(), Mockito.anyLong());
    }

    @Test
    @SneakyThrows
    void create_Status400_whenEndBeforeStart() {
        bookingRequestDto.setEnd(bookingRequestDto.getStart().minusHours(10));

        mockMvc.perform(post("/bookings")
                        .header(USER_ID, 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(bookingRequestDto)))
                .andExpect(status().isBadRequest());

        Mockito.verify(bookingService, Mockito.never()).createBooking(Mockito.any(), Mockito.anyLong());
    }

    @Test
    @SneakyThrows
    void create_Status400_whenStartInPast() {
        bookingRequestDto.setStart(LocalDateTime.now().minusHours(15));

        mockMvc.perform(post("/bookings")
                        .header(USER_ID, 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(bookingRequestDto)))
                .andExpect(status().isBadRequest());

        Mockito.verify(bookingService, Mockito.never()).createBooking(Mockito.any(), Mockito.anyLong());
    }

    @Test
    @SneakyThrows
    void updateStatus_Status200() {
        Mockito.when(bookingService.approveBooking(Mockito.anyLong(), Mockito.anyLong(), Mockito.anyBoolean()))
                .thenReturn(bookingResponseDto);

        String result = mockMvc.perform(patch("/bookings/{id}", 1L)
                        .header(USER_ID, 1L)
                        .param("approved", "true")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(bookingRequestDto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Mockito.verify(bookingService).approveBooking(Mockito.anyLong(), Mockito.anyLong(), Mockito.anyBoolean());

        assertEquals(objectMapper.writeValueAsString(bookingResponseDto), result);
    }

    @Test
    @SneakyThrows
    void getBooking_Status200() {
        Mockito.when(bookingService.getBooking(Mockito.anyLong(), Mockito.anyLong())).thenReturn(bookingResponseDto);

        mockMvc.perform(get("/bookings/{id}", 1L)
                        .header(USER_ID, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"));

        Mockito.verify(bookingService).getBooking(Mockito.anyLong(), Mockito.anyLong());
    }

    @Test
    @SneakyThrows
    void getAllBookerBookings_Status200() {
        Mockito.when(bookingService
                        .getBookingByBooker(Mockito.anyLong(), Mockito.any(), Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(List.of(bookingResponseDto));

        mockMvc.perform(get("/bookings")
                        .header(USER_ID, 1L))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(List.of(bookingResponseDto))));

        Mockito.verify(bookingService)
                .getBookingByBooker(Mockito.anyLong(), Mockito.any(), Mockito.anyInt(), Mockito.anyInt());
    }

    @Test
    @SneakyThrows
    void getAllOwnerItemBookings_Status200() {
        Mockito.when(bookingService
                        .getBookingItemsByOwner(Mockito.anyLong(), Mockito.any(), Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(List.of(bookingResponseDto));

        mockMvc.perform(get("/bookings/owner")
                        .header(USER_ID, 1L))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(List.of(bookingResponseDto))));

        Mockito.verify(bookingService)
                .getBookingItemsByOwner(Mockito.anyLong(), Mockito.any(), Mockito.anyInt(), Mockito.anyInt());
    }
}
