package com.example.moviecatalog.repository;

import com.example.moviecatalog.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface MovieRepository extends JpaRepository<Movie, Long> {

    Optional<Movie> findByTmdbId(Long tmdbId);

    @Query("SELECT m FROM Movie m WHERE LOWER(m.genres) LIKE LOWER(CONCAT('%', :genre, '%'))")
    List<Movie> findByGenre(@Param("genre") String genre);

    @Query("SELECT m FROM Movie m WHERE LOWER(m.director) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(m.castNames) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Movie> findByDirectorOrCast(@Param("query") String query);

    @Query("SELECT m FROM Movie m WHERE LOWER(m.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    List<Movie> findByTitleContaining(@Param("title") String title);

    @Query("SELECT m.tmdbId FROM Movie m WHERE m.id IN (SELECT bl.movieId FROM BlacklistMovie bl)")
    Set<Long> findBlacklistedTmdbIds();
}
