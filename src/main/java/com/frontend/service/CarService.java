package com.frontend.service;

import com.frontend.client.CarClient;
import com.frontend.domainDto.request.CarCreateDto;
import com.frontend.domainDto.response.CarDto;
import com.frontend.domainDto.response.MakeDto;
import com.frontend.domainDto.response.ModelDto;
import com.frontend.mapper.CarMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CarService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CarService.class);
    private final CarClient carClient;
    private final CarMapper carMapper;

    public List<CarDto> getCarsForGivenUsername(String username) {
        if (username == null) {
            LOGGER.error("Given username is invalid.");
            return new ArrayList<>();
        }
        List<CarDto> carDtoList = carClient.getCarsForGivenUsername(username);
        LOGGER.info("Retrieved car list with size of: {}", carDtoList.size());
        return carDtoList;
    }

    public List<CarCreateDto> getMappedCarsForGivenUsername (String username) {
        List<CarDto> carDtoList = getCarsForGivenUsername(username);
        return carMapper.mapToCarCreateDtoList(carDtoList);
    }

    public void updateCar(CarCreateDto carCreateDto) {
        if (carCreateDto == null) {
            LOGGER.error("Given object for update is null.");
            return;
        }{
            carClient.updateCar(carCreateDto);
            LOGGER.info("Car with id {} has been updated.", carCreateDto.getId());
        }
    }

    public void saveCar(CarCreateDto carCreateDto, String username) {
        if (carCreateDto == null) {
            LOGGER.error("Cannot save null car.");
            return;
        }{
            carClient.saveCar(carCreateDto, username);
            LOGGER.info("Car with ID {} has been saved.", carCreateDto.getId());
        }
    }

    public void deleteCar(Long carId) {
        if (carId == null || carId < 0) {
            LOGGER.error("Cannot delete car when id is invalid.");
            return;
        }{
            carClient.deleteCar(carId);
            LOGGER.info("Car with ID {} has been deleted.", carId);
        }
    }

    public List<String> getCarMakes() {
        LOGGER.info("Getting car makes.");
        List<MakeDto> carMakes = carClient.getCarMakes();
        return carMakes.stream().map(MakeDto::makeName).toList();
    }

    public List<String> getCarModels(String make, String type, Integer year) {
        LOGGER.info("Getting car models, for make:{}", make);
        List<ModelDto> carModelDtos = carClient.getCarModels(make);
        List<String> carModels = carModelDtos.stream().map(ModelDto::modelName).toList();
        LOGGER.info("Car models received.");
        return carModels;
    }
}
