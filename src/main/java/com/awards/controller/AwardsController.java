package com.awards.controller;

import com.awards.dto.AwardIntervalResponse;
import com.awards.service.AwardsService;
import com.awards.service.CsvImportService;
import com.opencsv.exceptions.CsvValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.io.IOException;
import java.util.Objects;

@RestController
@RequestMapping("/awards")
@Tag(name = "Awards", description = "Endpoints para análise de dados dos prêmios")
public class AwardsController {

    private final AwardsService awardsService;
    private final CsvImportService csvImportService;

    public AwardsController(AwardsService awardsService, CsvImportService csvImportService) {
        this.awardsService = awardsService;
        this.csvImportService = csvImportService;
    }

    @Operation(summary = "Obter intervalos de prêmios",
            description = "Calcula e retorna o produtor com o maior intervalo entre dois prêmios consecutivos e o que obteve dois prêmios mais rápido.")
    @ApiResponse(responseCode = "200", description = "Resultado do cálculo dos intervalos.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AwardIntervalResponse.class)))
    @GetMapping("/producers/intervals")
    public ResponseEntity<AwardIntervalResponse> getProducerIntervals() {
        return ResponseEntity.ok(awardsService.getProducersAwardIntervals());
    }

    @Operation(summary = "Upload de novo arquivo de filmes",
            description = "Envia um novo arquivo CSV para ser processado e adicionado à base de dados.")
    @ApiResponse(responseCode = "200", description = "Arquivo processado com sucesso.")
    @ApiResponse(responseCode = "400", description = "Arquivo vazio, com formato inválido ou com dados mal formatados.")
    @ApiResponse(responseCode = "415", description = "Tipo de arquivo não suportado. Apenas text/csv é aceito.")
    @ApiResponse(responseCode = "500", description = "Erro interno no servidor durante o processamento do arquivo.")
    @PostMapping(value = "/upload-movies", consumes = "multipart/form-data")
    public ResponseEntity<String> uploadNewMoviesFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Arquivo não pode ser vazio.");
        }
        if (!Objects.equals(file.getContentType(), "text/csv")) {
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body("Tipo de arquivo inválido. Apenas CSV é permitido.");
        }

        try {
            int importedCount = csvImportService.importMoviesFromCsv(file.getInputStream());
            String message = String.format("Arquivo processado com sucesso. %d filmes foram importados.", importedCount);
            return ResponseEntity.ok(message);
        } catch (NumberFormatException | CsvValidationException e) {
            return ResponseEntity.badRequest().body("Erro de formatação no arquivo CSV: " + e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao ler o arquivo: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ocorreu um erro inesperado ao processar o arquivo: " + e.getMessage());
        }
    }
}