package ru.skypro.homework.service;

import ru.skypro.homework.dto.CommentDto;
import ru.skypro.homework.dto.CommentsDto;
import ru.skypro.homework.dto.CreateOrUpdateCommentDto;

/**
 * Интерфейс для работы с комментариями
 */
public interface CommentService {

    CommentsDto getComments(Integer adId);

    CommentDto addComment(Integer adId, CreateOrUpdateCommentDto commentText);

    boolean deleteComment(Integer adId, Integer commentId);

    CommentDto updateComment(Integer adId, Integer commentId, CreateOrUpdateCommentDto comment);

    CommentDto findCommentByAdIdAndCommentId(Integer adId, Integer commentId);
}
