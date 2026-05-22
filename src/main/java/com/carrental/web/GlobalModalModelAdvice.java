package com.carrental.web;

import com.carrental.model.Car;
import com.carrental.model.CarStatus;
import com.carrental.web.dto.RentalForm;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(basePackages = "com.carrental.web")
public class GlobalModalModelAdvice {

    @ModelAttribute
    public void modalDefaults(Model model) {
        if (!model.containsAttribute("modalCar")) {
            Car car = new Car();
            car.setWithDriver(false);
            car.setStatus(CarStatus.AVAILABLE);
            car.setVehicleType(com.carrental.model.VehicleType.SEDAN);
            car.setFreeKmPerDay(0);
            model.addAttribute("modalCar", car);
        }
        model.addAttribute("carStatuses", CarStatus.values());
        model.addAttribute("vehicleTypes", com.carrental.model.VehicleType.values());
        if (!model.containsAttribute("hireForm")) {
            model.addAttribute("hireForm", new RentalForm());
        }
    }
}
