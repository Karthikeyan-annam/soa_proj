package com.cinepass.movie;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/movies")
public class MovieController {
    private final MovieRepository movies;

    public MovieController(MovieRepository movies) {
        this.movies = movies;
    }

    @GetMapping
    public List<Movie> list(@RequestParam(required = false) String status) {
        if (status == null || status.isBlank()) {
            return movies.findAll();
        }
        return movies.findByStatus(status);
    }

    @GetMapping("/{id}")
    public Movie get(@PathVariable Long id) {
        return movies.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Movie not found"));
    }

    @PostMapping
    public Movie create(@RequestBody Movie movie) {
        movie.setId(null);
        return movies.save(movie);
    }

    @PutMapping("/{id}")
    public Movie update(@PathVariable Long id, @RequestBody Movie incoming) {
        Movie existing = get(id);
        existing.setTitle(incoming.getTitle());
        existing.setGenres(incoming.getGenres());
        existing.setRating(incoming.getRating());
        existing.setRatingCount(incoming.getRatingCount());
        existing.setDuration(incoming.getDuration());
        existing.setLanguage(incoming.getLanguage());
        existing.setSynopsis(incoming.getSynopsis());
        existing.setDirector(incoming.getDirector());
        existing.setCastNames(incoming.getCastNames());
        existing.setPosterUrl(incoming.getPosterUrl());
        existing.setBackdropUrl(incoming.getBackdropUrl());
        existing.setTrailerUrl(incoming.getTrailerUrl());
        existing.setStatus(incoming.getStatus());
        existing.setCertificate(incoming.getCertificate());
        return movies.save(existing);
    }

    @DeleteMapping("/{id}")
    public Map<String, String> delete(@PathVariable Long id) {
        movies.delete(get(id));
        return Map.of("status", "deleted");
    }
}
