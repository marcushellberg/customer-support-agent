package org.vaadin.marcus.langchain4j;

import java.time.LocalDate;

import jakarta.enterprise.context.ApplicationScoped;

import org.vaadin.marcus.service.BookingDetails;
import org.vaadin.marcus.service.FlightService;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

@ApplicationScoped
public class LangChain4jTools {
    private final FlightService service;

    public LangChain4jTools(FlightService service) {
        this.service = service;
    }

    @Tool("""
            Retrieves information about an existing booking,
            such as the flight date, booking status, departure and arrival airports, and booking class.
            """)
    public BookingDetails getBookingDetails(String bookingNumber, String firstName, String lastName) {
        return service.getBookingDetails(bookingNumber, firstName, lastName);
    }

    @Tool("""
            Modifies an existing booking.
            This includes making changes to the flight date, and the departure and arrival airports.
            """)
    public void changeBooking(
        String bookingNumber,
        String firstName,
        String lastName,
        @P("month of the new flight date") int flightDateMonth,
        @P("day of the month of the new flight date") int flightDateDayOfMonth,
        @P("year of the new flight date") int flightDateYear,
        @P("3-letter code for departure airport") String departureAirport,
        @P("3-letter code for arrival airport") String arrivalAirport
    ) {
        service.changeBooking(bookingNumber, firstName, lastName, LocalDate.of(flightDateYear, flightDateMonth, flightDateDayOfMonth), departureAirport, arrivalAirport);
    }

    @Tool
    public void cancelBooking(String bookingNumber, String firstName, String lastName) {
        service.cancelBooking(bookingNumber, firstName, lastName);
    }
}
