package com.example.moviecatalog.client;

import com.example.moviecatalog.dto.TmdbCreditsResponse;
import com.example.moviecatalog.dto.TmdbMovieDetail;
import com.example.moviecatalog.dto.TmdbMovieListResponse;
import com.example.moviecatalog.dto.TmdbMovieResponse;

import java.util.List;

/**
 * The catalogue's only route to movie metadata.
 *
 * <p>Two implementations exist: {@link HttpTmdbClient} talks to the real TMDB API,
 * {@link OfflineTmdbClient} serves a catalogue bundled into the jar. Callers cannot tell
 * them apart, which is what lets the application boot without credentials.
 *
 * <p>Implementations never throw for an unreachable or empty source: they return empty
 * results so that a degraded movie source cannot take a page down.
 */
public interface TmdbClient {

    TmdbMovieListResponse popular(int page);

    List<TmdbMovieResponse> searchByTitle(String query);

    TmdbMovieDetail movieDetail(Long tmdbId);

    TmdbCreditsResponse credits(Long tmdbId);

    String trailerKey(Long tmdbId, String language);
}
