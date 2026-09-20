package com.example.moviecatalog.service;

import com.example.moviecatalog.client.TmdbClient;
import com.example.moviecatalog.dto.*;
import com.example.moviecatalog.entity.Movie;
import com.example.moviecatalog.repository.BlacklistMovieRepository;
import com.example.moviecatalog.repository.MovieRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovieServiceTest {

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private BlacklistMovieRepository blacklistMovieRepository;

    @Mock
    private TmdbClient tmdbClient;

    @InjectMocks
    private MovieService movieService;

    @Test
    void getOrFetchMovie_whenMovieAlreadyStored_thenDoesNotCallTheMovieSource() {
        Movie stored = new Movie();
        stored.setTmdbId(27205L);
        when(movieRepository.findByTmdbId(27205L)).thenReturn(Optional.of(stored));

        assertThat(movieService.getOrFetchMovie(27205L)).isSameAs(stored);
        verifyNoInteractions(tmdbClient);
    }

    @Test
    void getOrFetchMovie_whenMovieIsUnknownLocally_thenFetchesMapsAndSavesIt() {
        when(movieRepository.findByTmdbId(27205L)).thenReturn(Optional.empty());
        when(tmdbClient.movieDetail(27205L)).thenReturn(detail());
        when(tmdbClient.credits(27205L)).thenReturn(credits());
        when(tmdbClient.trailerKey(eq(27205L), any())).thenReturn(null);
        when(movieRepository.save(any(Movie.class))).thenAnswer(inv -> inv.getArgument(0));

        Movie saved = movieService.getOrFetchMovie(27205L);

        assertThat(saved.getTitle()).isEqualTo("Начало");
        assertThat(saved.getReleaseDate()).isEqualTo(LocalDate.of(2010, 7, 16));
        assertThat(saved.getGenres()).isEqualTo("Фантастика, Боевик");
        assertThat(saved.getDirector()).isEqualTo("Кристофер Нолан");
        assertThat(saved.getCastNames()).isEqualTo("Леонардо Ди Каприо, Том Харди");
        verify(movieRepository).save(any(Movie.class));
    }

    @Test
    void getOrFetchMovie_whenMovieSourceReturnsNothing_thenReturnsNullWithoutSaving() {
        when(movieRepository.findByTmdbId(404L)).thenReturn(Optional.empty());
        when(tmdbClient.movieDetail(404L)).thenReturn(null);

        assertThat(movieService.getOrFetchMovie(404L)).isNull();
        verify(movieRepository, never()).save(any());
    }

    @Test
    void getOrFetchMovie_whenReleaseDateIsUnparseable_thenKeepsTheMovieWithoutADate() {
        TmdbMovieDetail detail = detail();
        detail.setReleaseDate("not-a-date");
        when(movieRepository.findByTmdbId(27205L)).thenReturn(Optional.empty());
        when(tmdbClient.movieDetail(27205L)).thenReturn(detail);
        when(tmdbClient.credits(27205L)).thenReturn(credits());
        when(movieRepository.save(any(Movie.class))).thenAnswer(inv -> inv.getArgument(0));

        Movie saved = movieService.getOrFetchMovie(27205L);

        assertThat(saved).isNotNull();
        assertThat(saved.getReleaseDate()).isNull();
    }

    @Test
    void getPopularMovies_whenSourceReturnsNoResults_thenReturnsEmptyListRatherThanNull() {
        when(tmdbClient.popular(1)).thenReturn(new TmdbMovieListResponse());

        assertThat(movieService.getPopularMovies()).isEmpty();
    }

    @Test
    void mapGenreIds_whenIdsAreKnown_thenJoinsLocalisedNames() {
        assertThat(movieService.mapGenreIds(List.of(878, 28))).isEqualTo("Фантастика, Боевик");
    }

    @Test
    void mapGenreIds_whenIdsAreUnknownOrNull_thenSkipsThemSilently() {
        assertThat(movieService.mapGenreIds(List.of(999_999))).isEmpty();
        assertThat(movieService.mapGenreIds(null)).isEmpty();
    }

    @Test
    void addToBlacklist_whenMovieIsAlreadyBlacklisted_thenDoesNotInsertADuplicate() {
        Movie movie = new Movie();
        movie.setId(7L);
        when(blacklistMovieRepository.existsById(7L)).thenReturn(true);

        movieService.addToBlacklist(movie);

        verify(blacklistMovieRepository, never()).save(any());
    }

    @Test
    void isBlacklisted_whenMovieIsNotStoredLocally_thenReturnsFalse() {
        when(movieRepository.findByTmdbId(1L)).thenReturn(Optional.empty());

        assertThat(movieService.isBlacklisted(1L)).isFalse();
    }

    private static TmdbMovieDetail detail() {
        TmdbMovieDetail detail = new TmdbMovieDetail();
        detail.setId(27205L);
        detail.setTitle("Начало");
        detail.setReleaseDate("2010-07-16");
        detail.setVoteAverage(8.4);
        TmdbGenre sciFi = new TmdbGenre();
        sciFi.setName("Фантастика");
        TmdbGenre action = new TmdbGenre();
        action.setName("Боевик");
        detail.setGenres(List.of(sciFi, action));
        return detail;
    }

    private static TmdbCreditsResponse credits() {
        TmdbCreditsResponse credits = new TmdbCreditsResponse();
        TmdbCrew director = new TmdbCrew();
        director.setName("Кристофер Нолан");
        director.setJob("Director");
        TmdbCrew writer = new TmdbCrew();
        writer.setName("Кто-то ещё");
        writer.setJob("Writer");
        credits.setCrew(List.of(writer, director));

        TmdbCast first = new TmdbCast();
        first.setName("Леонардо Ди Каприо");
        TmdbCast second = new TmdbCast();
        second.setName("Том Харди");
        credits.setCast(List.of(first, second));
        return credits;
    }
}
