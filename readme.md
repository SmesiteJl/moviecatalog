# Кинокаталог

**Учебный проект по практике** — веб-сервис с каталогом фильмов

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3-brightgreen)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue)
![Thymeleaf](https://img.shields.io/badge/Thymeleaf-3.1-green)

## О проекте

Каталог фильмов с интеграцией TMDB API.  


### Основной функционал
- Просмотр популярных фильмов
- Поиск по названию, жанру, актёрам и режиссёру
- Детальная страница фильма (постер, трейлер YouTube, рейтинг TMDB, описание)
- Регистрация и авторизация пользователей
- Личный кабинет
- Добавление фильмов в «Избранное»
- Отзывы и оценки (свои + средний рейтинг)
- Админ-панель (блокировка пользователей, удаление отзывов, blacklist фильмов)

## Технологический стек

- **Backend**: Java 21 + Spring Boot 3.3
- **Шаблонизатор**: Thymeleaf (Server-Side Rendering)
- **Frontend**: Bootstrap 5 + vanilla JavaScript
- **База данных**: PostgreSQL + Spring Data JPA + Hibernate
- **Безопасность**: Spring Security (form-login)
- **Внешнее API**: TMDB API v3

## Инструкции для запуска 

### 1. Создание базы данных PostgreSQL

Выполните команды (в pgAdmin, psql или IntelliJ Database Console):

```sql
CREATE DATABASE moviecatalog;

CREATE USER movieuser WITH PASSWORD 'moviepass';

GRANT ALL PRIVILEGES ON DATABASE moviecatalog TO movieuser;
```
### 2. Запуск проекта
```bash
mvn clean spring-boot:run
```
Приложение будет доступно по адресу: http://localhost:8080

### Учётные данные
Администратор:
Логин: admin <br>
Пароль: admin123<br>

Обычный пользователь - зарегистрируйтесь на странице "Регистрация"
