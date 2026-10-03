package com.cinepass.movie;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MovieSeed {
    @Bean
    CommandLineRunner seedMovies(MovieRepository repo) {
        return args -> {
            if (repo.count() > 0) {
                return;
            }
            repo.save(movie(
                    "Interstellar",
                    "Sci-Fi · Drama · Adventure",
                    4.8, 1256, "2h 49m", "English",
                    "A team of explorers travel through a wormhole in space in an attempt to ensure humanity's survival. This visually stunning film explores love, sacrifice and the future of mankind.",
                    "Christopher Nolan",
                    "Matthew McConaughey, Anne Hathaway, Jessica Chastain",
                    "https://image.tmdb.org/t/p/w500/gEU2QniLT6KCkvRCGXb5JKz8Fx.jpg",
                    "https://image.tmdb.org/t/p/original/rAiYTfKGqDCRIIkoDw2lQCAB0IS.jpg",
                    "https://www.youtube.com/watch?v=zSWdZVtXT7E",
                    "NOW_SHOWING", "UA"
            ));
            repo.save(movie(
                    "Dune: Part Two",
                    "Sci-Fi · Action · English",
                    4.8, 980, "2h 46m", "English",
                    "Paul Atreides unites with Chani and the Fremen while seeking revenge against the conspirators who destroyed his family.",
                    "Denis Villeneuve",
                    "Timothée Chalamet, Zendaya, Rebecca Ferguson",
                    "https://image.tmdb.org/t/p/w500/1pdfLvkbY9ohJlCjQH2CZjjYVvJ.jpg",
                    "https://image.tmdb.org/t/p/original/xOMo8BRK7PfcbrCNbn5mqq4pQcA.jpg",
                    "https://www.youtube.com/watch?v=Way9Dexny3w",
                    "NOW_SHOWING", "UA"
            ));
            repo.save(movie(
                    "Deadpool & Wolverine",
                    "Action · English",
                    4.5, 2100, "2h 08m", "English",
                    "Wolverine is recovering from his adventures when he meets the loudmouth Deadpool, forming an unlikely team.",
                    "Shawn Levy",
                    "Ryan Reynolds, Hugh Jackman, Emma Corrin",
                    "https://image.tmdb.org/t/p/w500/8cdWjvZQUExUUTzyp4t6EDMubfO.jpg",
                    "https://image.tmdb.org/t/p/original/yDHYTfA3R0jFYba16jBB1ef8oIt.jpg",
                    "https://www.youtube.com/watch?v=73_1biulkYk",
                    "NOW_SHOWING", "A"
            ));
            repo.save(movie(
                    "Joker: Folie à Deux",
                    "Crime · Drama",
                    4.2, 640, "2h 18m", "English",
                    "Arthur Fleck is institutionalized at Arkham, awaiting trial for his crimes as Joker, while struggling with his dual identity.",
                    "Todd Phillips",
                    "Joaquin Phoenix, Lady Gaga, Brendan Gleeson",
                    "https://image.tmdb.org/t/p/w500/if8QiqCI7WAGImKcJCfzp6VHaTS.jpg",
                    "https://image.tmdb.org/t/p/original/uQ44ZhgtGwuYCSdqDjxx7pFZimr.jpg",
                    "https://www.youtube.com/watch?v=_OKAwz2MsJs",
                    "NOW_SHOWING", "A"
            ));
            repo.save(movie(
                    "Gladiator II",
                    "Action · Drama",
                    0.0, 0, "2h 28m", "English",
                    "Lucius must enter the Colosseum after the powerful emperors of Rome seize his home.",
                    "Ridley Scott",
                    "Paul Mescal, Denzel Washington, Pedro Pascal",
                    "https://image.tmdb.org/t/p/w500/2cxknyA5pZkzpJxHoBUlthA0eLo.jpg",
                    "https://image.tmdb.org/t/p/original/euYl5qVCacX7tx8nZ3oUh1CXO8U.jpg",
                    "https://www.youtube.com/watch?v=4rgYUipGJNo",
                    "COMING_SOON", "UA"
            ));
            repo.save(movie(
                    "Wicked",
                    "Fantasy · Musical",
                    0.0, 0, "2h 40m", "English",
                    "The untold story of the witches of Oz, Elphaba and Glinda, long before Dorothy arrived.",
                    "Jon M. Chu",
                    "Cynthia Erivo, Ariana Grande, Jonathan Bailey",
                    "https://image.tmdb.org/t/p/w500/c5TqXqZFVq5hDYEx9ldHjCCv3pR.jpg",
                    "https://image.tmdb.org/t/p/original/uVlUu574b7YLzOYc2v5LrK7D2pO.jpg",
                    "https://www.youtube.com/watch?v=6COmYeQ6egw",
                    "COMING_SOON", "U"
            ));
        };
    }

    private Movie movie(String title, String genres, double rating, int count, String duration,
                        String language, String synopsis, String director, String cast,
                        String poster, String backdrop, String trailer, String status, String cert) {
        Movie m = new Movie();
        m.setTitle(title);
        m.setGenres(genres);
        m.setRating(rating);
        m.setRatingCount(count);
        m.setDuration(duration);
        m.setLanguage(language);
        m.setSynopsis(synopsis);
        m.setDirector(director);
        m.setCastNames(cast);
        m.setPosterUrl(poster);
        m.setBackdropUrl(backdrop);
        m.setTrailerUrl(trailer);
        m.setStatus(status);
        m.setCertificate(cert);
        return m;
    }
}
