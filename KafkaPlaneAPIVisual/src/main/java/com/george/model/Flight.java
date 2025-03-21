package com.george.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Flight {
    @JsonProperty("flight_date")
    private String flightDate;

    @JsonProperty("flight_status")
    private String flightStatus;

    private Departure departure;
    private Arrival arrival;
    private Airline airline;
    private Aircraft aircraft;
    private Live live;
    
    private String predictedStatus; // AI-predicted status
    
    

    public static class Departure {
        private String airport;
        private String timezone;
        private String iata;
        private String icao;
        private String terminal;
        private String gate;
        private String delay;
        private String scheduled;
        private String estimated;
        private String actual;
        @JsonProperty("estimated_runway")
        private String estimatedRunway;
        @JsonProperty("actual_runway")
        private String actualRunway;

        public String getAirport() { return airport; }
        public void setAirport(String airport) { this.airport = airport; }
        public String getTimezone() { return timezone; }
        public void setTimezone(String timezone) { this.timezone = timezone; }
        public String getIata() { return iata; }
        public void setIata(String iata) { this.iata = iata; }
        public String getIcao() { return icao; }
        public void setIcao(String icao) { this.icao = icao; }
        public String getTerminal() { return terminal; }
        public void setTerminal(String terminal) { this.terminal = terminal; }
        public String getGate() { return gate; }
        public void setGate(String gate) { this.gate = gate; }
        public String getDelay() { return delay; }
        public void setDelay(String delay) { this.delay = delay; }
        public String getScheduled() { return scheduled; }
        public void setScheduled(String scheduled) { this.scheduled = scheduled; }
        public String getEstimated() { return estimated; }
        public void setEstimated(String estimated) { this.estimated = estimated; }
        public String getActual() { return actual; }
        public void setActual(String actual) { this.actual = actual; }
        public String getEstimatedRunway() { return estimatedRunway; }
        public void setEstimatedRunway(String estimatedRunway) { this.estimatedRunway = estimatedRunway; }
        public String getActualRunway() { return actualRunway; }
        public void setActualRunway(String actualRunway) { this.actualRunway = actualRunway; }

        @Override
        public String toString() {
            return "Departure{iata='" + iata + "', airport='" + airport + "'}";
        }
    }

    public static class Arrival {
        private String airport;
        private String timezone;
        private String iata;
        private String icao;
        private String terminal;
        private String gate;
        private String baggage;
        private String delay;
        private String scheduled;
        private String estimated;
        private String actual;
        @JsonProperty("estimated_runway")
        private String estimatedRunway;
        @JsonProperty("actual_runway")
        private String actualRunway;

        public String getAirport() { return airport; }
        public void setAirport(String airport) { this.airport = airport; }
        public String getTimezone() { return timezone; }
        public void setTimezone(String timezone) { this.timezone = timezone; }
        public String getIata() { return iata; }
        public void setIata(String iata) { this.iata = iata; }
        public String getIcao() { return icao; }
        public void setIcao(String icao) { this.icao = icao; }
        public String getTerminal() { return terminal; }
        public void setTerminal(String terminal) { this.terminal = terminal; }
        public String getGate() { return gate; }
        public void setGate(String gate) { this.gate = gate; }
        public String getBaggage() { return baggage; }
        public void setBaggage(String baggage) { this.baggage = baggage; }
        public String getDelay() { return delay; }
        public void setDelay(String delay) { this.delay = delay; }
        public String getScheduled() { return scheduled; }
        public void setScheduled(String scheduled) { this.scheduled = scheduled; }
        public String getEstimated() { return estimated; }
        public void setEstimated(String estimated) { this.estimated = estimated; }
        public String getActual() { return actual; }
        public void setActual(String actual) { this.actual = actual; }
        public String getEstimatedRunway() { return estimatedRunway; }
        public void setEstimatedRunway(String estimatedRunway) { this.estimatedRunway = estimatedRunway; }
        public String getActualRunway() { return actualRunway; }
        public void setActualRunway(String actualRunway) { this.actualRunway = actualRunway; }

        @Override
        public String toString() {
            return "Arrival{iata='" + iata + "', airport='" + airport + "'}";
        }
    }

    public static class Airline {
        private String name;
        private String iata;
        private String icao;

        public Airline(String string, String string2) {
			
		}
		public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getIata() { return iata; }
        public void setIata(String iata) { this.iata = iata; }
        public String getIcao() { return icao; }
        public void setIcao(String icao) { this.icao = icao; }

        @Override
        public String toString() {
            return "Airline{name='" + name + "', iata='" + iata + "'}";
        }
    }

    public static class Aircraft {
        private String registration;
        private String iata;
        private String icao;
        private String icao24;

        public String getRegistration() { return registration; }
        public void setRegistration(String registration) { this.registration = registration; }
        public String getIata() { return iata; }
        public void setIata(String iata) { this.iata = iata; }
        public String getIcao() { return icao; }
        public void setIcao(String icao) { this.icao = icao; }
        public String getIcao24() { return icao24; }
        public void setIcao24(String icao24) { this.icao24 = icao24; }

        @Override
        public String toString() {
            return "Aircraft{registration='" + registration + "'}";
        }
    }

    public static class Live {
        private String updated;
        private double latitude;
        private double longitude;
        private double altitude;
        private double direction;
        @JsonProperty("speed_horizontal")
        private double speedHorizontal;
        @JsonProperty("speed_vertical")
        private double speedVertical;
        @JsonProperty("is_ground")
        private boolean isGround;

        public String getUpdated() { return updated; }
        public void setUpdated(String updated) { this.updated = updated; }
        public double getLatitude() { return latitude; }
        public void setLatitude(double latitude) { this.latitude = latitude; }
        public double getLongitude() { return longitude; }
        public void setLongitude(double longitude) { this.longitude = longitude; }
        public double getAltitude() { return altitude; }
        public void setAltitude(double altitude) { this.altitude = altitude; }
        public double getDirection() { return direction; }
        public void setDirection(double direction) { this.direction = direction; }
        public double getSpeedHorizontal() { return speedHorizontal; }
        public void setSpeedHorizontal(double speedHorizontal) { this.speedHorizontal = speedHorizontal; }
        public double getSpeedVertical() { return speedVertical; }
        public void setSpeedVertical(double speedVertical) { this.speedVertical = speedVertical; }
        public boolean isGround() { return isGround; }
        public void setGround(boolean ground) { isGround = ground; }

        @Override
        public String toString() {
            return "Live{updated='" + updated + "'}";
        }
    }

    public String getFlightDate() { return flightDate; }
    public void setFlightDate(String flightDate) { this.flightDate = flightDate; }
    public String getFlightStatus() { return flightStatus; }
    public void setFlightStatus(String flightStatus) { this.flightStatus = flightStatus; }
    public Departure getDeparture() { return departure; }
    public void setDeparture(Departure departure) { this.departure = departure; }
    public Arrival getArrival() { return arrival; }
    public void setArrival(Arrival arrival) { this.arrival = arrival; }
    public Airline getAirline() { return airline; }
    public void setAirline(Airline airline) { this.airline = airline; }
    public Aircraft getAircraft() { return aircraft; }
    public void setAircraft(Aircraft aircraft) { this.aircraft = aircraft; }
    public Live getLive() { return live; }
    public void setLive(Live live) { this.live = live; }

    @Override
    public String toString() {
        return "Flight{" +
                "flightDate='" + flightDate + '\'' +
                ", flightStatus='" + flightStatus + '\'' +
                ", departure=" + (departure != null ? departure : "null") +
                ", arrival=" + (arrival != null ? arrival : "null") +
                ", airline=" + (airline != null ? airline : "null") +
                '}';
    }
	public String getPredictedStatus() {
		return predictedStatus;
	}
	public void setPredictedStatus(String predictedStatus) {
		this.predictedStatus = predictedStatus;
	}
}