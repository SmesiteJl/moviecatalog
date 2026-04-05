package com.example.moviecatalog.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "blacklist_movies")
@Data
@NoArgsConstructor
public class BlacklistMovie {

    @Id
    @Column(name = "movie_id")
    private Long movieId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "movie_id", insertable = false, updatable = false)
    private Movie movie;
}
