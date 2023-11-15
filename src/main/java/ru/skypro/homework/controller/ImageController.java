package ru.skypro.homework.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.skypro.homework.service.AdService;

import java.io.IOException;

@RestController
@RequestMapping(path = "/images")
@CrossOrigin(value = "http://localhost:3000")
public class ImageController {

    private final AdService adService;

    public ImageController(final AdService adService) {
        this.adService = adService;
    }

    @GetMapping(value = "/{image}", produces = {
            MediaType.IMAGE_PNG_VALUE,
            MediaType.IMAGE_JPEG_VALUE,
            MediaType.IMAGE_GIF_VALUE,
            "image/*"
    })
    public ResponseEntity<byte[]> getImage(@PathVariable(value = "image") final String image) throws IOException {
        byte[] imageResource = adService.getImage(image);
        return ResponseEntity.ok().body(imageResource);
    }

}
