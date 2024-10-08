package com.frontend.client;

import static java.util.Optional.ofNullable;

import com.frontend.config.BackendConfig;
import com.frontend.domainDto.request.CarCreateDto;
import com.frontend.domainDto.response.CarDto;
import com.frontend.domainDto.response.MakeDto;
import com.frontend.domainDto.response.ModelDto;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@AllArgsConstructor
public class CarClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(CarClient.class);
    private final RestTemplate restTemplate;
    private final BackendConfig backendConfig;

    public List<CarDto> getCarsForGivenUsername(String username) {
        try {
            HttpHeaders header = createJwtHeader();
            HttpEntity<Void> requestEntity = new HttpEntity<>(header);

            URI url = UriComponentsBuilder.fromHttpUrl(backendConfig.getCarApiEndpoint())
                    .queryParam("username", username)
                    .build()
                    .encode()
                    .toUri();

            ResponseEntity<CarDto[]> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, CarDto[].class);
            return Arrays.asList(ofNullable(response.getBody()).orElse(new CarDto[0]));
        } catch (RestClientException e) {
            LOGGER.error(e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    public void saveCar(CarCreateDto carCreateDto, String username) {
        try {
            HttpHeaders header = createJwtHeader();
            HttpEntity<CarCreateDto> requestEntity = new HttpEntity<>(carCreateDto, header);

            URI url = UriComponentsBuilder.fromHttpUrl(backendConfig.getCarApiEndpoint())
                    .queryParam("username", username)
                    .build()
                    .encode()
                    .toUri();

            restTemplate.exchange(url, HttpMethod.POST, requestEntity, Void.class);
        } catch (RestClientException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public void deleteCar(Long carId) {
        try {
            HttpHeaders header = createJwtHeader();
            HttpEntity<Void> requestEntity = new HttpEntity<>(header);

            URI url = UriComponentsBuilder.fromHttpUrl(backendConfig.getCarApiEndpoint() + "/" + carId)
                    .build()
                    .encode()
                    .toUri();

            restTemplate.exchange(url, HttpMethod.DELETE, requestEntity, Void.class);
        } catch (RestClientException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public void updateCar(CarCreateDto carCreateDto) {
        try {
            HttpHeaders header = createJwtHeader();
            HttpEntity<CarCreateDto> requestEntity = new HttpEntity<>(carCreateDto, header);

            URI url = UriComponentsBuilder.fromHttpUrl(backendConfig.getCarApiEndpoint())
                    .build()
                    .encode()
                    .toUri();

            restTemplate.exchange(url, HttpMethod.PUT, requestEntity, Void.class);
        } catch (RestClientException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private HttpHeaders createJwtHeader() {
        String jwtToken = VaadinSession.getCurrent().getAttribute("jwt").toString();
        HttpHeaders header = new HttpHeaders();
        header.set("Authorization", "Bearer " + jwtToken);
        return header;
    }

    public List<MakeDto> getCarMakes() {
        try {
            HttpHeaders header = createJwtHeader();
            HttpEntity<Void> requestEntity = new HttpEntity<>(header);

            URI url = UriComponentsBuilder.fromHttpUrl(backendConfig.getCarApiEndpoint() + "/makes")
                                          .build()
                                          .encode()
                                          .toUri();

            ResponseEntity<MakeDto[]> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, MakeDto[].class);
            return Arrays.asList(ofNullable(response.getBody()).orElse(new MakeDto[0]));
        } catch (RestClientException e) {
            LOGGER.error(e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    public List<ModelDto> getCarModels(String make) {
        try {
            HttpHeaders header = createJwtHeader();
            HttpEntity<Void> requestEntity = new HttpEntity<>(header);

            URI url = UriComponentsBuilder.fromHttpUrl(backendConfig.getCarApiEndpoint() + "/models")
                                          .queryParam("make", make)
                                          .build()
                                          .encode()
                                          .toUri();

            ResponseEntity<ModelDto[]> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, ModelDto[].class);
            return Arrays.asList(ofNullable(response.getBody()).orElse(new ModelDto[0]));
        } catch (RestClientException e) {
            LOGGER.error(e.getMessage(), e);
            return new ArrayList<>();
        }
    }
}
