package com.frontend.client;

import com.frontend.config.BackendConfig;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Optional.ofNullable;

@Component
@AllArgsConstructor
public class BookingClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(BookingClient.class);
    private final RestTemplate restTemplate;
    private final BackendConfig backendConfig;

    public List<LocalTime> getAvailableBookingTimes(LocalDate date, int repairDuration, Long garageId) {
        LOGGER.info("Given parameters to get available times, date: " + date + ", total repair time: " + repairDuration + ", garage id: " + garageId);
        try {
            URI url = UriComponentsBuilder.fromHttpUrl(backendConfig.getBookingApiEndpoint() + "/available-times")
                    .queryParam("date", date.toString())
                    .queryParam("repair-duration", repairDuration)
                    .queryParam("garage-id", garageId)
                    .build()
                    .encode()
                    .toUri();
            LocalTime[] response = restTemplate.getForObject(url, LocalTime[].class);
            return Arrays.asList(ofNullable(response).orElse(new LocalTime[0]));
        } catch (RestClientException e) {
            LOGGER.error(e.getMessage(), e);
            return new ArrayList<>();
        }
    }
}