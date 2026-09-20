package com.example.moviecatalog.web;

import com.example.moviecatalog.config.TmdbProperties;
import org.springframework.stereotype.Component;

/**
 * Builds artwork URLs for templates, which is the one place the offline and HTTP modes have to
 * differ visually: offline artwork is served from the application's own static resources.
 *
 * <p>Used from Thymeleaf as {@code ${@posterUrls.url(movie.posterPath, 'w342')}}.
 */
@Component("posterUrls")
public class PosterUrlResolver {

    private static final String OFFLINE_PREFIX = "/img/posters";
    private static final String PLACEHOLDER = "/img/posters/placeholder.svg";

    private final boolean offline;
    private final String imageBaseUrl;

    public PosterUrlResolver(TmdbProperties properties) {
        this.offline = properties.offline();
        this.imageBaseUrl = trimTrailingSlash(properties.imageBaseUrl());
    }

    /**
     * @param path TMDB-style artwork path, e.g. {@code /abc.jpg}; may be {@code null}
     * @param size TMDB size token, e.g. {@code w342}; ignored offline, where one asset serves every size
     * @return a URL the browser can load, never {@code null}
     */
    public String url(String path, String size) {
        if (path == null || path.isBlank()) {
            return PLACEHOLDER;
        }
        String normalised = path.startsWith("/") ? path : "/" + path;
        return offline ? OFFLINE_PREFIX + normalised : imageBaseUrl + "/" + size + normalised;
    }

    private static String trimTrailingSlash(String value) {
        if (value == null) {
            return "";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
