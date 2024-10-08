package com.frontend.views.layout;

import com.frontend.domainDto.request.CarCreateDto;
import com.frontend.domainDto.response.CarRepairDto;
import com.frontend.service.CarRepairService;
import com.frontend.service.CarService;
import com.frontend.views.CarView;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.IntStream;

public class CarForm extends FormLayout {
    private static final Logger LOGGER = LoggerFactory.getLogger(CarForm.class);
    private final String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
    private final CarService carService;
    private final CarView carView;
    private final CarRepairService carRepairService;
    private List<Integer> carYears;
    private final ComboBox<Integer> year = new ComboBox<>("Year");
    private final ComboBox<String> make = new ComboBox<>("Make");
    private final ComboBox<String> type = new ComboBox<>("Type");
    private final ComboBox<String> model = new ComboBox<>("Model");
    private final ComboBox<String> engine = new ComboBox<>("Engine type");
    private final Button save = new Button("Save");
    private final Button edit = new Button("Edit");
    private final Button delete = new Button("Delete");
    private final Button cancel = new Button("Cancel");
    private final Binder<CarCreateDto> binder = new BeanValidationBinder<>(CarCreateDto.class);
    private CarCreateDto temporaryDto;

    public CarForm(CarService carService, CarView carView, CarRepairService carRepairService) {
        this.carService = carService;
        this.carView = carView;
        this.carRepairService = carRepairService;

        binder.bindInstanceFields(this);

        setYearsMakesTypesLists();
        addFieldsAndButtons();
        addButtonsListeners();

        addBinderValueChangeListener();
    }

    private void setYearsMakesTypesLists() {
        carYears = generateCarYears();
        List<String> carMakeList = carService.getCarMakes();
        List<String> carTypeList =
                List.of(
                        "SEDAN",
                        "WAGON",
                        "HATCHBACK",
                        "CONVERTIBLE",
                        "SUV",
                        "MOTORCYCLE",
                        "PICKUP",
                        "VAN",
                        "COUPE");
        List<String> carEnginesList =
                List.of("DIESEL", "PETROL", "PETROL_AND_GAS", "HYBRID", "ELECTRIC");

        year.setItems(carYears);
        make.setItems(carMakeList);
        type.setItems(carTypeList);
        engine.setItems(carEnginesList);
    }

    private List<Integer> generateCarYears() {
        List<Integer> carYears = new ArrayList<>();
        IntStream.rangeClosed(1950, LocalDateTime.now().getYear()).forEach(carYears::add);
        return carYears;
    }

    private void addFieldsAndButtons() {
        HorizontalLayout buttons = new HorizontalLayout(save, edit, delete, cancel);
        delete.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        edit.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        add(year, make, type, model, engine);
        add(buttons);
    }

    private void addButtonsListeners() {
        save.addClickListener(event -> save());
        delete.addClickListener(event -> delete());
        edit.addClickListener(event -> edit());
        cancel.addClickListener(event -> cancel());
    }

    private void addBinderValueChangeListener() {
        binder.addValueChangeListener(event -> {
            CarCreateDto carCreateDto = new CarCreateDto(binder.getBean());
            LOGGER.info("Listener processed with binder values: {}", carCreateDto);
            int carYear = year.getValue();
            String carMake = make.getValue();
            String carType = type.getValue();
            if (temporaryDto != null) {
                if (temporaryDto.getYear() != carYear || !temporaryDto.getMake().equals(carMake) || !temporaryDto.getType().equals(carType)) {
                    setCarModels(carYear, carMake, carType);
                }
            } else if (carYear > 1950 && carMake != null && carType != null) {
                setCarModels(carYear, carMake, carType);
            }
        });
    }

    private void setCarModels(int carYear, String carMake, String carType) {
        List<String> modelList = carService.getCarModels(carMake, carType, carYear);
        LOGGER.info("Car models set to: {}", modelList);
        temporaryDto = new CarCreateDto();
        temporaryDto.setYear(carYear);
        temporaryDto.setMake(carMake);
        temporaryDto.setType(carType);
        model.setItems(modelList);
    }

    public void setCarCreateDto(CarCreateDto carCreateDto) {
        if (carCreateDto == null) {
            setVisible(false);
        } else if (carCreateDto.equals(new CarCreateDto())) {
            model.setItems(new ArrayList<>());
            binder.setBean(new CarCreateDto());
            temporaryDto = null;

            save.setVisible(true);
            cancel.setVisible(true);
            edit.setVisible(false);
            delete.setVisible(false);

            setVisible(true);
            year.focus();
            year.setValue(carYears.get(0));
        } else {
            LOGGER.info("Getting car models with values: {}, {}, {}", carCreateDto.getMake(), carCreateDto.getType(), carCreateDto.getYear());
            List<String> modelList = carService.getCarModels(carCreateDto.getMake(), carCreateDto.getType(), carCreateDto.getYear());
            model.setItems(modelList);
            temporaryDto = new CarCreateDto(carCreateDto);
            binder.setBean(new CarCreateDto(carCreateDto));

            save.setVisible(false);
            cancel.setVisible(true);
            edit.setVisible(true);
            delete.setVisible(true);

            setVisible(true);
            year.focus();
        }
    }

    private void cancel() {
        LOGGER.info("Button cancel clicked");
        carView.refresh();
        setCarCreateDto(null);
    }

    private void edit() {
        CarCreateDto carCreateDto = binder.getBean();
        if (binder.writeBeanIfValid(carCreateDto)) {
            carService.updateCar(carCreateDto);
            carView.refresh();
            setCarCreateDto(null);
            Notification.show("Car updated.");
        } else {
            Notification.show("All fields must be valid if you want to edit your car.");
        }
        LOGGER.info("Button edit clicked with object: {}", carCreateDto);
    }

    private void save() {
        CarCreateDto carCreateDto = binder.getBean();
        if (binder.writeBeanIfValid(carCreateDto)) {
            carService.saveCar(carCreateDto, currentUsername);
            carView.refresh();
            setCarCreateDto(null);
            Notification.show("Car added.");
        } else {
            Notification.show("All fields must be valid if you want to add a new car.");
        }
        LOGGER.info("Button save clicked with object: {}", carCreateDto);
    }

    private void delete() {
        CarCreateDto carCreateDto = binder.getBean();
        List<CarRepairDto> serviceList = carRepairService.getCarServices(currentUsername).stream().filter(n -> Objects.equals(n.getCarDto().getVehicleId(), carCreateDto.getId())).toList();
        if (serviceList.isEmpty()) {
            carService.deleteCar(carCreateDto.getId());
            LOGGER.info("Button delete clicked with object: " + carCreateDto);
            carView.refresh();
            setCarCreateDto(null);
            Notification.show("Car deleted.");
        } else {
            Notification.show("Cannot delete car, there are connected services. Check \"My Services\" page.");
        }
    }
}
