package com.george.service;

import com.george.model.Flight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import smile.classification.RandomForest;
import smile.data.DataFrame;
import smile.data.Tuple;
import smile.data.formula.Formula;
import smile.data.type.DataTypes;
import smile.data.type.StructField;
import smile.data.type.StructType;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class FlightPredictionService {

    private static final Logger logger = LoggerFactory.getLogger(FlightPredictionService.class);

    private RandomForest statusPredictor;
    private final StructType flightSchema;
    private Map<String, Integer> statusToIndex;
    private Map<Integer, String> indexToStatus;
    private Map<String, Integer> airlineToIndex;
    private Map<String, Integer> departureToIndex;
    private Map<String, Integer> arrivalToIndex;

    public FlightPredictionService() {
        this.flightSchema = new StructType(
            new StructField("airline_iata", DataTypes.IntegerType),
            new StructField("departure_iata", DataTypes.IntegerType),
            new StructField("arrival_iata", DataTypes.IntegerType),
            new StructField("flight_status", DataTypes.IntegerType)
        );
        this.statusToIndex = new HashMap<>();
        this.indexToStatus = new HashMap<>();
        this.airlineToIndex = new HashMap<>();
        this.departureToIndex = new HashMap<>();
        this.arrivalToIndex = new HashMap<>();

        // Map flight statuses to indices
        statusToIndex.put("active", 0);
        statusToIndex.put("scheduled", 1);
        statusToIndex.put("cancelled", 2);
        statusToIndex.put("landed", 3);
        indexToStatus.put(0, "active");
        indexToStatus.put(1, "scheduled");
        indexToStatus.put(2, "cancelled");
        indexToStatus.put(3, "landed");

        logger.info("FlightPredictionService instance created: {}", this.hashCode());
    }

    /**
     * Validates if a flight object contains all required data for training or prediction.
     */
    private boolean isValidFlight(Flight flight) {
        return flight != null &&
               flight.getAirline() != null &&
               flight.getDeparture() != null &&
               flight.getArrival() != null &&
               flight.getFlightStatus() != null &&
               statusToIndex.containsKey(flight.getFlightStatus().toLowerCase());
    }

    /**
     * Trains the Random Forest model using the provided flight data.
     *
     * @param flights List of Flight objects for training.
     */
    public void trainStatusPredictor(List<Flight> flights) {
        logger.info("trainStatusPredictor started with {} flights on instance {}", flights.size(), this.hashCode());
        if (flights.isEmpty()) {
            logger.warn("No flights provided for training");
            return;
        }

        logger.debug("Raw flights received: {}", flights);

        // Clear existing mappings
        airlineToIndex.clear();
        departureToIndex.clear();
        arrivalToIndex.clear();

        // Initialize counters for indices
        AtomicInteger nextAirlineId = new AtomicInteger(0);
        AtomicInteger nextDepartureId = new AtomicInteger(0);
        AtomicInteger nextArrivalId = new AtomicInteger(0);

        // Populate mappings for airlines, departures, and arrivals
        for (Flight f : flights) {
            if (f.getAirline() != null && f.getAirline().getIata() != null) {
                airlineToIndex.computeIfAbsent(f.getAirline().getIata(), k ->nextAirlineId.getAndIncrement());
            }
            if (f.getDeparture() != null && f.getDeparture().getIata() != null) {
                departureToIndex.computeIfAbsent(f.getDeparture().getIata(), k -> nextDepartureId.getAndIncrement());
            }
            if (f.getArrival() != null && f.getArrival().getIata() != null) {
                arrivalToIndex.computeIfAbsent(f.getArrival().getIata(), k -> nextArrivalId.getAndIncrement());
            }
        }

        // Add "N/A" as a fallback for missing IATA codes
        airlineToIndex.put("N/A", nextAirlineId.getAndIncrement());
        departureToIndex.put("N/A", nextDepartureId.getAndIncrement());
        arrivalToIndex.put("N/A", nextArrivalId.getAndIncrement());

        // Convert flights to tuples for training
        Stream<Tuple> tupleStream = flights.stream()
            .peek(f -> logger.debug("Filtering flight: {}", f))
            .filter(this::isValidFlight)
            .map(f -> {
                String airlineIata = f.getAirline().getIata() != null ? f.getAirline().getIata() : "N/A";
                String departureIata = f.getDeparture().getIata() != null ? f.getDeparture().getIata() : "N/A";
                String arrivalIata = f.getArrival().getIata() != null ? f.getArrival().getIata() : "N/A";
                Integer statusIndex = statusToIndex.get(f.getFlightStatus().toLowerCase());

                Integer airlineIndex = airlineToIndex.get(airlineIata);
                Integer departureIndex = departureToIndex.get(departureIata);
                Integer arrivalIndex = arrivalToIndex.get(arrivalIata);

                logger.debug("Mapping flight: airline_iata={}, departure_iata={}, arrival_iata={}, status={}",
                    airlineIndex, departureIndex, arrivalIndex, statusIndex);
                return Tuple.of(
                    new Object[]{ airlineIndex, departureIndex, arrivalIndex, statusIndex },
                    flightSchema
                );
            })
            .filter(Objects::nonNull);

        List<Tuple> tuples = tupleStream.collect(Collectors.toList());
        logger.info("Collected {} valid tuples after filtering", tuples.size());

        if (tuples.isEmpty()) {
            logger.warn("No valid tuples after filtering - cannot train");
            return;
        }

        // Prepare training data
        DataFrame trainingData = DataFrame.of(tuples, flightSchema);
        logger.info("Prepared training DataFrame with {} rows: {}", trainingData.size(), trainingData);

        // Log unique flight status classes
        int[] statusArray = trainingData.column("flight_status").toIntArray();
        Set<Integer> uniqueStatuses = Arrays.stream(statusArray)
            .boxed()
            .collect(Collectors.toSet());
        logger.info("Number of unique flight_status classes: {}", uniqueStatuses.size());
        logger.debug("Unique flight_status values: {}", uniqueStatuses);

        // Train the Random Forest model with 100 trees
        Formula formula = Formula.lhs("flight_status");
        Properties props = new Properties();
        props.setProperty("smile.random.forest.trees", "100"); // Set number of trees to 100

        try {
            logger.debug("Training RandomForest with {} rows and {} trees", trainingData.size(), 100);
            statusPredictor = RandomForest.fit(formula, trainingData, props);
            logger.info("Successfully trained RandomForest with {} flights", trainingData.size());
        } catch (Exception e) {
            logger.error("Failed to train RandomForest: {}", e.getMessage(), e);
        }
        logger.info("trainStatusPredictor finished on instance {}", this.hashCode());
    }

    /**
     * Predicts the status of a flight using the trained Random Forest model.
     *
     * @param flight The flight to predict the status for.
     * @return The predicted flight status ("active", "scheduled", "cancelled", or "landed").
     */
    public String predictFlightStatus(Flight flight) {
        logger.debug("predictFlightStatus called for flight: {} on instance {}", flight, this.hashCode());
        if (statusPredictor == null) {
            logger.warn("Model not trained for flight: {}", flight);
            return "Unknown (Model not trained)";
        }
        if (!isValidFlight(flight)) {
            logger.debug("Missing or invalid data for flight: {}", flight);
            return "Unknown (Missing or invalid data)";
        }

        // Prepare features for prediction
        String airlineIata = flight.getAirline().getIata() != null ? flight.getAirline().getIata() : "N/A";
        String departureIata = flight.getDeparture().getIata() != null ? flight.getDeparture().getIata() : "N/A";
        String arrivalIata = flight.getArrival().getIata() != null ? flight.getArrival().getIata() : "N/A";

        Integer airlineIndex = airlineToIndex.getOrDefault(airlineIata, airlineToIndex.get("N/A"));
        Integer departureIndex = departureToIndex.getOrDefault(departureIata, departureToIndex.get("N/A"));
        Integer arrivalIndex = arrivalToIndex.getOrDefault(arrivalIata, arrivalToIndex.get("N/A"));

        Object[] features = new Object[]{ airlineIndex, departureIndex, arrivalIndex };
        StructType inputSchema = new StructType(
            new StructField("airline_iata", DataTypes.IntegerType),
            new StructField("departure_iata", DataTypes.IntegerType),
            new StructField("arrival_iata", DataTypes.IntegerType)
        );
        DataFrame input = DataFrame.of(
            Stream.of(Tuple.of(features, inputSchema)),
            inputSchema
        );

        // Make prediction
        try {
            int predictionIndex = statusPredictor.predict(input)[0];
            String prediction = indexToStatus.getOrDefault(predictionIndex, "Unknown");
            logger.debug("Predicted '{}' for flight: {}", prediction, flight);
            return prediction;
        } catch (Exception e) {
            logger.error("Prediction error for flight {}: {}", flight, e.getMessage(), e);
            return "Unknown (Prediction error)";
        }
    }
}