package ru.skypro.homework.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.skypro.homework.service.UserService;

import java.io.IOException;

/**
 * Контроллер для обработки запросов для аватарок пользователей
 */
@RestController
@RequestMapping(path = AvatarController.BASE_PATH)
@CrossOrigin(value = "http://localhost:3000")
public class AvatarController {

    public static final String BASE_PATH = "avatars/";
    private final UserService userService;

    public AvatarController(final UserService userService) {
        this.userService = userService;
    }

    @GetMapping(value = "/{path}", produces = {
            MediaType.IMAGE_PNG_VALUE,
            MediaType.IMAGE_JPEG_VALUE,
            MediaType.IMAGE_GIF_VALUE,
            "image/*"
    })
    public ResponseEntity<byte[]> getAvatar(@PathVariable final String path) throws IOException {
        byte[] avatar = userService.getAvatar(BASE_PATH + path);
        return ResponseEntity.ok().body(avatar);
    }

}
