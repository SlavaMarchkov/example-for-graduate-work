package ru.skypro.homework.service.impl;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.skypro.homework.dto.UserPrincipalDto;
import ru.skypro.homework.entity.User;
import ru.skypro.homework.entity.UserPrincipal;
import ru.skypro.homework.mapper.UserMapper;
import ru.skypro.homework.repository.UserRepository;

/**
 * Сервис для получения данный текущего пользователя
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserDetailsServiceImpl(final UserRepository userRepository,
                                  final UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    /**
     * Метод, который получает текущего пользователя из базы данных
     * @param username
     * @return объект с данными аутентифицированного пользователя
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username);
        UserPrincipalDto userDto = userMapper.toUserPrincipalDto(user);
        if (userDto == null) {
            throw new UsernameNotFoundException(username);
        }
        return new UserPrincipal(userDto);
    }

}
