package com.cinepass.showtime;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ShowRepository extends JpaRepository<MovieShow, Long> {
    List<MovieShow> findByMovieIdOrderByShowDateAscShowTimeAsc(Long movieId);
    List<MovieShow> findByMovieIdAndShowDateOrderByShowTimeAsc(Long movieId, LocalDate date);
}
