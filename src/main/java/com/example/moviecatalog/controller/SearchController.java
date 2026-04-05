package com.example.moviecatalog.controller;

import com.example.moviecatalog.dto.TmdbMovieResponse;
import com.example.moviecatalog.entity.Movie;
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
public class SearchController {

    private final MovieService movieService;

    @GetMapping("/search")
    public String search(@RequestParam(required = false) String q,
                         @RequestParam(required = false) String genre,
                         @RequestParam(required = false) String person,
                         Model model) {

        Set<Long> blacklisted = movieService.getBlacklistedTmdbIds();
        String searchType = "";

        if (q != null && !q.isBlank()) {
            List<TmdbMovieResponse> results = movieService.searchByTitle(q).stream()
                    .filter(m -> !blacklisted.contains(m.getId()))
                    .toList();
            model.addAttribute("tmdbResults", results);
            model.addAttribute("localResults", List.of());
            model.addAttribute("searchQuery", q);
            searchType = "title";

        } else if (genre != null && !genre.isBlank()) {
            List<Movie> results = movieService.searchLocalByGenre(genre).stream()
                    .filter(m -> !blacklisted.contains(m.getTmdbId()))
                    .toList();
            model.addAttribute("tmdbResults", List.of());
            model.addAttribute("localResults", results);
            model.addAttribute("selectedGenre", genre);
            searchType = "genre";

        } else if (person != null && !person.isBlank()) {
            List<Movie> results = movieService.searchLocalByPerson(person).stream()
                    .filter(m -> !blacklisted.contains(m.getTmdbId()))
                    .toList();
            model.addAttribute("tmdbResults", List.of());
            model.addAttribute("localResults", results);
            model.addAttribute("personQuery", person);
            searchType = "person";

        } else {
            model.addAttribute("tmdbResults", List.of());
            model.addAttribute("localResults", List.of());
        }

        model.addAttribute("searchType", searchType);
        model.addAttribute("genres", MovieService.GENRE_LIST);
        return "search";
    }
}
