package com.awards.controller;

import com.awards.entity.Movie;
import com.awards.service.MovieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/movies")
@Tag(name = "Movies", description = "Endpoints para o gerenciamento completo de filmes (CRUD)")
public class MovieController {

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @Operation(summary = "Listar todos os filmes", description = "Retorna uma lista paginada de todos os filmes na base de dados.")
    @GetMapping
    public ResponseEntity<Page<Movie>> getAllMovies(@RequestParam(defaultValue="0") int pageNumber,
                                                    @RequestParam(defaultValue="20") int size) {

        return ResponseEntity.ok(movieService.findAllMovies(PageRequest.of(pageNumber,size)));
    }

    @Operation(summary = "Buscar filme por ID", description = "Retorna os detalhes de um filme específico pelo seu ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Filme encontrado com sucesso.", content = @Content(schema = @Schema(implementation = Movie.class))),
            @ApiResponse(responseCode = "404", description = "Filme não encontrado para o ID fornecido.", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<Movie> getMovieById(@Parameter(description = "ID do filme a ser buscado", required = true) @PathVariable Long id) {
        return movieService.findMovieById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Criar um novo filme", description = "Adiciona um novo filme à base de dados.")
    @ApiResponse(responseCode = "201", description = "Filme criado com sucesso.")
    @PostMapping
    public ResponseEntity<Movie> createMovie(@RequestBody Movie movie) {
        Movie savedMovie = movieService.saveMovie(movie);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(savedMovie.getId()).toUri();
        return ResponseEntity.created(location).body(savedMovie);
    }

    @Operation(summary = "Atualizar um filme", description = "Atualiza todos os dados de um filme existente a partir do seu ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Filme atualizado com sucesso."),
            @ApiResponse(responseCode = "404", description = "Filme não encontrado para o ID fornecido.", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<Movie> updateMovie(@Parameter(description = "ID do filme a ser atualizado", required = true) @PathVariable Long id, @RequestBody Movie movieDetails) {
        return movieService.findMovieById(id)
                .map(movie -> {
                    movie.setTitle(movieDetails.getTitle());
                    movie.setStudios(movieDetails.getStudios());
                    movie.setProducers(movieDetails.getProducers());
                    movie.setYear(movieDetails.getYear());
                    movie.setWinner(movieDetails.getWinner());
                    return ResponseEntity.ok(movieService.saveMovie(movie));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar um filme", description = "Remove um filme da base de dados pelo seu ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Filme deletado com sucesso."),
            @ApiResponse(responseCode = "404", description = "Filme não encontrado para o ID fornecido.")
    })
    public ResponseEntity<Void> deleteMovie(@Parameter(description = "ID do filme a ser deletado", required = true) @PathVariable Long id) {
        if (movieService.findMovieById(id).isPresent()) {
            movieService.deleteMovie(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}