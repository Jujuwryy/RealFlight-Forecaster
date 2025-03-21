package com.george.controller;

import com.george.model.Flight;
import com.george.service.FlightPredictionService;
import com.george.service.FlightService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class VisualizationController {

    private static final Logger logger = LoggerFactory.getLogger(VisualizationController.class);

    @Autowired
    private FlightService flightService;

    @Autowired
    private FlightPredictionService predictionService;

    @PostConstruct
    public void init() {
        logger.info("Initializing VisualizationController, training model with instance {}", predictionService.hashCode());
        flightService.getFlights()
            .doOnNext(flights -> logger.info("Fetched {} flights for initial training", flights.size()))
            .subscribe(flights -> {
                List<Flight> trainingFlights = flights.stream()
                    .distinct()
                    .limit(100)
                    .collect(Collectors.toList());
                logger.info("Training with {} flights", trainingFlights.size());
                predictionService.trainStatusPredictor(trainingFlights);
            }, error -> logger.error("Error during initial training: {}", error.getMessage(), error));
    }

    @GetMapping("/dashboard")
    public Mono<String> dashboard(Model model) {
        logger.info("Dashboard endpoint called");
        return getFlightData()
            .doOnNext(data -> {
                logger.debug("Adding attributes to model: flights.size={}", ((List<?>) data.get("flights")).size());
                model.addAllAttributes(data);
            })
            .then(Mono.just("flights-dashboard"));
       
    }

    private Mono<Map<String, Object>> getFlightData() {
        logger.info("Entering getFlightData");
        return flightService.getFlights()
            .doOnNext(flights -> logger.info("Fetched {} flights from FlightService", flights.size()))
            .doOnNext(flights -> logger.debug("Raw flights: {}", flights))
            .map(flights -> {
                logger.info("Processing {} flights in map", flights.size());
                List<Flight> uniqueFlights = flights.stream()
                    .distinct()
                    .limit(100)
                    .collect(Collectors.toList());
                logger.info("After unique filter: {} flights", uniqueFlights.size());

                List<Flight> flightsWithPredictions = uniqueFlights.stream()
                    .map(flight -> {
                        String predictedStatus = predictionService.predictFlightStatus(flight);
                        flight.setPredictedStatus(predictedStatus);
                        return flight;
                    })
                    .collect(Collectors.toList());
                logger.info("Processed {} flights with predictions", flightsWithPredictions.size());

                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put("flights", flightsWithPredictions);
                resultMap.put("scheduledCount", flightsWithPredictions.stream()
                    .filter(f -> "scheduled".equalsIgnoreCase(f.getFlightStatus())).count());
                resultMap.put("activeCount", flightsWithPredictions.stream()
                    .filter(f -> "active".equalsIgnoreCase(f.getFlightStatus())).count());
                resultMap.put("landedCount", flightsWithPredictions.stream()
                    .filter(f -> "landed".equalsIgnoreCase(f.getFlightStatus())).count());
                resultMap.put("cancelledCount", flightsWithPredictions.stream()
                    .filter(f -> "cancelled".equalsIgnoreCase(f.getFlightStatus())).count());

                return resultMap;
            })
            .doOnError(e -> logger.error("Error in getFlightData: {}", e.getMessage(), e))
            .onErrorResume(e -> {
                logger.error("Recovering from error in getFlightData: {}", e.getMessage());
                Map<String, Object> emptyResult = new HashMap<>();
                emptyResult.put("flights", Collections.emptyList());
                emptyResult.put("scheduledCount", 0L);
                emptyResult.put("activeCount", 0L);
                emptyResult.put("landedCount", 0L);
                emptyResult.put("cancelledCount", 0L);
                return Mono.just(emptyResult);
            });
    }
}