package com.awards.integration;

import com.awards.entity.Movie;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import static org.hamcrest.Matchers.is;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // Garante que cada teste rode em sua própria transação, que será revertida ao final
public class MovieControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAllMovies_shouldReturnPagedResults() throws Exception {
        mockMvc.perform(get("/movies?page=0&size=5")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").isNumber())
                .andExpect(jsonPath("$.totalPages").isNumber())
                .andExpect(jsonPath("$.size", is(5)));
    }

    @Test
    void crudOperations_shouldWorkCorrectly() throws Exception {
        // 1. Create
        Movie newMovie = Movie.builder().title("Test Movie").producers("Test Producer").releaseYear(2025).studios("Test Studio").winner(true).build();
        String movieJson = objectMapper.writeValueAsString(newMovie);

        String location = mockMvc.perform(post("/movies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(movieJson))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader("Location");

        // 2. Read
        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Test Movie")));
        
        // 3. Update
        newMovie.setTitle("Updated Test Movie");
        String updatedMovieJson = objectMapper.writeValueAsString(newMovie);
        
        mockMvc.perform(put(location)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updatedMovieJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Updated Test Movie")));

        // 4. Delete
        mockMvc.perform(delete(location))
                .andExpect(status().isNoContent());

        // 5. Verify Deletion
        mockMvc.perform(get(location))
                .andExpect(status().isNotFound());
    }
}