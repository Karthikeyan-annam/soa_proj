package com.cinepass.showtime;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/shows")
public class ShowController {
    private final ShowRepository shows;

    public ShowController(ShowRepository shows) {
        this.shows = shows;
    }

    @GetMapping
    public List<MovieShow> list(
            @RequestParam(required = false) Long movieId,
            @RequestParam(required = false) LocalDate date
    ) {
        if (movieId != null && date != null) {
            return shows.findByMovieIdAndShowDateOrderByShowTimeAsc(movieId, date);
        }
        if (movieId != null) {
            return shows.findByMovieIdOrderByShowDateAscShowTimeAsc(movieId);
        }
        return shows.findAll();
    }

    @GetMapping("/movie/{movieId}")
    public List<MovieShow> listByMovie(@PathVariable Long movieId) {
        return shows.findByMovieIdOrderByShowDateAscShowTimeAsc(movieId);
    }

    @GetMapping("/{id}")
    public MovieShow get(@PathVariable Long id) {
        return shows.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Show not found"));
    }

    @PostMapping
    public MovieShow create(@RequestBody MovieShow show) {
        show.setId(null);
        if (show.getRowsLayout() == null) {
            show.setRowsLayout("A,B,C,D,E,F,G,H,I");
        }
        if (show.getSeatsPerRow() == 0) {
            show.setSeatsPerRow(12);
        }
        return shows.save(show);
    }

    @PutMapping("/{id}")
    public MovieShow update(@PathVariable Long id, @RequestBody MovieShow incoming) {
        MovieShow existing = get(id);
        existing.setMovieId(incoming.getMovieId());
        existing.setCinema(incoming.getCinema());
        existing.setScreen(incoming.getScreen());
        existing.setShowDate(incoming.getShowDate());
        existing.setShowTime(incoming.getShowTime());
        existing.setPrice(incoming.getPrice());
        existing.setRowsLayout(incoming.getRowsLayout());
        existing.setSeatsPerRow(incoming.getSeatsPerRow());
        return shows.save(existing);
    }

    @DeleteMapping("/{id}")
    public Map<String, String> delete(@PathVariable Long id) {
        shows.delete(get(id));
        return Map.of("status", "deleted");
    }
}
