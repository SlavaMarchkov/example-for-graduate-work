package ru.skypro.homework.mapper;

import lombok.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import ru.skypro.homework.dto.UserDto;
import ru.skypro.homework.dto.UserPrincipalDto;
import ru.skypro.homework.entity.User;

import java.util.Optional;

@Component
public class UserMapper {

    private final String fullAvatarPath;

    public UserMapper(@Value("${path.to.avatars.folder}") String pathToAvatarsDir) {
        this.fullAvatarPath = UriComponentsBuilder.newInstance()
                .path("/" + pathToAvatarsDir + "/")
                .build()
                .toUriString();
    }

    public UserDto toDto(@NonNull User user) {
        UserDto userDto = new UserDto();

        userDto.setId(user.getId());
        userDto.setEmail(user.getEmail());
        userDto.setFirstName(user.getFirstName());
        userDto.setLastName(user.getLastName());
        userDto.setPhone(user.getPhone());
        userDto.setRole(user.getRole());

        Optional.ofNullable(user.getImage())
                .ifPresent(elem -> userDto.setImage(fullAvatarPath + user.getImage()));

        return userDto;
    }

    public UserPrincipalDto toUserPrincipalDto(@NonNull User user) {
        UserPrincipalDto userDto = new UserPrincipalDto();

        userDto.setId(user.getId());
        userDto.setEmail(user.getEmail());
        userDto.setPassword(user.getPassword());
        userDto.setRole(user.getRole());

        return userDto;
    }

    public User toEntity(UserDto userDto) {
        User user = new User();

        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setEmail(userDto.getEmail());
        user.setImage(userDto.getImage());
        user.setPhone(userDto.getPhone());
        user.setRole(userDto.getRole());

        return user;
    }

}
