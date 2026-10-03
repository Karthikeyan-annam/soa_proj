package com.cinepass.booking;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class BookingExceptionHandler {
    @ExceptionHandler(SeatUnavailableException.class)
    public ResponseEntity<Map<String, Object>> conflict(SeatUnavailableException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "error", "SEAT_UNAVAILABLE",
                "seat", ex.getSeat(),
                "message", "This seat was just booked by another customer."
        ));
    }
}
