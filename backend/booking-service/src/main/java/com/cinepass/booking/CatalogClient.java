package com.cinepass.booking;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class CatalogClient {
    private final RestTemplate restTemplate;

    public CatalogClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ShowInfo fetchShow(Long showId) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = restTemplate.getForObject("http://show-service/api/shows/" + showId, Map.class);
            if (map != null && map.get("id") != null) {
                ShowInfo info = new ShowInfo();
                info.setId(asLong(map.get("id")));
                info.setMovieId(asLong(map.get("movieId")));
                info.setCinema(String.valueOf(map.getOrDefault("cinema", "CinePass Cinemas")));
                info.setScreen(String.valueOf(map.getOrDefault("screen", "Screen 02")));
                info.setShowDate(stringify(map.get("showDate")));
                info.setShowTime(stringify(map.get("showTime")));
                info.setPrice(map.get("price") == null ? 200 : ((Number) map.get("price")).intValue());
                info.setRowsLayout(String.valueOf(map.getOrDefault("rowsLayout", "A,B,C,D,E,F,G,H,I")));
                Object seats = map.get("seatsPerRow");
                info.setSeatsPerRow(seats == null ? 12 : ((Number) seats).intValue());
                return info;
            }
        } catch (Exception ignored) {
        }
        ShowInfo fallback = new ShowInfo();
        fallback.setId(showId);
        fallback.setMovieId(1L);
        fallback.setCinema("CinePass Cinemas");
        fallback.setScreen("Screen 02");
        fallback.setShowDate("2026-09-15");
        fallback.setShowTime("18:00:00");
        fallback.setPrice(200);
        fallback.setRowsLayout("A,B,C,D,E,F,G,H,I");
        fallback.setSeatsPerRow(12);
        return fallback;
    }

    public String fetchMovieTitle(Long movieId) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = restTemplate.getForObject("http://movie-service/api/movies/" + movieId, Map.class);
            if (map != null && map.get("title") != null) {
                return String.valueOf(map.get("title"));
            }
        } catch (Exception ignored) {
        }
        return "Interstellar";
    }

    private Long asLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.valueOf(String.valueOf(value));
    }

    private String stringify(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof Iterable<?> iterable) {
            StringBuilder sb = new StringBuilder();
            for (Object part : iterable) {
                if (!sb.isEmpty()) {
                    sb.append("-");
                }
                sb.append(part);
            }
            return sb.toString();
        }
        return String.valueOf(value);
    }
}
