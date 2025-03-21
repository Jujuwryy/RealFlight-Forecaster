package com.george;

import com.george.config.AviationStackConfig;
import com.george.model.Flight;
import com.george.model.Flight.Airline;
import com.george.model.Flight.Arrival;
import com.george.model.Flight.Departure;
import com.george.model.FlightResponse;
import com.george.service.FlightService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;

@SpringBootTest
@EmbeddedKafka(partitions = 1, brokerProperties = {"listeners=PLAINTEXT://localhost:9093", "port=9093"})
public class FlightServiceIntegrationTest {

    @Autowired
    private FlightService flightService;

    @Autowired
    private KafkaTemplate<String, Flight> kafkaTemplate;

    @MockBean
    private AviationStackConfig config;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private KafkaListenerEndpointRegistry registry;

    private ClientAndServer mockServer;

    @Configuration
    static class TestConfig {
        @Bean
        WebClient webClient() {
            return WebClient.builder()
                .baseUrl("http://localhost:1080") // MockServer port
                .build();
        }
    }

    @BeforeEach
    void setUp() {
        mockServer = startClientAndServer(1080);
        when(config.getKey()).thenReturn("test-key");
        cacheManager.getCache("flights").clear();

        Flight flight = new Flight();
        flight.setFlightDate("2025-03-13");
        flight.setFlightStatus("active");
        flight.setAirline(new Airline("AA", "American Airlines"));

        Departure departure = new Departure();
        departure.setIata("JFK");
        departure.setAirport("JFK Airport");
        flight.setDeparture(departure);

        Arrival arrival = new Arrival();
        arrival.setIata("LAX");
        arrival.setAirport("LAX Airport");
        flight.setArrival(arrival);

        FlightResponse response = new FlightResponse();
        response.setData(List.of(flight));

        mockServer.when(
            HttpRequest.request()
                .withMethod("GET")
                .withPath("/v1/flights")
                .withQueryStringParameter("access_key", "test-key")
                .withQueryStringParameter("limit", "100")
        ).respond(
            HttpResponse.response()
                .withStatusCode(200)
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody("{\"data\": [{\"flight_date\": \"2025-03-13\", \"flight_status\": \"active\", " +
                    "\"airline\": {\"iata\": \"AA\", \"name\": \"American Airlines\"}, " +
                    "\"departure\": {\"iata\": \"JFK\", \"airport\": \"JFK Airport\"}, " +
                    "\"arrival\": {\"iata\": \"LAX\", \"airport\": \"LAX Airport\"}}]}")
        );
    }

    @AfterEach
    void tearDown() {
        mockServer.stop();
    }

    @Test
    void testGetFlights_success() {
        Mono<List<Flight>> flightsMono = flightService.getFlights();

        StepVerifier.create(flightsMono)
            .assertNext(flights -> {
                assertEquals(1, flights.size(), "Should return one flight");
                Flight flight = flights.get(0);
                assertEquals("2025-03-13", flight.getFlightDate(), "Flight date should match");
                assertEquals("active", flight.getFlightStatus(), "Flight status should be active");
                assertEquals("AA", flight.getAirline().getIata(), "Airline IATA should be AA");
                assertEquals("JFK", flight.getDeparture().getIata(), "Departure IATA should be JFK");
                assertEquals("LAX", flight.getArrival().getIata(), "Arrival IATA should be LAX");
            })
            .verifyComplete();

        List<Flight> cachedFlights = cacheManager.getCache("flights").get("all", List.class);
        assertEquals(1, cachedFlights.size(), "Cache should contain one flight");
    }

    @Test
    void testGetFlights_error() {
        mockServer.clear(HttpRequest.request().withPath("/v1/flights"));
        mockServer.when(
            HttpRequest.request().withPath("/v1/flights")
        ).respond(
            HttpResponse.response().withStatusCode(500)
        );

        Mono<List<Flight>> flightsMono = flightService.getFlights();

        StepVerifier.create(flightsMono)
            .assertNext(flights -> assertEquals(0, flights.size(), "Should return empty list on error"))
            .verifyComplete();
    }

    @Test
    void testKafkaIntegration() throws InterruptedException {
        flightService.getFlights().block();
        Thread.sleep(1000); // Simplified check for Kafka send
    }

    @Test
    void testKafkaIntegration_withConsumer() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        List<Flight> receivedFlights = new ArrayList<>();

        MessageListenerContainer listenerContainer = registry.getListenerContainer("test-listener");
        if (listenerContainer == null) {
            throw new IllegalStateException("No listener with ID 'test-listener' found. Ensure ConsumerService is configured with @KafkaListener(id = \"test-listener\").");
        }

        // Explicitly cast lambda to MessageListener<String, Flight>
        listenerContainer.setupMessageListener((MessageListener<String, Flight>) record -> {
            receivedFlights.add(record.value());
            latch.countDown();
        });
        listenerContainer.start();

        flightService.getFlights().block();

        boolean received = latch.await(5, TimeUnit.SECONDS);
        listenerContainer.stop();

        assertTrue(received, "Message should be received within 5 seconds");
        assertEquals(1, receivedFlights.size(), "Should receive one flight");
        Flight flight = receivedFlights.get(0);
        assertEquals("2025-03-13", flight.getFlightDate(), "Flight date should match");
        assertEquals("active", flight.getFlightStatus(), "Flight status should be active");
        assertEquals("AA", flight.getAirline().getIata(), "Airline IATA should be AA");
        assertEquals("JFK", flight.getDeparture().getIata(), "Departure IATA should be JFK");
        assertEquals("LAX", flight.getArrival().getIata(), "Arrival IATA should be LAX");
    }
}