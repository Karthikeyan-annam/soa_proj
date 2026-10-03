package com.cinepass.booking;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface SeatLockRepository extends JpaRepository<SeatLock, Long> {
    List<SeatLock> findByShowId(Long showId);

    Optional<SeatLock> findByShowIdAndSeatNumber(Long showId, String seatNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM SeatLock s WHERE s.showId = :showId AND s.seatNumber IN :seats")
    List<SeatLock> lockSeats(Long showId, Collection<String> seats);

    List<SeatLock> findByBookingId(Long bookingId);
}
