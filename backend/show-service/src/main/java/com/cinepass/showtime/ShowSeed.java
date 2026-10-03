package com.cinepass.showtime;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Configuration
public class ShowSeed {
    @Bean
    CommandLineRunner seedShows(ShowRepository repo) {
        return args -> {
            String[] cinemas = {
                    "CinePass Cinemas - Benz Circle",
                    "INOX Laila Mall",
                    "PVR Ripples Mall"
            };
            if (repo.count() > 0) {
                List<MovieShow> existing = repo.findAll();
                for (int i = 0; i < existing.size(); i++) {
                    MovieShow show = existing.get(i);
                    int cinemaIndex = i % cinemas.length;
                    show.setCinema(cinemas[cinemaIndex]);
                    show.setScreen("Screen 0" + (cinemaIndex + 1));
                }
                repo.saveAll(existing);
                return;
            }
            List<LocalDate> dates = List.of(
                    LocalDate.of(2026, 9, 14),
                    LocalDate.of(2026, 9, 15),
                    LocalDate.of(2026, 9, 16),
                    LocalDate.of(2026, 9, 17),
                    LocalDate.of(2026, 9, 18)
            );
            List<LocalTime> times = List.of(
                    LocalTime.of(10, 0),
                    LocalTime.of(12, 30),
                    LocalTime.of(15, 30),
                    LocalTime.of(18, 0),
                    LocalTime.of(20, 0)
            );
            for (long movieId = 1; movieId <= 4; movieId++) {
                for (int cinemaIndex = 0; cinemaIndex < cinemas.length; cinemaIndex++) {
                    for (LocalDate date : dates) {
                        for (LocalTime time : times) {
                            MovieShow show = new MovieShow();
                            show.setMovieId(movieId);
                            show.setCinema(cinemas[cinemaIndex]);
                            show.setScreen("Screen 0" + (cinemaIndex + 1));
                            show.setShowDate(date);
                            show.setShowTime(time);
                            show.setPrice(200 + (cinemaIndex * 30));
                            show.setRowsLayout("A,B,C,D,E,F,G,H,I");
                            show.setSeatsPerRow(12);
                            repo.save(show);
                        }
                    }
                }
            }
        };
    }
}
