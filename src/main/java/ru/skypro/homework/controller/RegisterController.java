package ru.skypro.homework.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.skypro.homework.dto.RegisterDto;
import ru.skypro.homework.service.RegisterService;

/**
 * Контроллер для обработки запросов для авторизации
 */
@RestController
@RequestMapping(path = "/register")
@CrossOrigin(value = "http://localhost:3000")
public class RegisterController {

    private final RegisterService registerService;

    public RegisterController(final RegisterService registerService) {
        this.registerService = registerService;
    }

    /**
     * Регистрирование пользователя
     * <br>Используется метод сервиса {@link ru.skypro.homework.service.impl.RegisterServiceImpl#registerUser}
     * @param register RegisterDto
     */
    @PostMapping
    public ResponseEntity<?> registerUser(@RequestBody RegisterDto register) {
        if (registerService.registerUser(register)) {
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

}
