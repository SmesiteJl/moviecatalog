package com.example.moviecatalog.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Everything the catalogue needs to know about its movie source.
 *
 * <p>The application ships with {@link Mode#OFFLINE} as the default so that a fresh clone runs
 * with no API key and no network access to TMDB.
 */
@ConfigurationProperties(prefix = "tmdb")
public record TmdbProperties(
        Mode mode,
        Api api,
        String imageBaseUrl,
        Proxy proxy,
        Timeout timeout
) {

    public enum Mode {
        /** Serve the catalogue bundled with the application. No network, no key. */
        OFFLINE,
        /** Call the real TMDB REST API. */
        HTTP
    }

    public record Api(String key, String baseUrl) {
    }

    public record Proxy(boolean enabled, String host, int port) {
    }

    public record Timeout(int connectMs, int readMs) {
    }

    public boolean offline() {
        return mode == Mode.OFFLINE;
    }
}
