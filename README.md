# Back-end часть интернет-магазина
Реализация back-end части интернет-сервиса для размещения объявлений о товарах. Проект реализован на основе front-end части - https://github.com/BizinMitya/front-react-avito

## Основные возможности
- Авторизация и аутентификация пользователей.
- Распределение ролей между пользователями: пользователь и администратор.
- CRUD-операции для объявлений и комментариев: администратор может удалять или редактировать все объявления и комментарии, а пользователи — только свои.
- Возможность для пользователей оставлять комментарии под каждым объявлением.
- Показ и сохранение картинок объявлений, а также аватарок пользователей.

## Технологии используемые в проекте
- Java 11 
- Maven 
- Spring Boot 
- Spring Web 
- Spring Data JPA 
- Spring Security 
- Swagger 
- PostgreSQL 
- Liquibase

## Структура проекта
```
/src/main/
    java/ru/skypro/homework/
        config/                # Конфигурация Spring security и Swagger
        controller/            # Контроллеры
        dto/                   # Data transfer objects
        entity/                # Сущности
        exception/             # Исключеия обрабатываемые в приложении
        filter/                # фильтор применяемый для определения роли пользователя
        mapper/                # Мапперы
        repository/            # Репозитории
        service/               # Интерфейсы сервисов
            impl/              # Их реализация
    resources/                 # Настройки приложения
        liquibase              # Инициализация БД средствами Liquibase
```
## Установка и подключение
### Установка
1. Скопируйте репозиторий с [GitHub](https://github.com/SlavaMarchkov/team5-graduate-work).
2. Установите [Docker](https://www.docker.com/).
3. Загрузите зависимости.

### ***Запуск***
1. Запуск проекта в IntelliJ IDEA.
2. Запуск Docker через командную строку.
3. Ввести в командную строку ```docker ps```,а потом <br/> ```docker run --rm -p 3000:3000 ghcr.io/bizinmitya/front-react-avito:v1.17```.
5. После этого зайти в браузер и ввести адрес ```localhost:3000```.
7. Проект запущен

## Над проектом работали
- Марчков Вячеслав ([SlavaMarchkov](https://github.com/SlavaMarchkov))
- Родионов Георгий ([george2066](https://github.com/george2066))
- Зражевский Роман ([ZRoman87](https://github.com/ZRoman87))
- Кудрявцев Владимир ([ztmwtm](https://github.com/ztmwtm))
- Шорикова Анастасия ([DuBlack1](https://github.com/DuBlack1))