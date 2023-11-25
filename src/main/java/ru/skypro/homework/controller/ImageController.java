package ru.skypro.homework.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.skypro.homework.service.AdService;

import java.io.IOException;

/**
 * Контроллер для обработки запросов для изображений объявлений
 */
@RestController
@RequestMapping(path = "/images")
@CrossOrigin(value = "http://localhost:3000")
public class ImageController {

    private final AdService adService;

    public ImageController(final AdService adService) {
        this.adService = adService;
    }

    @GetMapping(value = "/{fileName}", produces = {
            MediaType.IMAGE_PNG_VALUE,
            MediaType.IMAGE_JPEG_VALUE,
            MediaType.IMAGE_GIF_VALUE,
            "image/*"
    })
    public ResponseEntity<byte[]> getImage(@PathVariable(value = "fileName") final String fileName) throws IOException {
        byte[] image = adService.getImage(fileName);
        return ResponseEntity.ok().body(image);
    }

}
