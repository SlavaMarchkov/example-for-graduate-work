package ru.skypro.homework.controller;

import lombok.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.NewPasswordDto;
import ru.skypro.homework.dto.UpdateUserDto;
import ru.skypro.homework.dto.UserDto;
import ru.skypro.homework.service.UserService;
import ru.skypro.homework.service.impl.UserServiceImpl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Контроллер для обработки запросов для пользователей
 */
@RestController
@RequestMapping("/users")
@CrossOrigin(value = "http://localhost:3000")
public class UserController {

    private final UserService service;
    private final String avatarPath;

    public UserController(final UserService service,
                          @Value("${path.to.avatars.folder}") String avatarsDir,
                          @Value("${directory.separator}") String directorySeparator) {
        this.service = service;
        this.avatarPath = avatarsDir + directorySeparator;
    }

    /**
     * Установка нового пароля
     * <br>Используется метод сервиса {@link ru.skypro.homework.service.impl.UserServiceImpl#updatePassword}
     * @param newPassword      NewPasswordDto
     * @param authentication   Authentication
     * @return String
     */
    @PostMapping("/set_password") // POST http://localhost:8080/users/set_password
    public ResponseEntity<String> setPassword(@RequestBody NewPasswordDto newPassword,
                                              @NonNull Authentication authentication) {
        if (service.updatePassword(authentication.getName(),
                newPassword.getCurrentPassword(),
                newPassword.getNewPassword())) {
            return ResponseEntity.ok("Password was updated");
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Passwords do not match!");
        }
    }

    /**
     * Получение авторизованного пользователя
     * <br>Используется метод сервиса {@link UserServiceImpl#getAuthenticatedUser()}
     * @return UserDto
     */
    @GetMapping("/me") // GET http://localhost:8080/users/me
    public ResponseEntity<UserDto> getUser() {
        return ResponseEntity.ok(service.getAuthenticatedUser());
    }

    /**
     * Обновление данных пользователя
     * <br>Используется метод сервиса {@link ru.skypro.homework.service.impl.UserServiceImpl#updateUser}
     * @param updateUserDto UpdateUserDto
     * @return UpdateUserDto
     */
    @PatchMapping("/me") // PATCH http://localhost:8080/users/me
    public ResponseEntity<UpdateUserDto> updateUser(@RequestBody UpdateUserDto updateUserDto) {
        UpdateUserDto updatedUser = service.updateUser(updateUserDto);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Обновление аватара пользователя
     * <br>Используется метод сервиса {@link ru.skypro.homework.service.impl.UserServiceImpl#updateAvatar}
     * @param image MultipartFile
     * @return Resource
     * @throws IOException
     */
    @PatchMapping(
            path = "/me/image",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    public ResponseEntity<Resource> updateAvatar(@RequestParam MultipartFile image) throws IOException {
        String fileName = service.updateAvatar(image);
        return (fileName != null)
                ? ResponseEntity.ok().body(new ByteArrayResource(Files.readAllBytes(Paths.get(avatarPath + fileName))))
                : ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

}
