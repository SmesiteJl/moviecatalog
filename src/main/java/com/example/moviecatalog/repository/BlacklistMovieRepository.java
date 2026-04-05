package com.example.moviecatalog.repository;

import com.example.moviecatalog.entity.BlacklistMovie;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlacklistMovieRepository extends JpaRepository<BlacklistMovie, Long> {
}
