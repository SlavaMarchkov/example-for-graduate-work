package ru.skypro.homework.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;
import ru.skypro.homework.dto.*;
import ru.skypro.homework.entity.Ad;
import ru.skypro.homework.entity.Comment;
import ru.skypro.homework.entity.User;
import ru.skypro.homework.exception.AdImageProcessingException;
import ru.skypro.homework.mapper.AdMapper;
import ru.skypro.homework.repository.AdRepository;
import ru.skypro.homework.repository.CommentRepository;
import ru.skypro.homework.repository.UserRepository;
import ru.skypro.homework.service.AdService;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Сервис для работы с объявлениями
 */
@Service
public class AdServiceImpl implements AdService {

    private final AdRepository adRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final AdMapper mapper;
    private final String pathToImagesDir;

    public AdServiceImpl(final AdRepository adRepository,
                         final UserRepository userRepository,
                         final CommentRepository commentRepository,
                         final AdMapper mapper,
                         @Value("${path.to.images.folder}") String pathToImagesDir) {
        this.adRepository = adRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
        this.mapper = mapper;
        this.pathToImagesDir = UriComponentsBuilder.newInstance()
                .path(pathToImagesDir + "/")
                .build()
                .toUriString();
    }

    /**
     * Метод, который вытаскивает авторизованного пользователя.
     * <br><br> Используется объект SecurityContextHolder.
     * <br> В нем мы храним информацию о текущем контексте безопасности приложения, который включает в себя подробную информацию о пользователе работающем в настоящее время с приложением.
     * @return возвращает пользователя
     */
    @Override
    public User getCurrentUser() {
        Authentication authenticationUser = SecurityContextHolder.getContext().getAuthentication();
        UserDetails principalUser = (UserDetails) authenticationUser.getPrincipal();
        return userRepository.findByEmail(principalUser.getUsername());
    }

    /**
     * Метод, который создает новое объявление.
     * <br><br> Используется метод сервиса {@link AdServiceImpl#updateImage}
     * @param ad     Объект пользователя
     * @param file   фотография прикрепляемая к объявлению
     * @return AdDto – объект объявления
     */
    @Override
    public AdDto create(CreateOrUpdateAdDto ad, MultipartFile file) {
        Ad entity = new Ad();
        entity.setAuthor(getCurrentUser());
        entity.setPrice(ad.getPrice());
        entity.setTitle(ad.getTitle());
        entity.setDescription(ad.getDescription());

        Ad addedAd = adRepository.save(entity);

        String fileName = updateImage(addedAd.getPk(), file);
        entity.setImage(fileName);

        return mapper.toDto(entity);
    }

    /**
     * Метод, который выводит объявление по индефикатору
     * @param id             идентификатор объявления
     * @return ExtendedAdDto – расширенный объект объявления
     */
    @Override
    public ExtendedAdDto get(Integer id) {
        return adRepository
                .findById(id)
                .map(mapper::toExtendedDto)
                .orElse(null);
    }

    /**
     * Метод, который выводит все объявления
     * @return возвращает List объявлений
     */
    @Override
    public AdsDto getAll() {
        return mapper.toAdsDto(
                adRepository
                        .findAll()
                        .size(),
                adRepository
                        .findAll()
                        .stream()
                        .map(mapper::toDto)
                        .collect(Collectors.toList()));
    }

    /**
     * Метод, который выводит объявления авторизованного пользователя.
     * <br><br> Используется метод сервиса {@link AdServiceImpl#getCurrentUser()}
     * @return возвращает List объявлений
     */
    @Override
    public AdsDto getAuthorizedUserAds() {
        User user = this.getCurrentUser();
        Integer id = user.getId();
        return mapper.toAdsDto(
                adRepository
                        .findByAuthorId(id)
                        .size(),
                adRepository
                        .findByAuthorId(id)
                        .stream()
                        .map(mapper::toDto)
                        .collect(Collectors.toList()));
    }

    /**
     * Метод, который обновляет данные объявления в базе данных.
     * <br><br> Используются методы {@link AdServiceImpl#findAdById}, {@link AdServiceImpl#adBelongsToCurrentUserOrIsAdmin}
     * @param id идентификатор объявления
     * @param ad объект пользователя
     * @return AdDto – объект объявления
     */
    @Override
    public AdDto update(Integer id, CreateOrUpdateAdDto ad) {
        AdDto adDto = findAdById(id);
        if (adBelongsToCurrentUserOrIsAdmin(adDto)) {
            return adRepository
                    .findById(id)
                    .map(oldAd -> {
                        oldAd.setPrice(ad.getPrice());
                        oldAd.setTitle(ad.getTitle());
                        oldAd.setDescription(ad.getDescription());
                        return mapper.toDto(adRepository.save(oldAd));
                    })
                    .orElse(null);
        }
        return null;
    }

    /**
     * Метод, который удаляет объявление
     * <br><br> Используются методы {@link AdServiceImpl#adBelongsToCurrentUserOrIsAdmin}, {@link CommentRepository#findCommentsByAd_Pk}
     * @param adDto – объект объявления
     */
    @Override
    public boolean delete(AdDto adDto) {
        if (adBelongsToCurrentUserOrIsAdmin(adDto)) {
            List<Comment> comments = commentRepository.findCommentsByAd_Pk(adDto.getPk());
            comments.forEach(comment -> {
                commentRepository.deleteById(comment.getPk());
            });

            String image = adDto.getImage();
            if (image != null) {
                try {
                    Path path = Path.of(image.substring(1));
                    Files.delete(path);
                } catch (IOException e) {
                    throw new AdImageProcessingException();
                }
            }

            adRepository.deleteById(adDto.getPk());
            return true;
        }
        return false;
    }

    /**
     * Метод, который находит объявление по идентификатору
     * @param id идентификатор объявления
     * @return AdDto – объект объявления
     */
    @Override
    public AdDto findAdById(Integer id) {
        return adRepository
                .findById(id)
                .map(mapper::toDto)
                .orElse(null);
    }

    /**
     * Метод, который выводит фотографии
     * @param image название файла изображения
     * @return массив байтов
     * @throws IOException
     */
    @Override
    public byte[] getImage(final String fileName) throws IOException {
        Path path = Path.of(pathToImagesDir, fileName);
        return new ByteArrayResource(Files
                .readAllBytes(path)
        ).getByteArray();
    }

    /**
     * Метод, который обновляет фотографии по идентификатору объявления.
     * <br>Используются методы {@link AdServiceImpl#getExtensions}, {@link AdServiceImpl#writeToFile}, {@link AdServiceImpl#adBelongsToCurrentUserOrIsAdmin}
     * @param id   идентификатор объявления
     * @param file изображение для загрузки
     * @return String – название файла изображения
     */
    @Override
    public String updateImage(final Integer id, final MultipartFile file) {
        AdDto adDto = findAdById(id);
        if (adBelongsToCurrentUserOrIsAdmin(adDto)) {
            try {
                String extension = getExtensions(Objects.requireNonNull(file.getOriginalFilename()));
                byte[] data = file.getBytes();
                String fileName = UUID.randomUUID() + "." + extension;
                Path pathToImage = Path.of(pathToImagesDir, fileName);
                writeToFile(pathToImage, data);

                String image = adDto.getImage();
                if (image != null) {
                    Path path = Path.of(image.substring(1));
                    Files.delete(path);
                }

                adRepository
                        .findById(adDto.getPk())
                        .map(ad -> {
                            ad.setImage(fileName);
                            return mapper.toDto(adRepository.save(ad));
                        });
                return fileName;
            } catch (IOException e) {
                throw new AdImageProcessingException();
            }
        }
        return null;
    }

    /**
     * Приватный метод, который записывает переданный файл в папку на диске
     */
    private void writeToFile(Path path, byte[] data) {
        try (FileOutputStream fos = new FileOutputStream(path.toFile())) {
            fos.write(data);
        } catch (IOException e) {
            throw new AdImageProcessingException();
        }
    }

    /**
     * Приватный метод, который получает расширение загруженного файла
     * @param fileName имя файла
     */
    private String getExtensions(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    /**
     * Приветный метод, который проверяет что комментарий редактирует пользователь создавший его или администратор.
     * @param adDto объект объявления
     */
    private boolean adBelongsToCurrentUserOrIsAdmin(AdDto adDto) {
        User user = this.getCurrentUser();
        boolean isAdmin = user.getRole().equals(Role.ADMIN);
        Integer userId = user.getId();
        return isAdmin || Objects.equals(userId, adDto.getAuthor());
    }

}
