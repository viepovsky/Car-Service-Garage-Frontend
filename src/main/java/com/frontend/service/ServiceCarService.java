package com.frontend.service;

import com.frontend.client.CarServiceClient;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ServiceCarService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceCarService.class);
    private final CarServiceClient carServiceClient;

    public void addService(List<Long> selectedServicesIdList, Long carId) {
        carServiceClient.addService(selectedServicesIdList, carId);
        LOGGER.info("Added services of given id: " + selectedServicesIdList + " to car of given id: " + carId);
    }
}