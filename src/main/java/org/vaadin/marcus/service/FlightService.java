package org.vaadin.marcus.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

import org.vaadin.marcus.data.Booking;
import org.vaadin.marcus.data.BookingClass;
import org.vaadin.marcus.data.BookingData;
import org.vaadin.marcus.data.BookingStatus;
import org.vaadin.marcus.data.Customer;
import org.vaadin.marcus.service.Exceptions.BookingCannotBeCancelledException;
import org.vaadin.marcus.service.Exceptions.BookingCannotBeChangedException;
import org.vaadin.marcus.service.Exceptions.BookingNotFoundException;

import io.quarkus.logging.Log;

@ApplicationScoped
public class FlightService {
    private final BookingData db = new BookingData();

    @PostConstruct
    void initDemoData() {
        List<String> firstNames = List.of("John", "Jane", "Michael", "Sarah", "Robert");
        List<String> lastNames = List.of("Doe", "Smith", "Johnson", "Williams", "Taylor");
        List<String> airportCodes = List.of("LAX", "SFO", "JFK", "LHR", "CDG", "ARN", "HEL", "TXL", "MUC", "FRA", "MAD", "SJC");
        Random random = new Random();

        var customers = new ArrayList<Customer>();
        var bookings = new ArrayList<Booking>();

        IntStream.range(0, 5).forEach(i -> {
            String firstName = firstNames.get(i);
            String lastName = lastNames.get(i);
            String from = airportCodes.get(random.nextInt(airportCodes.size()));
            String to = airportCodes.get(random.nextInt(airportCodes.size()));
            BookingClass bookingClass = BookingClass.values()[random.nextInt(BookingClass.values().length)];
            Customer customer = new Customer(firstName, lastName);

            LocalDate date = LocalDate.now().plusDays(2*i);

            Booking booking = new Booking("10" + (i + 1), date, customer, BookingStatus.CONFIRMED, from, to, bookingClass);
            customer.getBookings().add(booking);

            customers.add(customer);
            bookings.add(booking);
        });

        // Reset the database on each start
        db.setCustomers(customers);
        db.setBookings(bookings);

        Log.info("Demo data initialized");
    }

    public List<BookingDetails> getBookings() {
        return db.getBookings().stream().map(this::toBookingDetails).toList();
    }

    private Booking findBooking(String bookingNumber, String firstName, String lastName) {
        return db.getBookings().stream()
                .filter(b -> b.getBookingNumber().equalsIgnoreCase(bookingNumber))
                .filter(b -> b.getCustomer().getFirstName().equalsIgnoreCase(firstName))
                .filter(b -> b.getCustomer().getLastName().equalsIgnoreCase(lastName))
                .findFirst()
                .orElseThrow(() -> new BookingNotFoundException(bookingNumber));
    }

    public BookingDetails getBookingDetails(String bookingNumber, String firstName, String lastName) {
        var booking = findBooking(bookingNumber, firstName, lastName);
        return toBookingDetails(booking);
    }

    public void changeBooking(String bookingNumber, String firstName, String lastName,
                              LocalDate newFlightDate, String newDepartureAirport, String newArrivalAirport) {
        var booking = findBooking(bookingNumber, firstName, lastName);
        if (booking.getDate().isBefore(LocalDate.now().plusDays(1))) {
            throw new BookingCannotBeChangedException(bookingNumber);
        }
        booking.setDate(newFlightDate);
        booking.setFrom(newDepartureAirport);
        booking.setTo(newArrivalAirport);
    }

    public void cancelBooking(String bookingNumber, String firstName, String lastName) {
        var booking = findBooking(bookingNumber, firstName, lastName);
        if (booking.getDate().isBefore(LocalDate.now().plusDays(2))) {
            throw new BookingCannotBeCancelledException(bookingNumber);
        }
        booking.setBookingStatus(BookingStatus.CANCELLED);
    }

    private BookingDetails toBookingDetails(Booking booking){
        return new BookingDetails(
                booking.getBookingNumber(),
                booking.getCustomer().getFirstName(),
                booking.getCustomer().getLastName(),
                booking.getDate(),
                booking.getBookingStatus(),
                booking.getFrom(),
                booking.getTo(),
                booking.getBookingClass().toString()
        );
    }
}
