package com.example.moviecatalog.config;

import com.example.moviecatalog.client.HttpTmdbClient;
import com.example.moviecatalog.client.OfflineTmdbClient;
import com.example.moviecatalog.client.TmdbClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.net.InetSocketAddress;
import java.net.Proxy;

/**
 * Chooses the movie source at startup.
 *
 * <p>The default is the offline catalogue, so {@code docker compose up} is enough to see a working
 * application. Switching to {@code tmdb.mode=http} without a key is treated as a configuration
 * error rather than silently producing empty pages.
 */
@Configuration
@EnableConfigurationProperties(TmdbProperties.class)
@Slf4j
public class TmdbConfig {

    @Bean
    public RestTemplate tmdbRestTemplate(TmdbProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(properties.timeout().connectMs());
        factory.setReadTimeout(properties.timeout().readMs());

        TmdbProperties.Proxy proxy = properties.proxy();
        if (proxy != null && proxy.enabled()) {
            factory.setProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxy.host(), proxy.port())));
            log.info("TMDB requests will be routed through the proxy at {}:{}", proxy.host(), proxy.port());
        }
        return new RestTemplate(factory);
    }

    @Bean
    public TmdbClient tmdbClient(TmdbProperties properties, RestTemplate tmdbRestTemplate, ObjectMapper objectMapper) {
        if (properties.offline()) {
            return new OfflineTmdbClient(objectMapper, "offline/catalogue.json");
        }
        if (properties.api() == null || properties.api().key() == null || properties.api().key().isBlank()) {
            throw new IllegalStateException(
                    "tmdb.mode=http requires a TMDB API key. Set TMDB_API_KEY, or leave tmdb.mode=offline "
                            + "to run against the bundled catalogue.");
        }
        log.info("Using the live TMDB API at {}", properties.api().baseUrl());
        return new HttpTmdbClient(tmdbRestTemplate, properties);
    }
}
