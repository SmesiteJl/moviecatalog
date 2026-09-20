package com.example.moviecatalog.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Shape of {@code classpath:offline/catalogue.json} — the catalogue the application falls back to
 * when TMDB is not configured. Kept deliberately close to the TMDB payload so that the offline
 * and HTTP clients produce interchangeable results.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OfflineCatalogue(List<Entry> movies) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Entry(
            Long id,
            String title,
            String originalTitle,
            String overview,
            String posterPath,
            String backdropPath,
            String releaseDate,
            Double voteAverage,
            List<Integer> genreIds,
            List<Genre> genres,
            String director,
            List<String> cast
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Genre(Integer id, String name) {
    }
}
