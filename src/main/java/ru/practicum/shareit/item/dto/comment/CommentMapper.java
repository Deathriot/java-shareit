package ru.practicum.shareit.item.dto.comment;

import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

public final class CommentMapper {
    private CommentMapper() {

    }

    public static Comment toComment(CommentRequestDto commentDto, User user, Item item) {
        return Comment.builder()
                .text(commentDto.getText())
                .user(user)
                .item(item)
                .created(LocalDateTime.now())
                .build();
    }

    public static CommentResponseDto toCommentDto(Comment comment) {
        return CommentResponseDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorName(comment.getUser().getName())
                .created(comment.getCreated())
                .build();
    }
}
