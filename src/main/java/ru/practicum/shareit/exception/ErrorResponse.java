package ru.practicum.shareit.exception;

import lombok.Getter;

@Getter
public class ErrorResponse {
    private final String massage;
    private final String error;

    public ErrorResponse(String massage, String error) {
        this.massage = massage;
        this.error = error;
    }
}
