package com.cinepass.booking;

import jakarta.persistence.*;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(name = "uk_show_seat", columnNames = {"showId", "seatNumber"}))
public class SeatLock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long showId;
    private String seatNumber;
    private String status;
    private Long bookingId;
    private Long version;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getShowId() { return showId; }
    public void setShowId(Long showId) { this.showId = showId; }
    public String getSeatNumber() { return seatNumber; }
    public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}
