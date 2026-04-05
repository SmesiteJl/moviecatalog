package com.example.moviecatalog.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TmdbCreditsResponse {
    private List<TmdbCrew> crew;
    private List<TmdbCast> cast;
}
