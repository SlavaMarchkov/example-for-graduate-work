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

    @Override
    public User getCurrentUser() {
        Authentication authenticationUser = SecurityContextHolder.getContext().getAuthentication();
        UserDetails principalUser = (UserDetails) authenticationUser.getPrincipal();
        return userRepository.findByEmail(principalUser.getUsername());
    }

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

    @Override
    public ExtendedAdDto get(Integer id) {
        return adRepository
                .findById(id)
                .map(mapper::toExtendedDto)
                .orElse(null);
    }

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

    @Override
    public AdDto findAdById(Integer id) {
        return adRepository
                .findById(id)
                .map(mapper::toDto)
                .orElse(null);
    }

    @Override
    public byte[] getImage(final String fileName) throws IOException {
        Path path = Path.of(pathToImagesDir, fileName);
        return new ByteArrayResource(Files
                .readAllBytes(path)
        ).getByteArray();
    }

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

    private void writeToFile(Path path, byte[] data) {
        try (FileOutputStream fos = new FileOutputStream(path.toFile())) {
            fos.write(data);
        } catch (IOException e) {
            throw new AdImageProcessingException();
        }
    }

    private String getExtensions(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    private boolean adBelongsToCurrentUserOrIsAdmin(AdDto adDto) {
        User user = this.getCurrentUser();
        boolean isAdmin = user.getRole().equals(Role.ADMIN);
        Integer userId = user.getId();
        return isAdmin || Objects.equals(userId, adDto.getAuthor());
    }

}
