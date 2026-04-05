package com.example.moviecatalog.controller;

import com.example.moviecatalog.dto.TmdbMovieResponse;
import com.example.moviecatalog.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Set;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final MovieService movieService;

    @GetMapping("/")
    public String index(@RequestParam(required = false) String q, Model model) {
        Set<Long> blacklisted = movieService.getBlacklistedTmdbIds();

        if (q != null && !q.isBlank()) {
            List<TmdbMovieResponse> results = movieService.searchByTitle(q).stream()
                    .filter(m -> !blacklisted.contains(m.getId()))
                    .toList();
            model.addAttribute("movies", results);
            model.addAttribute("searchQuery", q);
            model.addAttribute("isSearch", true);
        } else {
            List<TmdbMovieResponse> popular = movieService.getPopularMovies().stream()
                    .filter(m -> !blacklisted.contains(m.getId()))
                    .toList();
            model.addAttribute("movies", popular);
            model.addAttribute("searchQuery", "");
            model.addAttribute("isSearch", false);
        }

        model.addAttribute("genres", MovieService.GENRE_LIST);
        return "index";
    }
}
