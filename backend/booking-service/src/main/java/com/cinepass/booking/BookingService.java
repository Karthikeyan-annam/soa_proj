package com.cinepass.booking;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
public class BookingService {
    public static final int CONVENIENCE_FEE = 30;
    private static final int HOLD_MINUTES = 8;

    private final SeatLockRepository seats;
    private final BookingRepository bookings;
    private final CatalogClient catalog;

    public BookingService(SeatLockRepository seats, BookingRepository bookings, CatalogClient catalog) {
        this.seats = seats;
        this.bookings = bookings;
        this.catalog = catalog;
    }

    @Transactional
    public Map<String, Object> seatMap(Long showId) {
        ShowInfo show = catalog.fetchShow(showId);
        expireHolds(showId);
        Map<String, String> occupancy = seats.findByShowId(showId).stream()
                .collect(Collectors.toMap(SeatLock::getSeatNumber, SeatLock::getStatus, (a, b) -> a));
        List<String> rows = Arrays.asList(show.getRowsLayout().split(","));
        List<Map<String, Object>> grid = new ArrayList<>();
        for (String row : rows) {
            List<Map<String, Object>> rowSeats = new ArrayList<>();
            for (int i = 1; i <= show.getSeatsPerRow(); i++) {
                String number = row + i;
                Map<String, Object> seat = new LinkedHashMap<>();
                seat.put("id", number);
                seat.put("status", occupancy.getOrDefault(number, "AVAILABLE"));
                rowSeats.add(seat);
            }
            grid.add(Map.of("row", row, "seats", rowSeats));
        }
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("show", show);
        payload.put("grid", grid);
        payload.put("price", show.getPrice());
        return payload;
    }

    @Transactional
    public Booking reserve(Long userId, String email, List<String> requestedSeats, Long showId) {
        if (requestedSeats == null || requestedSeats.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Select at least one seat");
        }
        List<String> uniqueSeats = requestedSeats.stream().distinct().sorted().toList();
        ShowInfo show = catalog.fetchShow(showId);
        expireHolds(showId);

        List<SeatLock> locked = seats.lockSeats(showId, uniqueSeats);
        Map<String, SeatLock> byNumber = locked.stream()
                .collect(Collectors.toMap(SeatLock::getSeatNumber, s -> s));

        for (String seatNumber : uniqueSeats) {
            SeatLock existing = byNumber.get(seatNumber);
            if (existing != null && !"AVAILABLE".equals(existing.getStatus())) {
                throw new SeatUnavailableException(seatNumber);
            }
        }

        Booking booking = new Booking();
        booking.setBookingCode(nextCode());
        booking.setUserId(userId);
        booking.setEmail(email);
        booking.setShowId(showId);
        booking.setMovieId(show.getMovieId());
        booking.setMovieTitle(catalog.fetchMovieTitle(show.getMovieId()));
        booking.setCinema(show.getCinema());
        booking.setScreen(show.getScreen());
        booking.setShowDate(show.getShowDate());
        booking.setShowTime(formatTime(show.getShowTime()));
        booking.setSeats(String.join(", ", uniqueSeats));
        booking.setTicketCount(uniqueSeats.size());
        booking.setTicketPrice(show.getPrice() * uniqueSeats.size());
        booking.setConvenienceFee(CONVENIENCE_FEE);
        booking.setTotal(booking.getTicketPrice() + CONVENIENCE_FEE);
        booking.setStatus("HELD");
        booking.setCreatedAt(Instant.now());
        booking.setExpiresAt(Instant.now().plusSeconds(HOLD_MINUTES * 60L));
        bookings.save(booking);

        try {
            for (String seatNumber : uniqueSeats) {
                SeatLock lock = byNumber.get(seatNumber);
                if (lock == null) {
                    lock = new SeatLock();
                    lock.setShowId(showId);
                    lock.setSeatNumber(seatNumber);
                }
                lock.setStatus("HELD");
                lock.setBookingId(booking.getId());
                seats.save(lock);
            }
            seats.flush();
        } catch (RuntimeException ex) {
            Throwable cursor = ex;
            while (cursor != null) {
                if (cursor instanceof DataIntegrityViolationException
                        || cursor.getClass().getName().contains("ConstraintViolation")) {
                    throw new SeatUnavailableException(uniqueSeats.get(0));
                }
                cursor = cursor.getCause();
            }
            throw ex;
        }
        return booking;
    }

    @Transactional
    public Booking pay(Long userId, Long bookingId, String method) {
        Booking booking = bookings.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));
        if (!booking.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your booking");
        }
        if ("CONFIRMED".equals(booking.getStatus())) {
            return booking;
        }
        if (!"HELD".equals(booking.getStatus()) || booking.getExpiresAt().isBefore(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.GONE, "Hold expired. Please choose seats again.");
        }
        List<SeatLock> locks = seats.lockSeats(booking.getShowId(), Arrays.asList(booking.getSeats().split(", ")));
        for (SeatLock lock : locks) {
            if (!booking.getId().equals(lock.getBookingId())) {
                throw new SeatUnavailableException(lock.getSeatNumber());
            }
            lock.setStatus("BOOKED");
        }
        seats.saveAll(locks);
        booking.setPaymentMethod(method);
        booking.setStatus("CONFIRMED");
        return bookings.save(booking);
    }

    public List<Booking> mine(Long userId) {
        return bookings.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Booking verify(String code) {
        return bookings.findByBookingCode(code)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found"));
    }

    public Booking getOwned(Long userId, Long id) {
        Booking booking = bookings.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));
        if (!booking.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your booking");
        }
        return booking;
    }

    @Transactional
    public void seedOccupied(Long showId, List<String> occupied) {
        for (String seat : occupied) {
            if (seats.findByShowIdAndSeatNumber(showId, seat).isPresent()) {
                continue;
            }
            SeatLock lock = new SeatLock();
            lock.setShowId(showId);
            lock.setSeatNumber(seat);
            lock.setStatus("BOOKED");
            seats.save(lock);
        }
    }

    private void expireHolds(Long showId) {
        Instant now = Instant.now();
        for (SeatLock lock : seats.findByShowId(showId)) {
            if (!"HELD".equals(lock.getStatus()) || lock.getBookingId() == null) {
                continue;
            }
            bookings.findById(lock.getBookingId()).ifPresent(booking -> {
                if (booking.getExpiresAt() != null && booking.getExpiresAt().isBefore(now) && "HELD".equals(booking.getStatus())) {
                    booking.setStatus("EXPIRED");
                    bookings.save(booking);
                    lock.setStatus("AVAILABLE");
                    lock.setBookingId(null);
                    seats.save(lock);
                }
            });
        }
    }

    private String nextCode() {
        return "CPX" + ThreadLocalRandom.current().nextInt(100000, 999999);
    }

    private String formatTime(String raw) {
        if (raw == null) {
            return "";
        }
        try {
            return LocalTime.parse(raw).format(DateTimeFormatter.ofPattern("h:mm a"));
        } catch (Exception ex) {
            return raw;
        }
    }
}
