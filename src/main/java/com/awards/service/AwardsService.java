package com.awards.service;

import com.awards.dto.AwardIntervalResponse;
import com.awards.dto.ProducerInterval;
import com.awards.entity.Movie;
import com.awards.repository.MovieRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AwardsService {

    private final MovieRepository movieRepository;

    public AwardsService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    public AwardIntervalResponse getProducersAwardIntervals() {
        List<Movie> winners = movieRepository.findByWinnerTrueOrderByYearAsc();
        Map<String, List<Integer>> producerWins = new HashMap<>();

        // 1. Mapeia cada produtor a uma lista de anos em que venceu
        for (Movie winner : winners) {
            String[] producers = winner.getProducers().split(",|\\s+and\\s+");
            for (String producerName : producers) {
                String trimmedName = producerName.trim();
                if (!trimmedName.isEmpty()) {
                    producerWins.computeIfAbsent(trimmedName, k -> new ArrayList<>()).add(winner.getYear());
                }
            }
        }

        List<ProducerInterval> allIntervals = new ArrayList<>();

        // 2. Calcula os intervalos para produtores com mais de um prêmio
        for (Map.Entry<String, List<Integer>> entry : producerWins.entrySet()) {
            String producer = entry.getKey();
            List<Integer> years = entry.getValue().stream().sorted().toList();

            if (years.size() > 1) {
                for (int i = 0; i < years.size() - 1; i++) {
                    int previousWin = years.get(i);
                    int followingWin = years.get(i + 1);
                    int interval = followingWin - previousWin;
                    allIntervals.add(new ProducerInterval(producer, interval, previousWin, followingWin));
                }
            }
        }
        
        if (allIntervals.isEmpty()) {
            return new AwardIntervalResponse(Collections.emptyList(), Collections.emptyList());
        }

        // 3. Encontra o intervalo mínimo e máximo
        int minInterval = allIntervals.stream().min(Comparator.comparingInt(ProducerInterval::getInterval)).get().getInterval();
        int maxInterval = allIntervals.stream().max(Comparator.comparingInt(ProducerInterval::getInterval)).get().getInterval();

        // 4. Filtra para obter todos os produtores com esses intervalos
        List<ProducerInterval> minProducers = allIntervals.stream()
                .filter(p -> p.getInterval() == minInterval)
                .collect(Collectors.toList());

        List<ProducerInterval> maxProducers = allIntervals.stream()
                .filter(p -> p.getInterval() == maxInterval)
                .collect(Collectors.toList());

        return new AwardIntervalResponse(minProducers, maxProducers);
    }
}