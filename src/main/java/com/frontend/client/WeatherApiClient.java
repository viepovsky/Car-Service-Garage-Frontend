package com.frontend.client;

import com.frontend.config.BackendConfig;
import com.frontend.domainDto.response.ForecastDto;
import com.frontend.domainDto.response.GarageDto;
import com.vaadin.flow.server.VaadinSession;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;

@Component
@AllArgsConstructor
public class WeatherApiClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(WeatherApiClient.class);
    private final RestTemplate restTemplate;
    private final BackendConfig backendConfig;

    public ForecastDto getWeatherForCityAndDate(String city, LocalDate date) {
        try {
            HttpHeaders header = createJwtHeader();
            HttpEntity<Void> requestEntity = new HttpEntity<>(header);

            URI url = UriComponentsBuilder.fromHttpUrl(backendConfig.getWeatherApiEndpoint())
                    .queryParam("city", city)
                    .queryParam("date", date.toString())
                    .build()
                    .encode()
                    .toUri();

            ResponseEntity<ForecastDto> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, ForecastDto.class);
            return response.getBody();
        } catch (RestClientException e) {
            LOGGER.error(e.getMessage(), e);
            return new ForecastDto();
        }
    }

    private HttpHeaders createJwtHeader() {
        String jwtToken = VaadinSession.getCurrent().getAttribute("jwt").toString();
        HttpHeaders header = new HttpHeaders();
        header.set("Authorization", "Bearer " + jwtToken);
        return header;
    }
}
