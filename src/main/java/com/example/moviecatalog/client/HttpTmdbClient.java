package com.example.moviecatalog.client;

import com.example.moviecatalog.config.TmdbProperties;
import com.example.moviecatalog.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

/**
 * Talks to the real TMDB API. Active only when {@code tmdb.mode=http}.
 *
 * <p>Every call is defensive: TMDB being slow, blocked or rate-limited degrades the page to
 * "no results" rather than propagating an exception into a controller.
 */
@Slf4j
public class HttpTmdbClient implements TmdbClient {

    private static final String DEFAULT_LANGUAGE = "ru-RU";

    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String baseUrl;

    public HttpTmdbClient(RestTemplate restTemplate, TmdbProperties properties) {
        this.restTemplate = restTemplate;
        this.apiKey = properties.api().key();
        this.baseUrl = properties.api().baseUrl();
    }

    @Override
    public TmdbMovieListResponse popular(int page) {
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/movie/popular")
                .queryParam("api_key", apiKey)
                .queryParam("language", DEFAULT_LANGUAGE)
                .queryParam("page", page)
                .toUriString();
        return get(url, TmdbMovieListResponse.class, "popular movies page " + page)
                .orElseGet(TmdbMovieListResponse::new);
    }

    @Override
    public List<TmdbMovieResponse> searchByTitle(String query) {
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/search/movie")
                .queryParam("api_key", apiKey)
                .queryParam("language", DEFAULT_LANGUAGE)
                .queryParam("query", query)
                .encode()
                .toUriString();
        return get(url, TmdbMovieListResponse.class, "search for '" + query + "'")
                .map(TmdbMovieListResponse::getResults)
                .filter(results -> results != null)
                .orElseGet(List::of);
    }

    @Override
    public TmdbMovieDetail movieDetail(Long tmdbId) {
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/movie/" + tmdbId)
                .queryParam("api_key", apiKey)
                .queryParam("language", DEFAULT_LANGUAGE)
                .toUriString();
        return get(url, TmdbMovieDetail.class, "details of movie " + tmdbId).orElse(null);
    }

    @Override
    public TmdbCreditsResponse credits(Long tmdbId) {
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/movie/" + tmdbId + "/credits")
                .queryParam("api_key", apiKey)
                .queryParam("language", DEFAULT_LANGUAGE)
                .toUriString();
        return get(url, TmdbCreditsResponse.class, "credits of movie " + tmdbId).orElse(null);
    }

    @Override
    public String trailerKey(Long tmdbId, String language) {
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/movie/" + tmdbId + "/videos")
                .queryParam("api_key", apiKey)
                .queryParam("language", language)
                .toUriString();
        return get(url, TmdbVideosResponse.class, "videos of movie " + tmdbId)
                .map(TmdbVideosResponse::getResults)
                .filter(results -> results != null)
                .flatMap(results -> results.stream()
                        .filter(v -> "YouTube".equals(v.getSite()) && "Trailer".equals(v.getType()))
                        .map(TmdbVideo::getKey)
                        .findFirst())
                .orElse(null);
    }

    private <T> java.util.Optional<T> get(String url, Class<T> type, String what) {
        try {
            return java.util.Optional.ofNullable(restTemplate.getForObject(url, type));
        } catch (Exception e) {
            log.error("TMDB request failed while fetching {}: {}", what, e.getMessage());
            return java.util.Optional.empty();
        }
    }
}
