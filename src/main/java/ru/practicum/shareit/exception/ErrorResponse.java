package ru.practicum.shareit.exception;

public class ErrorResponse {
    private final static String WARNING = "ПРОИЗОШЛА ОШИБКА!!!";
    private final String massage;
    private final String error;

    public ErrorResponse(String massage, String error) {
        this.massage = massage;
        this.error = error;
    }
}
