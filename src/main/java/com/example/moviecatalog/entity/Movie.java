package com.example.moviecatalog.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "movies")
@Data
@NoArgsConstructor
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tmdb_id", unique = true, nullable = false)
    private Long tmdbId;

    @Column(nullable = false)
    private String title;

    @Column(name = "original_title")
    private String originalTitle;

    @Column(columnDefinition = "TEXT")
    private String overview;

    @Column(name = "poster_path")
    private String posterPath;

    @Column(name = "backdrop_path")
    private String backdropPath;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    @Column(name = "tmdb_rating")
    private Double tmdbRating;

    @Column(length = 500)
    private String genres;

    @Column(length = 200)
    private String director;

    @Column(name = "cast_names", length = 1000)
    private String castNames;

    @Column(name = "trailer_key", length = 100)
    private String trailerKey;
}
