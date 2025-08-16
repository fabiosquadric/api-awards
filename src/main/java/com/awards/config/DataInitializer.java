package com.awards.config;

import com.awards.entity.Movie;
import com.awards.repository.MovieRepository;
import com.awards.service.CsvImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    private final MovieRepository movieRepository;
    private final CsvImportService csvImportService;

    public DataInitializer(MovieRepository movieRepository, CsvImportService csvImportService) {
        this.movieRepository = movieRepository;
        this.csvImportService = csvImportService;
    }

    @Override
    public void run(String... args) throws Exception {
        if (movieRepository.count() == 0) {
            log.info("Database is empty. Initializing with data from movielist.csv");
            try (InputStream inputStream = new ClassPathResource("movielist.csv").getInputStream()) {
                int count = csvImportService.importMoviesFromCsv(inputStream);
                log.info("Successfully imported {} movies from the initial file.", count);
            } catch (Exception e) {
                log.error("Failed to initialize database from CSV file.", e);
            }
        } else {
            log.info("Database already contains data. Skipping initialization.");
        }
    }
}