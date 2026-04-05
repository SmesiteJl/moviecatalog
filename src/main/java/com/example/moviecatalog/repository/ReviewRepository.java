package com.example.moviecatalog.repository;

import com.example.moviecatalog.entity.Movie;
import com.example.moviecatalog.entity.Review;
import com.example.moviecatalog.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByMovieOrderByCreatedAtDesc(Movie movie);

    List<Review> findByUserOrderByCreatedAtDesc(User user);

    Optional<Review> findByMovieAndUser(Movie movie, User user);

    boolean existsByMovieAndUser(Movie movie, User user);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.movie = :movie")
    Double findAverageRatingByMovie(@Param("movie") Movie movie);
}
