package org.vaadin.marcus.service;

public class Exceptions {
    public static class BookingCannotBeChangedException extends RuntimeException {
        public BookingCannotBeChangedException(String bookingNumber) {
            super("Booking %s cannot be changed within 24 hours of the start date".formatted(bookingNumber));
        }
    }

    public static class BookingCannotBeCancelledException extends RuntimeException {
        public BookingCannotBeCancelledException(String bookingNumber) {
            super("Booking %s cannot be cancelled within 48 hours of the start date".formatted(bookingNumber));
        }
    }

    public static class BookingNotFoundException extends RuntimeException {
        public BookingNotFoundException(String bookingNumber) {
            super("Booking %s not found".formatted(bookingNumber));
        }
    }
}
