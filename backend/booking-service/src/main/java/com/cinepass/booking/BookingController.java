package com.cinepass.booking;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {
    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping("/seats/{showId}")
    public Map<String, Object> seats(@PathVariable Long showId) {
        return bookingService.seatMap(showId);
    }

    @PostMapping("/reserve")
    public Booking reserve(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader(value = "X-User-Email", required = false) String email,
            @RequestBody Map<String, Object> body
    ) {
        Long showId = Long.valueOf(String.valueOf(body.get("showId")));
        @SuppressWarnings("unchecked")
        List<String> seats = (List<String>) body.get("seats");
        return bookingService.reserve(userId, email, seats, showId);
    }

    @PostMapping
    public Booking create(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader(value = "X-User-Email", required = false) String email,
            @RequestBody Map<String, Object> body
    ) {
        return reserve(userId, email, body);
    }

    @PostMapping("/{id}/pay")
    public Booking pay(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {
        return bookingService.pay(userId, id, body.getOrDefault("method", "UPI"));
    }

    @GetMapping("/mine")
    public List<Booking> mine(@RequestHeader("X-User-Id") Long userId) {
        return bookingService.mine(userId);
    }

    @GetMapping("/my")
    public List<Booking> my(@RequestHeader("X-User-Id") Long userId) {
        return bookingService.mine(userId);
    }

    @GetMapping("/{id}")
    public Booking one(@RequestHeader("X-User-Id") Long userId, @PathVariable Long id) {
        return bookingService.getOwned(userId, id);
    }

    @GetMapping("/verify/{code}")
    public Map<String, Object> verify(@PathVariable String code) {
        Booking booking = bookingService.verify(code);
        boolean valid = "CONFIRMED".equals(booking.getStatus());
        return Map.of(
                "valid", valid,
                "booking", booking
        );
    }
}
