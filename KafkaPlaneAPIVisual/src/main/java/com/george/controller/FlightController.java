package com.george.controller;

import com.george.model.Flight;
import com.george.service.FlightService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/flights")
@Tag(name = "Flight API", description = "Operations for retrieving flight information")
public class FlightController {

    private final FlightService flightService;

    public FlightController(FlightService flightService) {
        this.flightService = flightService;
    }

    @Operation(summary = "Get all flights", description = "Retrieves a list of all available flights")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved flights", 
                    content = @Content(schema = @Schema(implementation = Flight.class))),
        @ApiResponse(responseCode = "404", description = "No flights found")
    })
    @GetMapping
    public Mono<ResponseEntity<List<Flight>>> getAllFlights() {
        return flightService.getFlights()
                .map(flights -> ResponseEntity.ok().body(flights))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Search flights", description = "Search for flights based on various criteria")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved flights", 
                    content = @Content(schema = @Schema(implementation = Flight.class))),
        @ApiResponse(responseCode = "404", description = "No flights found")
    })
    @GetMapping("/search")
    public Mono<ResponseEntity<List<Flight>>> searchFlights(
            @Parameter(description = "Airline IATA code") @RequestParam(required = false) String airline,
            @Parameter(description = "Flight number") @RequestParam(required = false) String flight_number,
            @Parameter(description = "Departure airport IATA code") @RequestParam(required = false) String departure,
            @Parameter(description = "Arrival airport IATA code") @RequestParam(required = false) String arrival,
            @Parameter(description = "Flight status") @RequestParam(required = false) String status) {
        Map<String, String> queryParams = new HashMap<>();
        if (airline != null) queryParams.put("airline_iata", airline);
        if (flight_number != null) queryParams.put("flight_number", flight_number);
        if (departure != null) queryParams.put("dep_iata", departure);
        if (arrival != null) queryParams.put("arr_iata", arrival);
        if (status != null) queryParams.put("flight_status", status);
        return flightService.searchFlights(queryParams)
                .map(flights -> ResponseEntity.ok().body(flights))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get flights by airline", description = "Retrieves flights operated by a specific airline")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved flights", 
                    content = @Content(schema = @Schema(implementation = Flight.class))),
        @ApiResponse(responseCode = "404", description = "No flights found")
    })
    @GetMapping("/airline/{code}")
    public Mono<ResponseEntity<List<Flight>>> getFlightsByAirline(
            @Parameter(description = "Airline IATA code") @PathVariable String code) {
        return flightService.getFlightsByAirline(code)
                .map(flights -> ResponseEntity.ok().body(flights))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get flights by airport", description = "Retrieves flights arriving at or departing from a specific airport")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved flights", 
                    content = @Content(schema = @Schema(implementation = Flight.class))),
        @ApiResponse(responseCode = "404", description = "No flights found")
    })
    @GetMapping("/airport/{code}")
    public Mono<ResponseEntity<List<Flight>>> getFlightsByAirport(
            @Parameter(description = "Airport IATA code") @PathVariable String code) {
        return flightService.getFlightsByAirport(code)
                .map(flights -> ResponseEntity.ok().body(flights))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get flights by flight number", description = "Retrieves flights with a specific flight number")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved flights", 
                    content = @Content(schema = @Schema(implementation = Flight.class))),
        @ApiResponse(responseCode = "404", description = "No flights found")
    })
    @GetMapping("/number/{flightNumber}")
    public Mono<ResponseEntity<List<Flight>>> getFlightsByNumber(
            @Parameter(description = "Flight number") @PathVariable String flightNumber) {
        return flightService.getFlightsByFlightNumber(flightNumber)
                .map(flights -> ResponseEntity.ok().body(flights))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get flights by status", description = "Retrieves flights with a specific status")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved flights", 
                    content = @Content(schema = @Schema(implementation = Flight.class))),
        @ApiResponse(responseCode = "404", description = "No flights found")
    })
    @GetMapping("/status/{status}")
    public Mono<ResponseEntity<List<Flight>>> getFlightsByStatus(
            @Parameter(description = "Flight status (e.g., scheduled, active, landed, cancelled)") 
            @PathVariable String status) {
        return flightService.getFlightsByStatus(status)
                .map(flights -> ResponseEntity.ok().body(flights))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}