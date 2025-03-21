package com.george.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import com.george.model.Flight;
import java.util.List;

@Service
public class ConsumerService {
    private static final Logger logger = LoggerFactory.getLogger(ConsumerService.class);
    
    @KafkaListener(id = "test-listener", topics = "${kafka.topic.flights}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeFlights(List<Flight> flights) {
        logger.info("Consumed batch of {} flights", flights.size());
        
        for (Flight flight : flights) {
            String flightKey = flight.getFlightDate() != null ? 
                flight.getFlightDate() + "-" + (flight.getDeparture() != null ? flight.getDeparture().getIata() : "unknown") : 
                "unknown-" + (flight.getDeparture() != null ? flight.getDeparture().getIata() : "unknown");
                
            logger.debug("Processing flight: Key={}, Departure={}, Airline={}", 
                flightKey,
                flight.getDeparture() != null ? flight.getDeparture().getIata() : "N/A",
                flight.getAirline() != null ? flight.getAirline().getName() : "N/A");
        }
    }
}