package com.example.moviecatalog.service;

import com.example.moviecatalog.entity.Movie;
import com.example.moviecatalog.entity.Review;
import com.example.moviecatalog.entity.User;
import com.example.moviecatalog.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;

    public List<Review> getReviewsByMovie(Movie movie) {
        return reviewRepository.findByMovieOrderByCreatedAtDesc(movie);
    }

    public List<Review> getReviewsByUser(User user) {
        return reviewRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public boolean hasReviewed(Movie movie, User user) {
        return reviewRepository.existsByMovieAndUser(movie, user);
    }

    public boolean addReview(Movie movie, User user, int rating, String comment) {
        if (reviewRepository.existsByMovieAndUser(movie, user)) {
            return false;
        }
        Review review = new Review();
        review.setMovie(movie);
        review.setUser(user);
        review.setRating(rating);
        review.setComment(comment);
        reviewRepository.save(review);
        return true;
    }

    public void deleteReview(Long id) {
        reviewRepository.deleteById(id);
    }

    public Double getAverageRating(Movie movie) {
        return reviewRepository.findAverageRatingByMovie(movie);
    }

    public List<Review> findAll() {
        return reviewRepository.findAll();
    }
}
