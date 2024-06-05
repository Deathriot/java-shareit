package ru.practicum.shareit.exception;

public class UnSupportedStateException extends RuntimeException {
    public UnSupportedStateException(String message) {
        super(message);
    }
}
