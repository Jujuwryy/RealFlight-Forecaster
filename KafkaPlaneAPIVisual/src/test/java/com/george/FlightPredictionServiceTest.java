package com.george;

import com.george.model.Flight;
import com.george.model.Flight.Airline;
import com.george.model.Flight.Arrival;
import com.george.model.Flight.Departure;
import com.george.service.FlightPredictionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class FlightPredictionServiceTest {

    @Autowired
    private FlightPredictionService predictionService;

    private List<Flight> trainingData;

    @BeforeEach
    void setUp() {
        // Sample training data with diverse statuses
        // Flight 1: Active status
        Flight flight1 = new Flight();
        flight1.setFlightStatus("active");
        flight1.setAirline(new Airline("AA", "American Airlines"));
        Departure dep1 = new Departure();
        dep1.setIata("JFK");
        dep1.setAirport("JFK Airport");
        flight1.setDeparture(dep1);
        Arrival arr1 = new Arrival();
        arr1.setIata("LAX");
        arr1.setAirport("LAX Airport");
        flight1.setArrival(arr1);

        // Flight 2: Landed status (changed from "active" to "landed")
        Flight flight2 = new Flight();
        flight2.setFlightStatus("landed");
        flight2.setAirline(new Airline("SJ", "Shanghai Airlines"));
        Departure dep2 = new Departure();
        dep2.setIata("IST");
        dep2.setAirport("IST Airport");
        flight2.setDeparture(dep2);
        Arrival arr2 = new Arrival();
        arr2.setIata("DXB");
        arr2.setAirport("DXB Airport");
        flight2.setArrival(arr2);

        trainingData = List.of(flight1, flight2);
    }

    @Test
    void testTrainAndPredict() {
        // Train the model with diverse data
        predictionService.trainStatusPredictor(trainingData);

        // Predict with a flight similar to flight1 (AA, JFK -> LAX)
        Flight testFlight = new Flight();
        testFlight.setAirline(new Airline("AA", "American Airlines"));
        Departure dep = new Departure();
        dep.setIata("JFK");
        testFlight.setDeparture(dep);
        Arrival arr = new Arrival();
        arr.setIata("LAX");
        testFlight.setArrival(arr);

        String prediction = predictionService.predictFlightStatus(testFlight);
        assertEquals("active", prediction, "Should predict 'active' for AA JFK->LAX");

        // Predict with a different flight (DL, ATL -> ORD)
        Flight testFlight2 = new Flight();
        testFlight2.setAirline(new Airline("DL", "Delta"));
        Departure dep2 = new Departure();
        dep2.setIata("ATL");
        testFlight2.setDeparture(dep2);
        Arrival arr2 = new Arrival();
        arr2.setIata("ORD");
        testFlight2.setArrival(arr2);

        String prediction2 = predictionService.predictFlightStatus(testFlight2);
        assertEquals("landed", prediction2, "Should predict 'landed' for DL ATL->ORD based on limited training data");
    }

    @Test
    void testPredictWithoutTraining() {
        // Test prediction without training
        Flight testFlight = new Flight();
        testFlight.setAirline(new Airline("AA", "American Airlines"));
        Departure dep = new Departure();
        dep.setIata("JFK");
        testFlight.setDeparture(dep);
        Arrival arr = new Arrival();
        arr.setIata("LAX");
        testFlight.setArrival(arr);

        String prediction = predictionService.predictFlightStatus(testFlight);
        assertEquals("Unknown (Model not trained)", prediction, "Should return 'Unknown' when model isn’t trained");
    }

    @Test
    void testPredictWithInvalidFlight() {
        // Train the model first
        predictionService.trainStatusPredictor(trainingData);

        // Test with an invalid flight (missing required fields)
        Flight invalidFlight = new Flight();
        String prediction = predictionService.predictFlightStatus(invalidFlight);
        assertEquals("Unknown (Missing or invalid data)", prediction, "Should return 'Unknown' for invalid flight");
    }
}