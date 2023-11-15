package ru.skypro.homework.mapper;

import lombok.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.skypro.homework.dto.CommentDto;
import ru.skypro.homework.dto.CommentsDto;
import ru.skypro.homework.entity.Comment;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

@Component
public class CommentMapper {

    private final String fullAvatarPath;

    public CommentMapper(@Value("${path.to.avatars.folder}") String pathToAvatarsDir,
                      @Value("${directory.separator}") String directorySeparator) {
        Path pathToAvatars = Path.of(pathToAvatarsDir);
        this.fullAvatarPath = directorySeparator + pathToAvatars + directorySeparator;
    }

    public CommentDto toDto(@NonNull Comment comment) {
        CommentDto commentDto = new CommentDto();

        commentDto.setPk(comment.getPk());
        commentDto.setText(comment.getText());
        commentDto.setAuthor(comment.getAuthor().getId());
        commentDto.setCreatedAt(comment.getCreatedAt());
        commentDto.setAuthorFirstName(comment.getAuthor().getFirstName());

        Optional.ofNullable(comment.getAuthor().getImage())
                .ifPresent(elem -> commentDto.setAuthorImage(fullAvatarPath + comment.getAuthor().getImage()));

        return commentDto;
    }

    public CommentsDto toCommentsDto(Integer count, @NonNull List<CommentDto> results) {
        CommentsDto commentsDto = new CommentsDto();

        commentsDto.setCount(count);
        commentsDto.setResults(results);

        return commentsDto;
    }

    public Comment toEntity(CommentDto commentDto) {
        Comment comment = new Comment();

        comment.setText(commentDto.getText());

        return comment;
    }

}
