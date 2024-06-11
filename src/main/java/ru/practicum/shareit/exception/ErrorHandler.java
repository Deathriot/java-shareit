package ru.practicum.shareit.exception;

import lombok.extern.slf4j.Slf4j;
import org.h2.jdbc.JdbcSQLIntegrityConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ValidationException;
import java.util.Map;

@RestControllerAdvice("ru.practicum.shareit")
@Slf4j
public class ErrorHandler {

    @ExceptionHandler({BadRequestException.class,
            MissingRequestHeaderException.class, MethodArgumentNotValidException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse badRequestHandler(final Exception e) {
        final String message = "Неверный запрос ";
        log.error(message + e.getMessage());
        return new ErrorResponse(message, e.getMessage());
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse validationHandler(final Exception e) {
        final String message = "Не пройдена валидация объекта ";
        log.error(message + e.getMessage());
        return new ErrorResponse(message, e.getMessage());
    }

    @ExceptionHandler({JdbcSQLIntegrityConstraintViolationException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse conflictHandler(final Exception e) {
        final String message = "Такой элемент уже существует ";
        log.error(message + e.getMessage());
        return new ErrorResponse(message, e.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse forbiddenHandler(final Exception e) {
        final String message = "У вас не допуска к этому действию ";
        log.error(message + e.getMessage());
        return new ErrorResponse(message, e.getMessage());
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse notFoundHandler(final Exception e) {
        final String message = "Данный элемент не найден: ";
        log.error(message + e.getMessage());
        return new ErrorResponse(message, e.getMessage());
    }


    // все ради того чтоб один тест был доволен...
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> unsupportedStateHandler(final Exception e) {
        return Map.of("error", e.getMessage());
    }
}
