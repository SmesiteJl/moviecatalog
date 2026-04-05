package com.example.moviecatalog.service;

import com.example.moviecatalog.dto.*;
import com.example.moviecatalog.entity.BlacklistMovie;
import com.example.moviecatalog.entity.Movie;
import com.example.moviecatalog.repository.BlacklistMovieRepository;
import com.example.moviecatalog.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MovieService {

    private final MovieRepository movieRepository;
    private final BlacklistMovieRepository blacklistMovieRepository;
    private final RestTemplate restTemplate;

    @Value("${tmdb.api.key}")
    private String apiKey;

    @Value("${tmdb.api.base-url}")
    private String baseUrl;

    private static final Map<Integer, String> GENRE_MAP = Map.ofEntries(
            Map.entry(28, "Боевик"),
            Map.entry(12, "Приключения"),
            Map.entry(16, "Мультфильм"),
            Map.entry(35, "Комедия"),
            Map.entry(80, "Криминал"),
            Map.entry(99, "Документальный"),
            Map.entry(18, "Драма"),
            Map.entry(10751, "Семейный"),
            Map.entry(14, "Фэнтези"),
            Map.entry(36, "История"),
            Map.entry(27, "Ужасы"),
            Map.entry(10402, "Музыка"),
            Map.entry(9648, "Детектив"),
            Map.entry(10749, "Мелодрама"),
            Map.entry(878, "Фантастика"),
            Map.entry(53, "Триллер"),
            Map.entry(10752, "Военный"),
            Map.entry(37, "Вестерн")
    );

    public static final List<String> GENRE_LIST = List.of(
            "Боевик", "Приключения", "Мультфильм", "Комедия", "Криминал",
            "Документальный", "Драма", "Семейный", "Фэнтези", "История",
            "Ужасы", "Музыка", "Детектив", "Мелодрама", "Фантастика",
            "Триллер", "Военный", "Вестерн"
    );

    public TmdbMovieListResponse getPopularMoviesPage(int page) {
        try {
            String url = baseUrl + "/movie/popular?api_key=" + apiKey + "&language=ru-RU&page=" + page;
            return restTemplate.getForObject(url, TmdbMovieListResponse.class);
        } catch (Exception e) {
            log.error("Ошибка при получении популярных фильмов: {}", e.getMessage());
            return new TmdbMovieListResponse();
        }
    }

    public List<TmdbMovieResponse> getPopularMovies() {
        TmdbMovieListResponse resp = getPopularMoviesPage(1);
        return resp != null && resp.getResults() != null ? resp.getResults() : List.of();
    }

    public List<TmdbMovieResponse> searchByTitle(String query) {
        try {
            String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String url = baseUrl + "/search/movie?api_key=" + apiKey + "&language=ru-RU&query=" + encoded;
            TmdbMovieListResponse resp = restTemplate.getForObject(url, TmdbMovieListResponse.class);
            return resp != null && resp.getResults() != null ? resp.getResults() : List.of();
        } catch (Exception e) {
            log.error("Ошибка при поиске фильмов: {}", e.getMessage());
            return List.of();
        }
    }

    public Movie getOrFetchMovie(Long tmdbId) {
        return movieRepository.findByTmdbId(tmdbId)
                .orElseGet(() -> fetchAndSaveMovie(tmdbId));
    }

    private Movie fetchAndSaveMovie(Long tmdbId) {
        try {
            String detailUrl = baseUrl + "/movie/" + tmdbId + "?api_key=" + apiKey + "&language=ru-RU";
            TmdbMovieDetail detail = restTemplate.getForObject(detailUrl, TmdbMovieDetail.class);
            if (detail == null || detail.getId() == null) return null;

            String creditsUrl = baseUrl + "/movie/" + tmdbId + "/credits?api_key=" + apiKey + "&language=ru-RU";
            TmdbCreditsResponse credits = restTemplate.getForObject(creditsUrl, TmdbCreditsResponse.class);

            // Пробуем русские видео, затем английские
            String trailerKey = fetchTrailerKey(tmdbId, "ru-RU");
            if (trailerKey == null) {
                trailerKey = fetchTrailerKey(tmdbId, "en-US");
            }

            Movie movie = new Movie();
            movie.setTmdbId(tmdbId);
            movie.setTitle(detail.getTitle() != null ? detail.getTitle() : "Без названия");
            movie.setOriginalTitle(detail.getOriginalTitle());
            movie.setOverview(detail.getOverview());
            movie.setPosterPath(detail.getPosterPath());
            movie.setBackdropPath(detail.getBackdropPath());

            if (detail.getReleaseDate() != null && !detail.getReleaseDate().isBlank()) {
                try {
                    movie.setReleaseDate(LocalDate.parse(detail.getReleaseDate()));
                } catch (Exception ignored) {}
            }

            movie.setTmdbRating(detail.getVoteAverage());

            if (detail.getGenres() != null) {
                String genres = detail.getGenres().stream()
                        .map(TmdbGenre::getName)
                        .collect(Collectors.joining(", "));
                movie.setGenres(genres);
            }

            if (credits != null && credits.getCrew() != null) {
                String director = credits.getCrew().stream()
                        .filter(c -> "Director".equals(c.getJob()))
                        .map(TmdbCrew::getName)
                        .findFirst().orElse(null);
                movie.setDirector(director);
            }

            if (credits != null && credits.getCast() != null) {
                String cast = credits.getCast().stream()
                        .limit(6)
                        .map(TmdbCast::getName)
                        .collect(Collectors.joining(", "));
                movie.setCastNames(cast);
            }

            movie.setTrailerKey(trailerKey);

            return movieRepository.save(movie);
        } catch (Exception e) {
            log.error("Ошибка при получении деталей фильма {}: {}", tmdbId, e.getMessage());
            return null;
        }
    }

    private String fetchTrailerKey(Long tmdbId, String language) {
        try {
            String url = baseUrl + "/movie/" + tmdbId + "/videos?api_key=" + apiKey + "&language=" + language;
            TmdbVideosResponse videos = restTemplate.getForObject(url, TmdbVideosResponse.class);
            if (videos != null && videos.getResults() != null) {
                return videos.getResults().stream()
                        .filter(v -> "YouTube".equals(v.getSite()) && "Trailer".equals(v.getType()))
                        .map(TmdbVideo::getKey)
                        .findFirst().orElse(null);
            }
        } catch (Exception e) {
            log.warn("Не удалось получить трейлер для фильма {} ({}): {}", tmdbId, language, e.getMessage());
        }
        return null;
    }

    public Set<Long> getBlacklistedTmdbIds() {
        try {
            return movieRepository.findBlacklistedTmdbIds();
        } catch (Exception e) {
            return Set.of();
        }
    }

    public boolean isBlacklisted(Long tmdbId) {
        return movieRepository.findByTmdbId(tmdbId)
                .map(m -> blacklistMovieRepository.existsById(m.getId()))
                .orElse(false);
    }

    public List<Movie> searchLocalByGenre(String genre) {
        return movieRepository.findByGenre(genre);
    }

    public List<Movie> searchLocalByPerson(String query) {
        return movieRepository.findByDirectorOrCast(query);
    }

    public List<Movie> searchLocalByTitle(String title) {
        return movieRepository.findByTitleContaining(title);
    }

    public String mapGenreIds(List<Integer> genreIds) {
        if (genreIds == null) return "";
        return genreIds.stream()
                .map(id -> GENRE_MAP.getOrDefault(id, ""))
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining(", "));
    }

    public void addToBlacklist(Movie movie) {
        if (!blacklistMovieRepository.existsById(movie.getId())) {
            BlacklistMovie bl = new BlacklistMovie();
            bl.setMovieId(movie.getId());
            blacklistMovieRepository.save(bl);
        }
    }

    public void removeFromBlacklist(Long movieId) {
        blacklistMovieRepository.deleteById(movieId);
    }

    public List<BlacklistMovie> getBlacklist() {
        return blacklistMovieRepository.findAll();
    }
}
