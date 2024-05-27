package ru.practicum.shareit.exception;

import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ValidationException;

@RestControllerAdvice("ru.practicum.shareit")
@Primary
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse BadRequestHandler(final BadRequestException e){
        return new ErrorResponse("Неверный запрос", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse BadRequestHandler(final ValidationException e){
        return new ErrorResponse("Не пройдена валидация объекта", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse ConflictHandler(final AlreadyExistException e){
        return new ErrorResponse("Такой элемент уже существует", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse ForbiddenHandler(final AccessDeniedException e){
        return new ErrorResponse("У вас не допуска к этому действию", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse NotFoundHandler(final NotFoundException e){
        return new ErrorResponse("Данный элемент не найден: ", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse AllOtherStuffHandler(final Throwable e){
        return new ErrorResponse("Что-то пошло не так...", e.getMessage());
    }
}
