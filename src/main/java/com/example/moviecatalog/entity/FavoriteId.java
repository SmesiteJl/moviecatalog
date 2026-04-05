package com.example.moviecatalog.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteId implements Serializable {
    private Long userId;
    private Long movieId;
}
