package ru.skypro.homework.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.skypro.homework.dto.AdDto;
import ru.skypro.homework.dto.CommentDto;
import ru.skypro.homework.dto.CommentsDto;
import ru.skypro.homework.dto.CreateOrUpdateCommentDto;
import ru.skypro.homework.service.AdService;
import ru.skypro.homework.service.CommentService;

@RestController
@RequestMapping(path = "/ads")
@CrossOrigin(value = "http://localhost:3000")
public class CommentController {
    private final CommentService commentService;
    private final AdService adService;

    public CommentController(final CommentService commentService,
                             final AdService adService) {
        this.commentService = commentService;
        this.adService = adService;
    }

    /**
     * Получение всех комментариев объявления для авторизованного пользователя
     *
     * @param adId Integer
     * @return CommentsDto
     */
    @GetMapping("/{id}/comments")
    public ResponseEntity<CommentsDto> getComments(@PathVariable(value = "id") Integer adId) {
        AdDto foundAd = adService.findAdById(adId);
        if (foundAd == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } else {
            CommentsDto comments = commentService.getComments(adId);
            return ResponseEntity.ok(comments);
        }
    }

    /**
     * Добавление комментария к объявлению
     *
     * @param adId    Integer
     * @param comment CreateOrUpdateCommentDto
     * @return CommentDto
     */
    @PostMapping("/{id}/comments")
    public ResponseEntity<CommentDto> addComment(@PathVariable(value = "id") Integer adId,
                                                 @RequestBody CreateOrUpdateCommentDto comment) {
        AdDto foundAd = adService.findAdById(adId);
        if (foundAd == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } else {
            CommentDto addedComment = commentService.addComment(adId, comment);
            return ResponseEntity.ok(addedComment);
        }
    }

    /**
     * Удаление комментария по ID объявления и ID комментария для авторизованного пользователя
     *
     * @param adId      Integer
     * @param commentId Integer
     * @return Void (статус 200 OK)
     */
    @DeleteMapping("/{adId}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable(value = "adId") Integer adId,
                                              @PathVariable(value = "commentId") Integer commentId) {
        AdDto foundAd = adService.findAdById(adId);
        CommentDto foundComment = commentService.findCommentByAdIdAndCommentId(adId, commentId);

        return (foundAd == null || foundComment == null)
                ? ResponseEntity.status(HttpStatus.NOT_FOUND).build()
                : commentService.deleteComment(adId, commentId)
                ? ResponseEntity.ok().build()
                : ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    /**
     * Обновление комментария
     *
     * @param adId      Integer
     * @param commentId Integer
     * @param comment   CreateOrUpdateCommentDto
     * @return CommentDto
     */
    @PatchMapping("/{adId}/comments/{commentId}")
    public ResponseEntity<CommentDto> updateComment(@PathVariable(value = "adId") Integer adId,
                                                    @PathVariable(value = "commentId") Integer commentId,
                                                    @RequestBody CreateOrUpdateCommentDto comment) {
        AdDto foundAd = adService.findAdById(adId);
        CommentDto foundComment = commentService.findCommentByAdIdAndCommentId(adId, commentId);
        if (foundAd == null || foundComment == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } else {
            CommentDto updatedComment = commentService.updateComment(adId, commentId, comment);
            return (updatedComment != null)
                    ? ResponseEntity.ok(updatedComment)
                    : ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

}
