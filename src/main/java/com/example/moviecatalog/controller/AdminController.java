package com.example.moviecatalog.controller;

import com.example.moviecatalog.entity.BlacklistMovie;
import com.example.moviecatalog.entity.Movie;
import com.example.moviecatalog.service.AdminService;
import com.example.moviecatalog.service.MovieService;
import com.example.moviecatalog.service.ReviewService;
import com.example.moviecatalog.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final UserService userService;
    private final ReviewService reviewService;
    private final MovieService movieService;

    @GetMapping({"", "/"})
    public String dashboard(Model model) {
        model.addAttribute("userCount", adminService.getUserCount());
        model.addAttribute("reviewCount", adminService.getReviewCount());
        model.addAttribute("blacklistCount", adminService.getBlacklistCount());
        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("users", userService.findAll());
        return "admin/users";
    }

    @PostMapping("/users/{id}/block")
    public String blockUser(@PathVariable Long id) {
        userService.blockUser(id);
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/unblock")
    public String unblockUser(@PathVariable Long id) {
        userService.unblockUser(id);
        return "redirect:/admin/users";
    }

    @GetMapping("/reviews")
    public String reviews(Model model) {
        model.addAttribute("reviews", reviewService.findAll());
        return "admin/reviews";
    }

    @PostMapping("/reviews/{id}/delete")
    public String deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return "redirect:/admin/reviews";
    }

    @GetMapping("/blacklist")
    public String blacklist(Model model) {
        List<BlacklistMovie> blacklist = movieService.getBlacklist();
        model.addAttribute("blacklist", blacklist);
        return "admin/blacklist";
    }

    @PostMapping("/blacklist/add")
    public String addToBlacklist(@RequestParam Long tmdbId, Model model) {
        try {
            Movie movie = movieService.getOrFetchMovie(tmdbId);
            if (movie == null) {
                model.addAttribute("blacklist", movieService.getBlacklist());
                model.addAttribute("errorMessage", "Фильм с ID " + tmdbId + " не найден в TMDB.");
                return "admin/blacklist";
            }
            movieService.addToBlacklist(movie);
        } catch (Exception e) {
            model.addAttribute("blacklist", movieService.getBlacklist());
            model.addAttribute("errorMessage", "Ошибка: " + e.getMessage());
            return "admin/blacklist";
        }
        return "redirect:/admin/blacklist";
    }

    @PostMapping("/blacklist/remove/{movieId}")
    public String removeFromBlacklist(@PathVariable Long movieId) {
        movieService.removeFromBlacklist(movieId);
        return "redirect:/admin/blacklist";
    }
}
