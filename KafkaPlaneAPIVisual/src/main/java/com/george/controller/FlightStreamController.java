package com.george.controller;

import com.george.model.Flight;
import com.george.service.FlightService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
public class FlightStreamController {

    private final FlightService flightService;

    public FlightStreamController(FlightService flightService) {
        this.flightService = flightService;
    }

    @GetMapping(value = "/dashboard/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<List<Flight>> streamFlights() {
        return flightService.getFlightsStream();
    }
}