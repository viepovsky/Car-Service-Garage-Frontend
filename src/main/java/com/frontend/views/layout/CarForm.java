package com.frontend.views.layout;

import com.frontend.domainDto.request.CarCreateDto;
import com.frontend.service.CarApiService;
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
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CarForm extends FormLayout {
    private static final Logger LOGGER = LoggerFactory.getLogger(CarForm.class);
    private final String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
    private final CarApiService carApiService;
    private final CarService carService;
    private final CarView carView;
    private List<Integer> carYearList;
    private final ComboBox<Integer> year = new ComboBox<>("Year");
    private final ComboBox<String> make = new ComboBox<>("Make");
    private final ComboBox<String> type = new ComboBox<>("Type");
    private final ComboBox<String> model = new ComboBox<>("Model");
    private final ComboBox<String> engine = new ComboBox<>("Engine type");
    private final Button save = new Button("Save");
    private final Button edit = new Button("Edit");
    private final Button delete = new Button("Delete");
    private final Button refresh = new Button("Click to refresh car models.");
    private final Button cancel = new Button("Cancel");
    private final Binder<CarCreateDto> binder = new BeanValidationBinder<>(CarCreateDto.class);
    private CarCreateDto temporaryDto;
    public CarForm(CarApiService carApiService, CarService carService, CarView carView) {
        this.carApiService = carApiService;
        this.carService = carService;
        this.carView = carView;

        binder.bindInstanceFields(this);

        setYearsMakesTypesLists();
        addFieldsAndButtons();
        addButtonsListeners();

        addBinderValueChangeListener();
    }

    private void setYearsMakesTypesLists() {
        carYearList = carApiService.getCarYears();
        List<String> carMakeList = carApiService.getCarMakes();
        List<String> carTypeList = carApiService.getCarTypes();
        List<String> carEnginesList = Arrays.asList("Petrol", "Diesel", "Hybrid", "Electric", "LPG", "Other");
        Collections.sort(carYearList);
        Collections.sort(carMakeList);
        Collections.sort(carTypeList);

        year.setItems(carYearList);
        make.setItems(carMakeList);
        type.setItems(carTypeList);
        engine.setItems(carEnginesList);
    }

    private void addFieldsAndButtons() {
        HorizontalLayout buttons = new HorizontalLayout(save, edit, delete, cancel);
        delete.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        edit.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        refresh.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        refresh.addClassName(LumoUtility.Margin.Top.MEDIUM);
        add(year, make, type, model, engine);
        add(refresh);
        add(buttons);
    }

    private void addButtonsListeners() {
        save.addClickListener(event -> save());
        delete.addClickListener(event -> delete());
        refresh.addClickListener(event -> refreshCarModels());
        edit.addClickListener(event -> edit());
        cancel.addClickListener(event -> cancel());
    }

    private void addBinderValueChangeListener() {
        binder.addValueChangeListener( event -> {
            CarCreateDto carCreateDto = new CarCreateDto(binder.getBean());
            if (temporaryDto != null) {
                if (temporaryDto.getYear() != carCreateDto.getYear() || !temporaryDto.getMake().equals(carCreateDto.getMake()) || !temporaryDto.getType().equals(carCreateDto.getType())) {
                    if (!model.isEmpty()){
                        LOGGER.info("Model items set to new ArrayList");
                        model.setItems(new ArrayList<>());
                    }
                }
            }
        });
    }

    public void setCarCreateDto(CarCreateDto carCreateDto) {
    if (carCreateDto == null) {
        setVisible(false);
    } else if (carCreateDto.equals(new CarCreateDto())) {
        model.setItems(new ArrayList<>());
        binder.setBean(new CarCreateDto());

        save.setVisible(true);
        cancel.setVisible(true);
        refresh.setVisible(true);
        edit.setVisible(false);
        delete.setVisible(false);

        setVisible(true);
        year.setItems(carYearList);
    } else {
        binder.setBean(new CarCreateDto());
        LOGGER.info("Getting car models with values: " + carCreateDto.getMake() + ", " + carCreateDto.getType() + ", " + carCreateDto.getYear());
        List<String> modelList = carApiService.getCarModels(carCreateDto.getMake(), carCreateDto.getType(), carCreateDto.getYear());
        model.setItems(modelList);
        temporaryDto = new CarCreateDto(carCreateDto);
        binder.setBean(carCreateDto);

        save.setVisible(false);
        cancel.setVisible(true);
        refresh.setVisible(true);
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
        if (binder.writeBeanIfValid(carCreateDto)){
            carService.updateCar(carCreateDto);
            carView.refresh();
            setCarCreateDto(null);
            Notification.show("Car updated.");
        } else {
            Notification.show("All fields must be valid if you want to edit your car.");
        }
        LOGGER.info("Button edit clicked with object: " + carCreateDto);
    }

    private void save() {
        CarCreateDto carCreateDto = binder.getBean();
        if (binder.writeBeanIfValid(carCreateDto)){
            carService.saveCar(carCreateDto, currentUsername);
            carView.refresh();
            setCarCreateDto(null);
            Notification.show("Car added.");
        } else {
            Notification.show("All fields must be valid if you want to add a new car.");
        }
        LOGGER.info("Button save clicked with object: " + carCreateDto);
    }

    private void delete() {
        CarCreateDto carCreateDto = binder.getBean();
        carService.deleteCar(carCreateDto.getId());
        LOGGER.info("Button delete clicked with object: " + carCreateDto);
        carView.refresh();
        setCarCreateDto(null);
    }

    private void refreshCarModels() {
        int carYear = year.getValue();
        String carMake = make.getValue();
        String carType = type.getValue();
        LOGGER.info("Clicked refresh button with: " + carYear + ", " + carMake + ", " + carType);
        if (carYear > 1950 && carMake != null && carType != null ){
            List<String> modelList = carApiService.getCarModels(carMake, carType, carYear);
            model.setItems(modelList);
            Notification.show("You can pick your car model now.");
            temporaryDto = new CarCreateDto();
            temporaryDto.setYear(carYear);
            temporaryDto.setMake(carMake);
            temporaryDto.setType(carType);
        } else {
            Notification.show("You need to pick year, make and type in order to refresh model.");
        }
    }
}
