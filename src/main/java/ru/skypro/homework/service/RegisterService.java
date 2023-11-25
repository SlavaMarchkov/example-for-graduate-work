package ru.skypro.homework.service;

import ru.skypro.homework.dto.RegisterDto;

/**
 * Интерфейс для работы с регистрацией
 */
public interface RegisterService {
    boolean registerUser(RegisterDto register);
}
