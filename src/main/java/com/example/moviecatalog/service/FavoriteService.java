package com.example.moviecatalog.service;

import com.example.moviecatalog.entity.Favorite;
import com.example.moviecatalog.entity.Movie;
import com.example.moviecatalog.entity.User;
import com.example.moviecatalog.repository.FavoriteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;

    public boolean isFavorite(User user, Movie movie) {
        return favoriteRepository.existsByUserIdAndMovieId(user.getId(), movie.getId());
    }

    @Transactional
    public void toggleFavorite(User user, Movie movie) {
        if (favoriteRepository.existsByUserIdAndMovieId(user.getId(), movie.getId())) {
            favoriteRepository.deleteByUserIdAndMovieId(user.getId(), movie.getId());
        } else {
            Favorite f = new Favorite();
            f.setUserId(user.getId());
            f.setMovieId(movie.getId());
            favoriteRepository.save(f);
        }
    }

    public List<Movie> getFavoriteMovies(User user) {
        return favoriteRepository.findByUserId(user.getId()).stream()
                .map(Favorite::getMovie)
                .collect(Collectors.toList());
    }
}
