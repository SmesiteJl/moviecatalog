package com.example.moviecatalog.repository;

import com.example.moviecatalog.entity.Favorite;
import com.example.moviecatalog.entity.FavoriteId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FavoriteRepository extends JpaRepository<Favorite, FavoriteId> {

    List<Favorite> findByUserId(Long userId);

    boolean existsByUserIdAndMovieId(Long userId, Long movieId);

    void deleteByUserIdAndMovieId(Long userId, Long movieId);
}
