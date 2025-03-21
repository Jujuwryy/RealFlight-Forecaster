package com.george.service;

import com.george.config.AviationStackConfig;
import com.george.model.Flight;
import com.george.model.FlightResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class FlightService {

    private final AviationStackConfig config;
    private final WebClient webClient;
    private final KafkaTemplate<String, Flight> kafkaTemplate;
    private final String flightTopic;

    private static final Logger logger = LoggerFactory.getLogger(FlightService.class);

    public FlightService(
            AviationStackConfig config,
            WebClient aviationStackWebClient,
            KafkaTemplate<String, Flight> kafkaTemplate,
            @Value("${kafka.topic.flights}") String flightTopic) {
        this.config = config;
        this.webClient = aviationStackWebClient;
        this.kafkaTemplate = kafkaTemplate;
        this.flightTopic = flightTopic;
        logger.info("FlightService initialized with topic: {}", flightTopic);
    }

    @Cacheable(value = "flights", key = "'all'", unless = "#result.isEmpty()")
    public Mono<List<Flight>> getFlights() {
    	logger.info("Using access_key: {}", config.getKey());
        return webClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/v1/flights")
                .queryParam("access_key", config.getKey())
                .queryParam("limit", 500)
                .build())
            .retrieve()
            .bodyToMono(FlightResponse.class)
            .doOnNext(response -> logger.debug("Raw API response: {}", response))
            .map(FlightResponse::getData)
            .doOnNext(flights -> logger.debug("Mapped flights: {}", flights))
            .doOnNext(this::sendFlightsToKafka)
            .onErrorResume(e -> {
                logger.error("Error fetching flights: {}", e.getMessage(), e);
                return Mono.just(Collections.emptyList());
            });
    }

    private void sendFlightsToKafka(List<Flight> flights) {
        CompletableFuture.runAsync(() -> {
            for (Flight flight : flights) {
                try {
                    String key = (flight.getFlightDate() != null && flight.getDeparture() != null && flight.getDeparture().getIata() != null)
                        ? flight.getFlightDate() + "-" + flight.getDeparture().getIata()
                        : "unknown-" + (flight.getDeparture() != null && flight.getDeparture().getIata() != null ? flight.getDeparture().getIata() : "unknown");
                    kafkaTemplate.send(flightTopic, key, flight);
                    logger.debug("Sent flight to Kafka: key={}", key);
                } catch (Exception e) {
                    logger.error("Error sending flight to Kafka: {}", e.getMessage(), e);
                }
            }
        });
    }

    public Mono<List<Flight>> searchFlights(Map<String, String> queryParams) {
        return webClient.get()
            .uri(uriBuilder -> {
                uriBuilder.path("/v1/flights");
                queryParams.forEach(uriBuilder::queryParam);
                uriBuilder.queryParam("access_key", config.getKey());
                return uriBuilder.build();
            })
            .retrieve()
            .bodyToMono(FlightResponse.class)
            .map(FlightResponse::getData)
            .doOnNext(this::sendFlightsToKafka)
            .onErrorResume(e -> {
                logger.error("Error searching flights: {}", e.getMessage(), e);
                return Mono.just(Collections.emptyList());
            });
    }

    public Flux<List<Flight>> getFlightsStream() {
        return Flux.interval(Duration.ofSeconds(30))
            .flatMap(i -> getFlights())
            .doOnNext(flights -> logger.info("Streamed flights update: {} flights", flights.size()))
            .onErrorContinue((e, obj) -> logger.error("Error in flight stream: {}", e.getMessage(), e));
    }

    public Mono<List<Flight>> getActiveFlights() {
        return searchFlights(Map.of("flight_status", "active"));
    }

    public Mono<List<Flight>> getLandedFlights() {
        return searchFlights(Map.of("flight_status", "landed"));
    }

    @Cacheable(value = "flightsByAirline", key = "#airlineCode")
    public Mono<List<Flight>> getFlightsByAirline(String airlineCode) {
        return searchFlights(Map.of("airline_iata", airlineCode));
    }

    public Mono<List<Flight>> getFlightsByAirport(String airportCode) {
        return searchFlights(Map.of("dep_iata", airportCode));
    }

    public Mono<List<Flight>> getFlightsByFlightNumber(String flightNumber) {
        return searchFlights(Map.of("flight_number", flightNumber));
    }

    public Mono<List<Flight>> getFlightsByStatus(String status) {
        return searchFlights(Map.of("flight_status", status));
    }
}