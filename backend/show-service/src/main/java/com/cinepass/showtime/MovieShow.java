package com.cinepass.showtime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
public class MovieShow {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long movieId;
    private String cinema;
    private String screen;
    private LocalDate showDate;
    private LocalTime showTime;
    private int price;
    private String rowsLayout;
    private int seatsPerRow;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getMovieId() { return movieId; }
    public void setMovieId(Long movieId) { this.movieId = movieId; }
    public String getCinema() { return cinema; }
    public void setCinema(String cinema) { this.cinema = cinema; }
    public String getScreen() { return screen; }
    public void setScreen(String screen) { this.screen = screen; }
    public LocalDate getShowDate() { return showDate; }
    public void setShowDate(LocalDate showDate) { this.showDate = showDate; }
    public LocalTime getShowTime() { return showTime; }
    public void setShowTime(LocalTime showTime) { this.showTime = showTime; }
    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }
    public String getRowsLayout() { return rowsLayout; }
    public void setRowsLayout(String rowsLayout) { this.rowsLayout = rowsLayout; }
    public int getSeatsPerRow() { return seatsPerRow; }
    public void setSeatsPerRow(int seatsPerRow) { this.seatsPerRow = seatsPerRow; }
}
