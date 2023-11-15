package ru.skypro.homework.mapper;

import lombok.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.skypro.homework.dto.AdDto;
import ru.skypro.homework.dto.AdsDto;
import ru.skypro.homework.dto.ExtendedAdDto;
import ru.skypro.homework.entity.Ad;
import ru.skypro.homework.exception.UserNotFoundException;
import ru.skypro.homework.repository.UserRepository;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

@Component
public class AdMapper {

    private final UserRepository userRepository;
    private final String imagePath;

    public AdMapper(final UserRepository userRepository,
                    @Value("${path.to.images.folder}") String pathToImagesDir,
                    @Value("${directory.separator}") String directorySeparator) {
        this.userRepository = userRepository;
        Path pathToImages = Path.of(pathToImagesDir);
        this.imagePath = directorySeparator + pathToImages + directorySeparator;
    }

    public ExtendedAdDto toExtendedDto(@NonNull Ad ad) {
        ExtendedAdDto adDto = new ExtendedAdDto();

        adDto.setPk(ad.getPk());
        adDto.setTitle(ad.getTitle());
        adDto.setDescription(ad.getDescription());
        adDto.setPrice(ad.getPrice());
        adDto.setAuthorFirstName(ad.getAuthor().getFirstName());
        adDto.setAuthorLastName(ad.getAuthor().getLastName());
        adDto.setEmail(ad.getAuthor().getEmail());
        adDto.setPhone(ad.getAuthor().getPhone());

        Optional.ofNullable(ad.getImage())
                .ifPresent(elem -> adDto.setImage(imagePath + ad.getImage()));

        return adDto;
    }

    public AdDto toDto(@NonNull Ad ad) {
        AdDto adDto = new AdDto();

        adDto.setPk(ad.getPk());
        adDto.setTitle(ad.getTitle());
        adDto.setPrice(ad.getPrice());
        adDto.setAuthor(ad.getAuthor().getId());

        Optional.ofNullable(ad.getImage())
                .ifPresent(elem -> adDto.setImage(imagePath + ad.getImage()));

        return adDto;
    }

    public AdsDto toAdsDto(Integer count, @NonNull List<AdDto> results) {
        AdsDto adsDto = new AdsDto();

        adsDto.setCount(count);
        adsDto.setResults(results);

        return adsDto;
    }

    public Ad toEntityFromDto(AdDto adDto) {
        Ad ad = new Ad();

        ad.setTitle(adDto.getTitle());
        ad.setPrice(adDto.getPrice());
        ad.setImage(adDto.getImage());

        Optional.ofNullable(adDto.getAuthor())
                .ifPresent(authorId ->
                        ad.setAuthor(
                                userRepository.findById(authorId)
                                        .orElseThrow(UserNotFoundException::new)
                        )
                );

        return ad;
    }

}
