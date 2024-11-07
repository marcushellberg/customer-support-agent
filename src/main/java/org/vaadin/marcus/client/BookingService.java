package org.vaadin.marcus.client;

import java.util.List;

import org.vaadin.marcus.service.BookingDetails;
import org.vaadin.marcus.service.FlightService;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.BrowserCallable;

@BrowserCallable
@AnonymousAllowed
public class BookingService {
    private final FlightService flightService;

    public BookingService(FlightService flightService) {
        this.flightService = flightService;
    }

    public List<BookingDetails> getBookings() {
        return flightService.getBookings();
    }
}
