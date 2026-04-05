package com.example.moviecatalog.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "favorites")
@IdClass(FavoriteId.class)
@Data
@NoArgsConstructor
public class Favorite {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Id
    @Column(name = "movie_id")
    private Long movieId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "movie_id", insertable = false, updatable = false)
    private Movie movie;
}
