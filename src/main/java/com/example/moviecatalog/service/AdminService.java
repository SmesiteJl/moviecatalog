package com.example.moviecatalog.service;

import com.example.moviecatalog.repository.BlacklistMovieRepository;
import com.example.moviecatalog.repository.ReviewRepository;
import com.example.moviecatalog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final BlacklistMovieRepository blacklistMovieRepository;

    public long getUserCount() {
        return userRepository.count();
    }

    public long getReviewCount() {
        return reviewRepository.count();
    }

    public long getBlacklistCount() {
        return blacklistMovieRepository.count();
    }
}
