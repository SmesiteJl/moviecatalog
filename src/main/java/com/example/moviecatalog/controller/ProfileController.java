package com.example.moviecatalog.controller;

import com.example.moviecatalog.entity.Movie;
import com.example.moviecatalog.entity.Review;
import com.example.moviecatalog.entity.User;
import com.example.moviecatalog.service.FavoriteService;
import com.example.moviecatalog.service.ReviewService;
import com.example.moviecatalog.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;
    private final FavoriteService favoriteService;
    private final ReviewService reviewService;

    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername());
        List<Movie> favorites = favoriteService.getFavoriteMovies(user);
        List<Review> reviews = reviewService.getReviewsByUser(user);

        model.addAttribute("user", user);
        model.addAttribute("favorites", favorites);
        model.addAttribute("reviews", reviews);
        return "profile";
    }

    @GetMapping("/favorites")
    public String favorites(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername());
        List<Movie> favorites = favoriteService.getFavoriteMovies(user);
        model.addAttribute("favorites", favorites);
        model.addAttribute("user", user);
        return "favorites";
    }
}
