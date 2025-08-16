package com.awards.service;

import com.awards.entity.Movie;
import com.awards.repository.MovieRepository;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
public class CsvImportService {

    private static final Logger log = LoggerFactory.getLogger(CsvImportService.class);
    private final MovieRepository movieRepository;

    public CsvImportService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    /**
     * Processa um arquivo CSV a partir de um InputStream, parseia os dados
     * e os persiste no banco de dados de forma transacional e paralela.
     *
     * @param inputStream O fluxo de dados do arquivo CSV.
     * @return O número de registros de filmes importados.
     * @throws IOException Se ocorrer um erro de I/O.
     * @throws CsvValidationException Se o CSV for inválido.
     * @throws InterruptedException Se a thread for interrompida.
     */
    @Transactional(rollbackFor = Exception.class)
    public int importMoviesFromCsv(InputStream inputStream) throws IOException, CsvValidationException, InterruptedException {
        long startTime = System.currentTimeMillis();
        List<Movie> moviesToSave = new ArrayList<>();

        try (CSVReader reader = new CSVReader(new InputStreamReader(inputStream))) {
            reader.readNext(); // Pula o cabeçalho

            String[] line;
            while ((line = reader.readNext()) != null) {
                String[] values = line[0].split(";", -1); // -1 para incluir colunas vazias no final

                if (values.length >= 4) {
                    Movie movie = Movie.builder()
                            .releaseYear(Integer.parseInt(values[0].trim()))
                            .title(values[1].trim())
                            .studios(values[2].trim())
                            .producers(values[3].trim())
                            .winner(values.length > 4 && "yes".equalsIgnoreCase(values[4].trim()))
                            .build();
                    moviesToSave.add(movie);
                }
            }
        }

        // Usar saveAll é mais performático para inserções em lote do que saves individuais em um loop.
        // A paralelização aqui é mais útil para processamentos pesados por linha (ex: chamadas externas),
        // para simples inserções, o batch do JPA já é muito eficiente.
        // Mantendo a paralelização para seguir o requisito original.
        int numThreads = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        for (final Movie movie : moviesToSave) {
            executor.submit(() -> movieRepository.save(movie));
        }

        executor.shutdown();
        if (!executor.awaitTermination(5, TimeUnit.MINUTES)) {
            log.error("CSV data processing timed out.");
            executor.shutdownNow();
        }

        long endTime = System.currentTimeMillis();
        log.info("Finished processing {} records from CSV in {} ms", moviesToSave.size(), (endTime - startTime));
        return moviesToSave.size();
    }
}