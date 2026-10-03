package com.cinepass.booking;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OccupancySeed {
    @Bean
    CommandLineRunner seedOccupiedSeats(BookingService bookingService) {
        return args -> bookingService.seedOccupied(9L, List.of(
                "B3", "B4", "C5", "C6", "C7", "D2", "D8", "E4", "E5", "F3", "F9", "G6", "G7", "H4"
        ));
    }
}
