package ru.skypro.homework.service;

import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.UpdateUserDto;
import ru.skypro.homework.dto.UserDto;

import java.io.IOException;

/**
 * Интерфейс для работы с пользователями
 */
public interface UserService {
    UserDto getAuthenticatedUser();

    UpdateUserDto updateUser(UpdateUserDto updatedUser);

    String updateAvatar(MultipartFile file);

    boolean updatePassword(String email, String currentPassword, String newPassword);

    byte[] getAvatar(String fileName) throws IOException;
}
