package com.example.moviecatalog.controller;

import com.example.moviecatalog.dto.TmdbMovieListResponse;
import com.example.moviecatalog.dto.TmdbMovieResponse;
import com.example.moviecatalog.entity.Movie;
import com.example.moviecatalog.entity.Review;
import com.example.moviecatalog.entity.User;
import com.example.moviecatalog.service.FavoriteService;
import com.example.moviecatalog.service.MovieService;
import com.example.moviecatalog.service.ReviewService;
import com.example.moviecatalog.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@Controller
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;
    private final ReviewService reviewService;
    private final FavoriteService favoriteService;
    private final UserService userService;

    @GetMapping("/movies")
    public String movies(@RequestParam(defaultValue = "1") int page,
                         @RequestParam(required = false) String q,
                         Model model) {
        Set<Long> blacklisted = movieService.getBlacklistedTmdbIds();

        if (q != null && !q.isBlank()) {
            List<TmdbMovieResponse> results = movieService.searchByTitle(q).stream()
                    .filter(m -> !blacklisted.contains(m.getId()))
                    .toList();
            model.addAttribute("movies", results);
            model.addAttribute("searchQuery", q);
            model.addAttribute("isSearch", true);
            model.addAttribute("currentPage", 1);
            model.addAttribute("totalPages", 1);
        } else {
            TmdbMovieListResponse data = movieService.getPopularMoviesPage(page);
            List<TmdbMovieResponse> movies = data != null && data.getResults() != null
                    ? data.getResults().stream().filter(m -> !blacklisted.contains(m.getId())).toList()
                    : List.of();
            int totalPages = data != null ? Math.min(data.getTotalPages(), 500) : 1;
            model.addAttribute("movies", movies);
            model.addAttribute("searchQuery", "");
            model.addAttribute("isSearch", false);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", totalPages);
        }

        model.addAttribute("genres", MovieService.GENRE_LIST);
        return "movies";
    }

    @GetMapping("/movie/{tmdbId}")
    public String movieDetail(@PathVariable Long tmdbId,
                              @AuthenticationPrincipal UserDetails userDetails,
                              Model model) {
        if (movieService.isBlacklisted(tmdbId)) {
            return "redirect:/movies";
        }

        Movie movie = movieService.getOrFetchMovie(tmdbId);
        if (movie == null) {
            return "redirect:/movies";
        }

        List<Review> reviews = reviewService.getReviewsByMovie(movie);
        Double avgRating = reviewService.getAverageRating(movie);

        model.addAttribute("movie", movie);
        model.addAttribute("reviews", reviews);
        model.addAttribute("avgRating", avgRating != null ? String.format("%.1f", avgRating) : null);

        if (userDetails != null) {
            User user = userService.findByUsername(userDetails.getUsername());
            if (user != null) {
                model.addAttribute("isFavorite", favoriteService.isFavorite(user, movie));
                model.addAttribute("hasReviewed", reviewService.hasReviewed(movie, user));
                model.addAttribute("currentUser", user);
            }
        }

        return "movie-detail";
    }

    @PostMapping("/movie/{tmdbId}/favorite")
    public String toggleFavorite(@PathVariable Long tmdbId,
                                 @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) return "redirect:/login";

        User user = userService.findByUsername(userDetails.getUsername());
        Movie movie = movieService.getOrFetchMovie(tmdbId);
        if (user != null && movie != null) {
            favoriteService.toggleFavorite(user, movie);
        }
        return "redirect:/movie/" + tmdbId;
    }

    @PostMapping("/movie/{tmdbId}/review")
    public String addReview(@PathVariable Long tmdbId,
                            @RequestParam Integer rating,
                            @RequestParam(required = false) String comment,
                            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) return "redirect:/login";

        User user = userService.findByUsername(userDetails.getUsername());
        Movie movie = movieService.getOrFetchMovie(tmdbId);
        if (user != null && movie != null) {
            reviewService.addReview(movie, user, rating, comment != null ? comment : "");
        }
        return "redirect:/movie/" + tmdbId;
    }
}
