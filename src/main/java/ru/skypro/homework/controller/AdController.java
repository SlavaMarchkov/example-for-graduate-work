package ru.skypro.homework.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.AdDto;
import ru.skypro.homework.dto.AdsDto;
import ru.skypro.homework.dto.CreateOrUpdateAdDto;
import ru.skypro.homework.dto.ExtendedAdDto;
import ru.skypro.homework.service.AdService;
import ru.skypro.homework.service.impl.AdServiceImpl;

import java.io.IOException;

/**
 * Контроллер для обработки запросов для объявлений
 */
@RestController
@RequestMapping(path = "/ads")
@CrossOrigin(value = "http://localhost:3000")
public class AdController {
    private final AdService service;

    public AdController(final AdService service) {
        this.service = service;
    }

    /**
     * Вывод всех объявлений.
     * <br>Используется метод сервиса {@link AdServiceImpl#getAll()}
     * @return AdsDto
     */
    @GetMapping
    public ResponseEntity<AdsDto> getAllAds() {
        return ResponseEntity.ok(
                service.getAll()
        );
    }

    /**
     * Добавления нового объявления авторизованным пользователем
     * <br>Используется метод сервиса {@link AdServiceImpl#create}
     * @param ad    CreateOrUpdateAdDto
     * @param file  MultipartFile
     * @return AdDto
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AdDto> addAd(@RequestPart(name = "properties") CreateOrUpdateAdDto ad,
                                       @RequestPart(name = "image") MultipartFile file) {
        return ResponseEntity.ok(
                service.create(ad, file)
        );
    }

    /**
     * Вывод объявления по идентификатору
     * Используется метод сервиса {@link AdServiceImpl#get}
     * @param id Integer
     * @return ExtendedAdDto
     */
    @GetMapping(path = "/{id}")
    public ResponseEntity<ExtendedAdDto> getAdById(@PathVariable(value = "id") Integer id) {
        ExtendedAdDto ad = service.get(id);
        return (ad != null)
                ? ResponseEntity.ok(ad)
                : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    /**
     * Удаление объявления по идентификатору
     * <br>Используется метод сервиса {@link AdServiceImpl#delete}
     * @param id Integer
     * @return Void (статус 200 OK)
     */
    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> deleteAdById(@PathVariable(value = "id") Integer id) {
        AdDto ad = service.findAdById(id);
        return (ad == null)
                ? ResponseEntity.status(HttpStatus.NOT_FOUND).build()
                : service.delete(ad)
                ? ResponseEntity.status(HttpStatus.NO_CONTENT).build()
                : ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    /**
     * Обновление объявления по идентификатору
     * <br>Используется метод сервиса {@link AdServiceImpl#update}
     * @param id  Integer
     * @param ad  CreateOrUpdateAdDto
     * @return AdDto
     */
    @PatchMapping(path = "/{id}")
    public ResponseEntity<AdDto> updateAdById(@PathVariable(value = "id") Integer id,
                                              @RequestBody CreateOrUpdateAdDto ad) {
        AdDto adDto = service.findAdById(id);
        if (adDto == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } else {
            AdDto updatedAd = service.update(id, ad);
            return (updatedAd != null)
                    ? ResponseEntity.ok(updatedAd)
                    : ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    /**
     * Получение объявлений авторизованного пользователя.
     * <br>Используется метод сервиса {@link AdServiceImpl#getAuthorizedUserAds}
     * @return AdsDto
     */
    @GetMapping(path = "/me")
    public ResponseEntity<AdsDto> getAuthorizedUserAds() {
        return ResponseEntity.ok(service.getAuthorizedUserAds());
    }

    /**
     * Обновление фотографий объявления
     * <br>Используется метод сервиса {@link AdServiceImpl#updateImage}
     * @param id     Integer
     * @param file   MultipartFile
     * @return Resource
     */
    @PatchMapping(
            path = "/{id}/image",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    public ResponseEntity<byte[]> updateImageByAdId(@PathVariable(value = "id") Integer id,
                                                      @RequestPart(name = "image") MultipartFile file) throws IOException {
        AdDto adDto = service.findAdById(id);
        if (adDto == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } else {
            String fileName = service.updateImage(id, file);
            if (fileName != null) {
                byte[] image = service.getImage(fileName);
                return ResponseEntity.ok().body(image);
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
    }

}
