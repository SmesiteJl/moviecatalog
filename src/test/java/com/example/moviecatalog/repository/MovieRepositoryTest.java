package com.example.moviecatalog.repository;

import com.example.moviecatalog.entity.BlacklistMovie;
import com.example.moviecatalog.entity.Movie;
import com.example.moviecatalog.support.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Covers the hand-written JPQL on {@link MovieRepository}; these queries back the catalogue's
 * search filters and are the easiest thing to break silently.
 */
@SpringBootTest
@Transactional
class MovieRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private BlacklistMovieRepository blacklistMovieRepository;

    private Movie inception;

    @BeforeEach
    void seed() {
        inception = movieRepository.save(movie(27205L, "Начало", "Фантастика, Боевик",
                "Кристофер Нолан", "Леонардо Ди Каприо, Том Харди"));
        movieRepository.save(movie(680L, "Криминальное чтиво", "Триллер, Криминал",
                "Квентин Тарантино", "Джон Траволта, Ума Турман"));
    }

    @Test
    void findByTitleContaining_whenQueryDiffersInCase_thenStillMatches() {
        assertThat(movieRepository.findByTitleContaining("НАЧАЛ"))
                .extracting(Movie::getTmdbId)
                .containsExactly(27205L);
    }

    @Test
    void findByGenre_whenGenreIsOneOfSeveral_thenMatchesTheMovie() {
        assertThat(movieRepository.findByGenre("Боевик"))
                .extracting(Movie::getTmdbId)
                .containsExactly(27205L);
    }

    @Test
    void findByDirectorOrCast_whenQueryMatchesDirector_thenReturnsMovie() {
        assertThat(movieRepository.findByDirectorOrCast("Тарантино"))
                .extracting(Movie::getTmdbId)
                .containsExactly(680L);
    }

    @Test
    void findByDirectorOrCast_whenQueryMatchesCastMember_thenReturnsMovie() {
        assertThat(movieRepository.findByDirectorOrCast("Ди Каприо"))
                .extracting(Movie::getTmdbId)
                .containsExactly(27205L);
    }

    @Test
    void findByDirectorOrCast_whenNobodyMatches_thenReturnsEmptyList() {
        assertThat(movieRepository.findByDirectorOrCast("Кубрик")).isEmpty();
    }

    @Test
    void findBlacklistedTmdbIds_whenMovieIsBlacklisted_thenReturnsItsTmdbId() {
        BlacklistMovie blocked = new BlacklistMovie();
        blocked.setMovieId(inception.getId());
        blacklistMovieRepository.save(blocked);

        assertThat(movieRepository.findBlacklistedTmdbIds()).containsExactly(27205L);
    }

    @Test
    void findBlacklistedTmdbIds_whenNothingIsBlacklisted_thenReturnsEmptySet() {
        assertThat(movieRepository.findBlacklistedTmdbIds()).isEmpty();
    }

    @Test
    void findByTmdbId_whenMovieIsAbsent_thenReturnsEmptyOptional() {
        assertThat(movieRepository.findByTmdbId(-1L)).isEmpty();
    }

    private static Movie movie(Long tmdbId, String title, String genres, String director, String cast) {
        Movie movie = new Movie();
        movie.setTmdbId(tmdbId);
        movie.setTitle(title);
        movie.setGenres(genres);
        movie.setDirector(director);
        movie.setCastNames(cast);
        return movie;
    }
}
