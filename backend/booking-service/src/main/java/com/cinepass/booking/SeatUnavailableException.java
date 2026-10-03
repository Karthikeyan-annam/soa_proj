package com.cinepass.booking;

public class SeatUnavailableException extends RuntimeException {
    private final String seat;

    public SeatUnavailableException(String seat) {
        super("Seat no longer available: " + seat);
        this.seat = seat;
    }

    public String getSeat() {
        return seat;
    }
}
