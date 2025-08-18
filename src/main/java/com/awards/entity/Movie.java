package com.awards.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "releaseYear", nullable = false)
    private Integer year;

    @NotBlank
    @Column(name = "title", nullable = false)
    private String title;

    @NotBlank
    @Column(name = "studios", nullable = false)
    private String studios;

    @NotBlank
    @Column(name = "producers", nullable = false, columnDefinition = "TEXT")
    private String producers;

    @Column(name = "winner")
    private Boolean winner;

}