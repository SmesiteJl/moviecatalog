package com.example.moviecatalog.client;

import com.example.moviecatalog.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Serves a catalogue bundled into the jar, so the application is fully usable with no TMDB key,
 * no network and no proxy. This is the default source; see {@code tmdb.mode}.
 *
 * <p>The catalogue is read once at construction and held in memory — it is a few kilobytes.
 */
@Slf4j
public class OfflineTmdbClient implements TmdbClient {

    private static final int PAGE_SIZE = 20;

    private final List<OfflineCatalogue.Entry> entries;
    private final Map<Long, OfflineCatalogue.Entry> byId;

    public OfflineTmdbClient(ObjectMapper objectMapper, String catalogueLocation) {
        this.entries = load(objectMapper, catalogueLocation);
        this.byId = entries.stream()
                .collect(Collectors.toMap(OfflineCatalogue.Entry::id, Function.identity()));
        log.info("Offline movie catalogue loaded: {} titles. Set tmdb.mode=http to use the live TMDB API.",
                entries.size());
    }

    private static List<OfflineCatalogue.Entry> load(ObjectMapper objectMapper, String location) {
        try (InputStream in = new ClassPathResource(location).getInputStream()) {
            OfflineCatalogue catalogue = objectMapper.readValue(in, OfflineCatalogue.class);
            return catalogue.movies() == null ? List.of() : List.copyOf(catalogue.movies());
        } catch (IOException e) {
            throw new IllegalStateException("Bundled offline catalogue is missing or unreadable: " + location, e);
        }
    }

    @Override
    public TmdbMovieListResponse popular(int page) {
        int from = Math.max(0, (page - 1) * PAGE_SIZE);
        int to = Math.min(entries.size(), from + PAGE_SIZE);
        List<TmdbMovieResponse> slice = from >= entries.size()
                ? List.of()
                : entries.subList(from, to).stream().map(OfflineTmdbClient::toSummary).toList();

        TmdbMovieListResponse response = new TmdbMovieListResponse();
        response.setResults(slice);
        response.setPage(page);
        response.setTotalResults(entries.size());
        response.setTotalPages((int) Math.ceil((double) entries.size() / PAGE_SIZE));
        return response;
    }

    @Override
    public List<TmdbMovieResponse> searchByTitle(String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        String needle = query.toLowerCase(Locale.ROOT).trim();
        return entries.stream()
                .filter(e -> contains(e.title(), needle) || contains(e.originalTitle(), needle))
                .map(OfflineTmdbClient::toSummary)
                .toList();
    }

    @Override
    public TmdbMovieDetail movieDetail(Long tmdbId) {
        OfflineCatalogue.Entry entry = byId.get(tmdbId);
        if (entry == null) {
            return null;
        }
        TmdbMovieDetail detail = new TmdbMovieDetail();
        detail.setId(entry.id());
        detail.setTitle(entry.title());
        detail.setOriginalTitle(entry.originalTitle());
        detail.setOverview(entry.overview());
        detail.setPosterPath(entry.posterPath());
        detail.setBackdropPath(entry.backdropPath());
        detail.setReleaseDate(entry.releaseDate());
        detail.setVoteAverage(entry.voteAverage());
        detail.setGenres(entry.genres() == null ? List.of() : entry.genres().stream()
                .map(g -> {
                    TmdbGenre genre = new TmdbGenre();
                    genre.setId(g.id());
                    genre.setName(g.name());
                    return genre;
                })
                .toList());
        return detail;
    }

    @Override
    public TmdbCreditsResponse credits(Long tmdbId) {
        OfflineCatalogue.Entry entry = byId.get(tmdbId);
        if (entry == null) {
            return null;
        }
        TmdbCreditsResponse credits = new TmdbCreditsResponse();

        TmdbCrew director = new TmdbCrew();
        director.setName(entry.director());
        director.setJob("Director");
        director.setDepartment("Directing");
        credits.setCrew(entry.director() == null ? List.of() : List.of(director));

        List<String> names = entry.cast() == null ? List.of() : entry.cast();
        credits.setCast(names.stream().map(name -> {
            TmdbCast cast = new TmdbCast();
            cast.setName(name);
            return cast;
        }).toList());

        return credits;
    }

    /**
     * Always {@code null}: an embedded YouTube player would need the network the offline mode
     * exists to avoid, and the detail page already renders without a trailer.
     */
    @Override
    public String trailerKey(Long tmdbId, String language) {
        return null;
    }

    private static boolean contains(String value, String lowercaseNeedle) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(lowercaseNeedle);
    }

    private static TmdbMovieResponse toSummary(OfflineCatalogue.Entry entry) {
        TmdbMovieResponse summary = new TmdbMovieResponse();
        summary.setId(entry.id());
        summary.setTitle(entry.title());
        summary.setOriginalTitle(entry.originalTitle());
        summary.setOverview(entry.overview());
        summary.setPosterPath(entry.posterPath());
        summary.setBackdropPath(entry.backdropPath());
        summary.setReleaseDate(entry.releaseDate());
        summary.setVoteAverage(entry.voteAverage());
        summary.setGenreIds(entry.genreIds() == null ? List.of() : entry.genreIds());
        return summary;
    }
}
