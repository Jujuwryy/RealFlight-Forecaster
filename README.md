# Flight-Forecaster

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.3-brightgreen) ![Kafka](https://img.shields.io/badge/Kafka-3.6.1-blue) ![Smile ML](https://img.shields.io/badge/Smile%20ML-3.1.1-yellow)

**Flight-Forecaster** is a Spring Boot application that fetches real-time flight data from the AviationStack API, streams it via Apache Kafka, and uses a Random Forest machine learning model to predict flight statuses. It features reactive programming with WebFlux, caching with Caffeine, and centralized logging with AOP. This project is designed for developers and data enthusiasts interested in integrating APIs, message queues, and ML in a modern Java ecosystem.

## Docker Image
[![Docker Hub](https://img.shields.io/docker/v/jujuwry/kafkaflightpredictor/latest?label=Docker%20Hub&style=for-the-badge)](https://hub.docker.com/r/jujuwry/kafkaflightpredictor)

The Docker image (\`jujuwry/kafkaflightpredictor:latest\`) is a pre-built, containerized version of this application, based on OpenJDK 21. It packages the Spring Boot JAR (\`project-kafkaplaneapidocker.jar\`) and exposes port 8080. Configuration is handled externally via an \`.env\` file or environment variables, ensuring no sensitive API keys are embedded. Pull it to quickly deploy the app without local builds.

## Features

### Core Functionality
- **Flight Data Retrieval**: Fetches real-time flight data from the AviationStack API using Spring WebFlux for reactive, non-blocking HTTP requests.
- **Kafka Integration**: Streams flight data to a Kafka topic (`flight-details`) for real-time processing and consumption.
- **Flight Status Prediction**: Trains a Random Forest model (via Smile ML library) to predict flight statuses (`active`, `scheduled`, `cancelled`, `landed`) based on airline, departure, and arrival data.
- **Caching**: Uses Spring’s Caffeine cache to optimize repeated API calls, reducing load on external services.
- **Streaming Updates**: Provides a reactive `Flux` stream of flight updates every 30 seconds.

### Technical Highlights
- **Reactive Programming**: Built with Spring WebFlux for efficient, asynchronous data handling.
- **Machine Learning**: Employs Smile’s Random Forest algorithm for flight status classification.
- **AOP Logging**: Implements Aspect-Oriented Programming for centralized method execution logging.
- **Comprehensive Testing**: Includes integration tests with embedded Kafka, MockServer, and JUnit 5 to validate API, Kafka, and ML functionality.

### Machine Learning
The application leverages the Smile ML library (v3.1.1) to implement a Random Forest classifier for predicting flight statuses. Here’s how it works:

- **Data Preparation**: Flight data from AviationStack (e.g., airline name, departure/arrival times, airport codes) is preprocessed into numerical features (e.g., one-hot encoded airlines, time deltas) and categorical labels (\`active\`, \`scheduled\`, \`cancelled\`, \`landed\`).
- **Model Training**: The \`FlightPredictionService\` trains a Random Forest model using a dataset of historical flight data (either mocked in tests or fetched live). Random Forest is chosen for its robustness to overfitting and ability to handle mixed data types, making it ideal for flight status prediction with potentially noisy API data.
- **Prediction**: Once trained, the model predicts statuses for new flight data streamed via Kafka. Features like departure delay, airline reliability, and route distance are weighted to classify the status.
- **Performance**: The model is lightweight, running in-memory within the Spring Boot app, and benefits from Caffeine caching to avoid redundant predictions for repeated flight queries.
- **Extensibility**: Users can extend the training dataset or tweak hyperparameters (e.g., number of trees, max depth) in \`FlightPredictionService\` to improve accuracy.


### Project Structure
- **`com.george.service.FlightService`**: Handles API calls, Kafka publishing, and caching.
- **`com.george.service.FlightPredictionService`**: Trains and predicts flight statuses using Random Forest.
- **`com.george.consumer.ConsumerService`**: Consumes flight data from Kafka.
- **`com.george.aop.LoggingAspect`**: Logs method execution across services.
- **`com.george.model.Flight`**: Data model for flight information.

## Prerequisites

- **Java 17**: Required for Spring Boot 3.2.3.
- **Maven**: For dependency management and building the project.
- **Docker**: Optional, for containerized deployment.
- **Kafka**: Local or remote Kafka broker (embedded Kafka used in tests).
- **AviationStack API Key**: Sign up at [AviationStack](https://aviationstack.com/) to get your API key.

## Configure Environment Variables
- Create an \`.env\` file in the project root:
\`\`\`bash
- aviationstack.api.key={YOUR_AVIATIONSTACK_API_KEY}
- spring.kafka.bootstrap-servers=localhost:9092
- kafka.topic.flights=flight-details
- spring.kafka.consumer.group-id=flight-consumer-group
- spring.kafka.consumer.properties.spring.json.trusted.packages={YOUR_PACKAGE}

- Replace \`YOUR_AVIATIONSTACK_API_KEY\` with your actual key.
