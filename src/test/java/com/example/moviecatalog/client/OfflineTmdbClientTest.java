package com.example.moviecatalog.client;

import com.example.moviecatalog.dto.TmdbCreditsResponse;
import com.example.moviecatalog.dto.TmdbMovieDetail;
import com.example.moviecatalog.dto.TmdbMovieListResponse;
import com.example.moviecatalog.dto.TmdbMovieResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The offline catalogue is what a reviewer sees on a fresh clone, so it is covered like a
 * production data source rather than like a fixture.
 */
class OfflineTmdbClientTest {

    private static OfflineTmdbClient client;

    @BeforeAll
    static void setUp() {
        client = new OfflineTmdbClient(new ObjectMapper(), "offline/catalogue.json");
    }

    @Test
    void popular_whenFirstPageRequested_thenReturnsNonEmptyPageWithTotals() {
        TmdbMovieListResponse page = client.popular(1);

        assertThat(page.getResults()).isNotEmpty();
        assertThat(page.getPage()).isEqualTo(1);
        assertThat(page.getTotalResults()).isEqualTo(20);
        assertThat(page.getTotalPages()).isEqualTo(1);
    }

    @Test
    void popular_whenPageBeyondCatalogueRequested_thenReturnsEmptyResults() {
        assertThat(client.popular(99).getResults()).isEmpty();
    }

    @Test
    void popular_whenCalled_thenEveryEntryCarriesArtworkAndTitle() {
        List<TmdbMovieResponse> results = client.popular(1).getResults();

        assertThat(results).allSatisfy(movie -> {
            assertThat(movie.getTitle()).isNotBlank();
            assertThat(movie.getPosterPath()).isNotBlank();
            assertThat(movie.getId()).isNotNull();
        });
    }

    @Test
    void searchByTitle_whenLocalisedTitleMatchesPartially_thenReturnsMovie() {
        assertThat(client.searchByTitle("матриц"))
                .extracting(TmdbMovieResponse::getTitle)
                .contains("Матрица");
    }

    @Test
    void searchByTitle_whenOriginalTitleMatches_thenReturnsMovie() {
        assertThat(client.searchByTitle("Inception"))
                .extracting(TmdbMovieResponse::getId)
                .contains(27205L);
    }

    @Test
    void searchByTitle_whenQueryIsBlank_thenReturnsEmptyList() {
        assertThat(client.searchByTitle("   ")).isEmpty();
        assertThat(client.searchByTitle(null)).isEmpty();
    }

    @Test
    void searchByTitle_whenNothingMatches_thenReturnsEmptyList() {
        assertThat(client.searchByTitle("zzzz-no-such-film")).isEmpty();
    }

    @Test
    void movieDetail_whenMovieExists_thenReturnsGenresAndRating() {
        TmdbMovieDetail detail = client.movieDetail(27205L);

        assertThat(detail).isNotNull();
        assertThat(detail.getTitle()).isEqualTo("Начало");
        assertThat(detail.getVoteAverage()).isPositive();
        assertThat(detail.getGenres()).isNotEmpty();
    }

    @Test
    void movieDetail_whenMovieIsUnknown_thenReturnsNull() {
        assertThat(client.movieDetail(-1L)).isNull();
    }

    @Test
    void credits_whenMovieExists_thenExposesDirectorAsCrewMemberWithDirectorJob() {
        TmdbCreditsResponse credits = client.credits(27205L);

        assertThat(credits).isNotNull();
        assertThat(credits.getCrew())
                .anySatisfy(crew -> assertThat(crew.getJob()).isEqualTo("Director"));
        assertThat(credits.getCast()).isNotEmpty();
    }

    @Test
    void credits_whenMovieIsUnknown_thenReturnsNull() {
        assertThat(client.credits(-1L)).isNull();
    }

    @Test
    void trailerKey_whenCalled_thenReturnsNullBecauseOfflineModeAvoidsTheNetwork() {
        assertThat(client.trailerKey(27205L, "ru-RU")).isNull();
    }

    @Test
    void constructor_whenCatalogueIsMissing_thenFailsFast() {
        assertThatThrownBy(() -> new OfflineTmdbClient(new ObjectMapper(), "offline/does-not-exist.json"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("offline catalogue");
    }
}
