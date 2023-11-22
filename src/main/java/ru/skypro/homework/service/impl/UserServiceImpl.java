package ru.skypro.homework.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.UpdateUserDto;
import ru.skypro.homework.dto.UserDto;
import ru.skypro.homework.entity.User;
import ru.skypro.homework.exception.UserAvatarProcessingException;
import ru.skypro.homework.mapper.UserMapper;
import ru.skypro.homework.repository.UserRepository;
import ru.skypro.homework.service.UserService;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.UUID;

/**
 * Сервис для работы с пользователями
 */
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository repository;
    private final UserMapper mapper;
    private final PasswordEncoder encoder;
    private final Path pathToAvatarsDir;

    public UserServiceImpl(final UserRepository repository,
                           final UserMapper mapper,
                           final PasswordEncoder encoder,
                           @Value("${path.to.avatars.folder}") String pathToAvatarsDir) {
        this.repository = repository;
        this.mapper = mapper;
        this.encoder = encoder;
        this.pathToAvatarsDir = Path.of(pathToAvatarsDir);
    }

    /**
     * Метод, который вытаскивает авторизованного пользователя
     * <br><br> Используется объект SecurityContextHolder.
     * <br> В нем мы храним информацию о текущем контексте безопасности приложения, который включает в себя подробную информацию о пользователе работающем в настоящее время с приложением.
     * @return UserDto – объект пользователя
     */
    @Override
    public UserDto getAuthenticatedUser() {
        Authentication currentUser = SecurityContextHolder.getContext().getAuthentication();
        UserDetails principalUser = (UserDetails) currentUser.getPrincipal();
        return mapper.toDto(
                repository.findByEmail(principalUser.getUsername())
        );
    }

    /**
     * Метод, который обновляет пароль от кабинета пользователя.
     * <br> Используются методы сервиса {@link UserServiceImpl#checkCurrentPassword}, {@link UserServiceImpl#setNewPassword}
     * @param email           почта
     * @param currentPassword пароль, который использовался ранее
     * @param newPassword     новый пароль
     */
    @Override
    public boolean updatePassword(final String email, final String currentPassword, final String newPassword) {
        if (checkCurrentPassword(email, currentPassword)) {
            setNewPassword(email, newPassword);
            return true;
        }
        return false;
    }

    /**
     * Метод, который возвращает аватар
     * @param path название файла изображения
     * @return массив байтов
     * @throws IOException
     */
    @Override
    public byte[] getAvatar(final String path) throws IOException {
        return new ByteArrayResource(Files
                .readAllBytes(Paths.get(path))
        ).getByteArray();
    }

    /**
     * Приватный метод, который обновляет пароль пользователя в базе данных
     * <br> Используется класс PasswordEncoder, нужен для выполнения одностороннего преобразования пароля, обеспечивающего безопасное хранение пароля
     * @param email    почта
     * @param password новый пароль
     */
    private void setNewPassword(final String email, final String password) {
        String encodedPassword = encoder.encode(password);
        User user = repository.findByEmail(email);
        user.setPassword(encodedPassword);
        repository.save(user);
    }

    /**
     * Приватный метод, который используется для проверки корректности введенного пароля
     * @param email    почта
     * @param password пароль
     */
    private boolean checkCurrentPassword(final String email, final String password) {
        User user = repository.findByEmail(email);
        return encoder.matches(password, user.getPassword());
    }

    /**
     * Метод, который обновляет данные пользователя.
     * <br>Используется метод {@link UserServiceImpl#getAuthenticatedUser()}
     * @param updatedUser
     * @return UpdateUserDto – обновленный объект пользователя
     */
    @Override
    public UpdateUserDto updateUser(final UpdateUserDto updatedUser) {
        UserDto userDto = this.getAuthenticatedUser();
        repository
                .findById(userDto.getId())
                .map(oldUser -> {
                    oldUser.setFirstName(updatedUser.getFirstName());
                    oldUser.setLastName(updatedUser.getLastName());
                    oldUser.setPhone(updatedUser.getPhone());
                    return mapper.toDto(repository.save(oldUser));
                });
        return updatedUser;
    }

    /**
     * Метод, который обновляет аватар пользователя
     * <br> Используются методы {@link UserServiceImpl#getExtensions}, {@link UserServiceImpl#writeToFile}
     * @param file изображение для загрузки
     * @return String – название файла изображения
     */
    @Override
    public String updateAvatar(final MultipartFile file) {
        UserDto userDto = this.getAuthenticatedUser();
        try {
            String extension = getExtensions(Objects.requireNonNull(file.getOriginalFilename()));
            byte[] data = file.getBytes();
            String fileName = UUID.randomUUID() + "." + extension;
            Path pathToAvatar = pathToAvatarsDir.resolve(fileName);
            writeToFile(pathToAvatar, data);

            String avatar = userDto.getImage();
            if (avatar != null) {
                Files.delete(Path.of(avatar.substring(1)));
            }

            repository
                    .findById(userDto.getId())
                    .map(user -> {
                        user.setImage(fileName);
                        return mapper.toDto(repository.save(user));
                    });

            return fileName;
        } catch (IOException e) {
            throw new UserAvatarProcessingException();
        }
    }

    /**
     * Приватный метод, который записывает переданный файл в папку на диске
     */
    private void writeToFile(Path path, byte[] data) {
        try (FileOutputStream fos = new FileOutputStream(path.toFile())) {
            fos.write(data);
        } catch (IOException e) {
            throw new UserAvatarProcessingException();
        }
    }

    /**
     * Приватный метод, который получает расширение загруженного файла
     */
    private String getExtensions(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

}
