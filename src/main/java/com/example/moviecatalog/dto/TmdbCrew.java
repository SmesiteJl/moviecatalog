package com.example.moviecatalog.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TmdbCrew {
    private String name;
    private String job;
    private String department;
}
