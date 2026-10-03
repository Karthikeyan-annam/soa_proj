package com.cinepass.booking;

public class ShowInfo {
    private Long id;
    private Long movieId;
    private String cinema;
    private String screen;
    private String showDate;
    private String showTime;
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
    public String getShowDate() { return showDate; }
    public void setShowDate(String showDate) { this.showDate = showDate; }
    public String getShowTime() { return showTime; }
    public void setShowTime(String showTime) { this.showTime = showTime; }
    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }
    public String getRowsLayout() { return rowsLayout; }
    public void setRowsLayout(String rowsLayout) { this.rowsLayout = rowsLayout; }
    public int getSeatsPerRow() { return seatsPerRow; }
    public void setSeatsPerRow(int seatsPerRow) { this.seatsPerRow = seatsPerRow; }
}
