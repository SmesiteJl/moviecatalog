package com.example.moviecatalog.web;

import com.example.moviecatalog.config.TmdbProperties;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Artwork URLs are the one thing that must differ between offline and HTTP mode, so both
 * branches are pinned down here.
 */
class PosterUrlResolverTest {

    private static TmdbProperties properties(TmdbProperties.Mode mode, String imageBaseUrl) {
        return new TmdbProperties(
                mode,
                new TmdbProperties.Api("key", "https://api.themoviedb.org/3"),
                imageBaseUrl,
                new TmdbProperties.Proxy(false, "127.0.0.1", 12334),
                new TmdbProperties.Timeout(1000, 1000));
    }

    @Test
    void url_whenOffline_thenServesArtworkFromLocalStaticResources() {
        PosterUrlResolver resolver =
                new PosterUrlResolver(properties(TmdbProperties.Mode.OFFLINE, "https://image.tmdb.org/t/p"));

        assertThat(resolver.url("/abc.svg", "w342")).isEqualTo("/img/posters/abc.svg");
    }

    @Test
    void url_whenOffline_thenIgnoresTheRequestedSize() {
        PosterUrlResolver resolver =
                new PosterUrlResolver(properties(TmdbProperties.Mode.OFFLINE, "https://image.tmdb.org/t/p"));

        assertThat(resolver.url("/abc.svg", "w92")).isEqualTo(resolver.url("/abc.svg", "w1280"));
    }

    @Test
    void url_whenHttpMode_thenBuildsTmdbUrlWithSizeSegment() {
        PosterUrlResolver resolver =
                new PosterUrlResolver(properties(TmdbProperties.Mode.HTTP, "https://image.tmdb.org/t/p"));

        assertThat(resolver.url("/abc.jpg", "w342"))
                .isEqualTo("https://image.tmdb.org/t/p/w342/abc.jpg");
    }

    @Test
    void url_whenBaseUrlHasTrailingSlash_thenDoesNotProduceDoubleSlash() {
        PosterUrlResolver resolver =
                new PosterUrlResolver(properties(TmdbProperties.Mode.HTTP, "https://image.tmdb.org/t/p/"));

        assertThat(resolver.url("/abc.jpg", "w342"))
                .isEqualTo("https://image.tmdb.org/t/p/w342/abc.jpg");
    }

    @Test
    void url_whenPathHasNoLeadingSlash_thenStillBuildsAValidUrl() {
        PosterUrlResolver resolver =
                new PosterUrlResolver(properties(TmdbProperties.Mode.OFFLINE, "https://image.tmdb.org/t/p"));

        assertThat(resolver.url("abc.svg", "w342")).isEqualTo("/img/posters/abc.svg");
    }

    @Test
    void url_whenPathIsMissing_thenReturnsPlaceholderInsteadOfBrokenImage() {
        PosterUrlResolver resolver =
                new PosterUrlResolver(properties(TmdbProperties.Mode.OFFLINE, "https://image.tmdb.org/t/p"));

        assertThat(resolver.url(null, "w342")).isEqualTo("/img/posters/placeholder.svg");
        assertThat(resolver.url("  ", "w342")).isEqualTo("/img/posters/placeholder.svg");
    }
}
