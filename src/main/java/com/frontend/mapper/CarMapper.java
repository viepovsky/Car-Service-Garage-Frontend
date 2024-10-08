package com.frontend.mapper;

import com.frontend.domainDto.request.CarCreateDto;
import com.frontend.domainDto.response.CarDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CarMapper {
    public List<CarCreateDto> mapToCarCreateDtoList(List<CarDto> carDtoList) {
        return carDtoList.stream()
                .map(n -> new CarCreateDto(
                        n.getVehicleId(),
                        n.getVehicleModel().vehicleMake().makeName(),
                        n.getVehicleModel().modelName(),
                        n.getYear(),
                        n.getVehicleModel().type(),
                        n.getEngineType()
                ))
                .toList();
    }
}
