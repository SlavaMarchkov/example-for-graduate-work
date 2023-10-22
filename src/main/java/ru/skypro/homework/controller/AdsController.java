package ru.skypro.homework.controller;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.AdDto;
import ru.skypro.homework.dto.AdsDto;
import ru.skypro.homework.dto.CreateAdDto;
import ru.skypro.homework.dto.ExtendedAdDto;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping(path = "/ads")
@CrossOrigin(value = "http://localhost:3000")
public class AdsController {

    @GetMapping
    public AdsDto getAllAds() {
        System.out.println("Get All Ads");
        return new AdsDto();
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AdDto addAd(@RequestBody CreateAdDto ad,
                       @RequestPart(name = "image") MultipartFile multipartFile) {
        System.out.println("Add Ad");
        return new AdDto();
    }

    @GetMapping(path = "{id}")
    public ExtendedAdDto getAdById(@PathVariable(value = "id") Integer id) {
        System.out.println("Get Ad by Id");
        return new ExtendedAdDto();
    }

    @DeleteMapping(path = "{id}")
    public ResponseEntity<AdDto> deleteAdById(@PathVariable(value = "id") Integer id) {
        System.out.println("Delete Ad by Id");
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PatchMapping(path = "{id}")
    public AdDto updateAdById(@PathVariable(value = "id") Integer id,
                              @RequestBody CreateAdDto ad) {
        System.out.println("Update Ad by Id");
        return new AdDto();
    }

    @GetMapping(path = "/me")
    public List<AdDto> getAuthorizedUserAds() {
        System.out.println("Get All Ads of Authorized User");
        return List.of(new AdDto[]{});
    }

    @PatchMapping(
            path = "{id}/image",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    public @ResponseBody Resource updateImageByAdId(@PathVariable(value = "id") Integer id,
                                                    @RequestPart(name = "image") MultipartFile file) throws IOException {
        System.out.println("Update Image of Ad by AdId");
        return new ByteArrayResource(Files.readAllBytes(Paths.get("mto.jpg")));
    }

}