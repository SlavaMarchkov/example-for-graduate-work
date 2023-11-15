package ru.skypro.homework.service.impl;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.skypro.homework.dto.CommentDto;
import ru.skypro.homework.dto.CommentsDto;
import ru.skypro.homework.dto.CreateOrUpdateCommentDto;
import ru.skypro.homework.dto.Role;
import ru.skypro.homework.entity.Ad;
import ru.skypro.homework.entity.Comment;
import ru.skypro.homework.entity.User;
import ru.skypro.homework.mapper.CommentMapper;
import ru.skypro.homework.repository.AdRepository;
import ru.skypro.homework.repository.CommentRepository;
import ru.skypro.homework.repository.UserRepository;
import ru.skypro.homework.service.CommentService;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final AdRepository adRepository;
    private final UserRepository userRepository;

    public CommentServiceImpl(CommentRepository commentRepository, CommentMapper commentMapper, AdRepository adRepository, UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.commentMapper = commentMapper;
        this.adRepository = adRepository;
        this.userRepository = userRepository;
    }

    /**
     * Приватный метод, который вытаскивает авторизованного пользователя
     *
     * @return возвращает пользователя
     */
    private User getCurrentUser() {
        Authentication authenticationUser = SecurityContextHolder.getContext().getAuthentication();
        UserDetails principalUser = (UserDetails) authenticationUser.getPrincipal();
        return userRepository.findByEmail(principalUser.getUsername());
    }

    /**
     * Метод, который выводит все комментарии к определенному объявлению
     *
     * @param adId (id объявления)
     * @return возвращает List комментариев
     */
    @Override
    public CommentsDto getComments(Integer adId) {
        return commentMapper.toCommentsDto(
                commentRepository
                        .findCommentsByAd_Pk(adId)
                        .size(),
                commentRepository
                        .findCommentsByAd_Pk(adId)
                        .stream()
                        .map(commentMapper::toDto)
                        .collect(Collectors.toList()));
    }

    /**
     * Метод, который добавляет комментарий к определенному объявлению
     *
     * @param adId        (id объявления)
     * @param commentText (текст комментария)
     * @return CommentDto (объект комментария)
     */
    @Override
    public CommentDto addComment(Integer adId, CreateOrUpdateCommentDto commentText) {
        Comment comment = new Comment();
        comment.setText(commentText.getText());
        comment.setCreatedAt(System.currentTimeMillis());
        comment.setAuthor(getCurrentUser());
        Optional<Ad> foundAd = adRepository.findById(adId);
        foundAd.ifPresent(comment::setAd);

        return commentMapper.toDto(commentRepository.save(comment));
    }

    /**
     * Метод, который удаляет комментарий
     *
     * @param adId      (id объявления)
     * @param commentId (id комментария)
     */
    @Override
    @Transactional
    public boolean deleteComment(Integer adId, Integer commentId) {
        CommentDto foundComment = findCommentByAdIdAndCommentId(adId, commentId);
        if (commentBelongsToCurrentUserOrIsAdmin(foundComment)) {
            commentRepository.deleteCommentByAd_PkAndPk(adId, commentId);
            return true;
        }
        return false;
    }

    /**
     * Метод, который изменяет текст комментария
     *
     * @param adId      (id объявления)
     * @param commentId (id комментария)
     * @param comment   (текст комментария)
     * @return CommentDto (объект комментария)
     */
    @Override
    public CommentDto updateComment(Integer adId,
                                    Integer commentId,
                                    CreateOrUpdateCommentDto comment) {
        CommentDto commentDto = findCommentByAdIdAndCommentId(adId, commentId);
        if (commentBelongsToCurrentUserOrIsAdmin(commentDto)) {
            return commentRepository
                    .findCommentByAd_PkAndPk(adId, commentId)
                    .map(oldComment -> {
                        oldComment.setText(comment.getText());
                        oldComment.setCreatedAt(System.currentTimeMillis());
                        return commentMapper.toDto(commentRepository.save(oldComment));
                    })
                    .orElse(null);
        }
        return null;
    }

    @Override
    public CommentDto findCommentByAdIdAndCommentId(final Integer adId, final Integer commentId) {
        return commentRepository
                .findCommentByAd_PkAndPk(adId, commentId)
                .map(commentMapper::toDto)
                .orElse(null);
    }

    private boolean commentBelongsToCurrentUserOrIsAdmin(CommentDto commentDto) {
        User user = getCurrentUser();
        boolean isAdmin = user.getRole().equals(Role.ADMIN);
        Integer userId = user.getId();
        return isAdmin || Objects.equals(userId, commentDto.getAuthor());
    }

}
