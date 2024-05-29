package ru.practicum.shareit.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ValidationException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice("ru.practicum.shareit")
@Slf4j
public class ErrorHandler {

    @ExceptionHandler({BadRequestException.class, MissingRequestHeaderException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse badRequestHandler(final BadRequestException e) {
        log.error(e.getMessage());
        return new ErrorResponse("Неверный запрос", e.getMessage());
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse validationHandler(final ValidationException e) {
        log.error(e.getMessage());
        return new ErrorResponse("Не пройдена валидация объекта", e.getMessage());
    }

    @ExceptionHandler(AlreadyExistException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse conflictHandler(final AlreadyExistException e) {
        log.error(e.getMessage());
        return new ErrorResponse("Такой элемент уже существует", e.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse forbiddenHandler(final AccessDeniedException e) {
        log.error(e.getMessage());
        return new ErrorResponse("У вас не допуска к этому действию", e.getMessage());
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse notFoundHandler(final NotFoundException e) {
        log.error(e.getMessage());
        return new ErrorResponse("Данный элемент не найден: ", e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleMethodArgumentNotValidException(final MethodArgumentNotValidException e) {
        log.error(e.getMessage());
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach((error) -> {
            String name = ((FieldError) error).getField();
            String msg = error.getDefaultMessage();
            errors.put(name, msg);
        });
        return errors;
    }
}
