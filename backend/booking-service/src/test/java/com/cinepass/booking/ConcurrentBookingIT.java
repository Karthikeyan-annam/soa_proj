package com.cinepass.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.profiles.active=test",
        "eureka.client.enabled=false",
        "spring.cloud.discovery.enabled=false"
})
class ConcurrentBookingIT {

    @Autowired
    BookingService bookingService;

    @Test
    void onlyOneCustomerCanBookTheSameSeat() throws Exception {
        int threads = 8;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger successes = new AtomicInteger();
        AtomicInteger conflicts = new AtomicInteger();
        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < threads; i++) {
            long userId = 100L + i;
            futures.add(pool.submit(() -> {
                start.await();
                try {
                    bookingService.reserve(userId, "user" + userId + "@cinepass.test", List.of("A10"), 99L);
                    successes.incrementAndGet();
                } catch (SeatUnavailableException ex) {
                    conflicts.incrementAndGet();
                } catch (Exception ex) {
                    if (ex.getMessage() != null && ex.getMessage().toLowerCase().contains("seat")) {
                        conflicts.incrementAndGet();
                    } else {
                        throw ex;
                    }
                }
                return null;
            }));
        }

        start.countDown();
        for (Future<?> future : futures) {
            future.get();
        }
        pool.shutdown();

        assertEquals(1, successes.get(), "exactly one booking must succeed");
        assertEquals(threads - 1, conflicts.get(), "everyone else must be rejected");
    }
}
