package com.carrental.service;

import com.carrental.model.Car;
import com.carrental.model.CarStatus;
import com.carrental.model.Rental;
import com.carrental.model.RentalStatus;
import com.carrental.repository.RentalRepository;
import com.carrental.web.SearchQuery;
import com.carrental.web.dto.AvailableCarOption;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RentalService {

    private static final List<RentalStatus> BLOCKING_STATUSES =
            Arrays.asList(RentalStatus.ACTIVE, RentalStatus.PENDING);

    private final RentalRepository rentalRepository;
    private final CarService carService;

    public RentalService(RentalRepository rentalRepository, CarService carService) {
        this.rentalRepository = rentalRepository;
        this.carService = carService;
    }

    public List<Rental> listActive() {
        return rentalRepository.findByRentalStatusOrderByHireDateDesc(RentalStatus.ACTIVE);
    }

    public List<Rental> listAll() {
        return rentalRepository.findAllByOrderByHireDateDesc();
    }

    public List<Rental> searchAll(String query) {
        String q = SearchQuery.normalize(query);
        if (q.isEmpty()) {
            return listAll();
        }
        return rentalRepository.searchAllByTerm(q);
    }

    public List<Rental> searchActive(String query) {
        String q = SearchQuery.normalize(query);
        if (q.isEmpty()) {
            return listActive();
        }
        return rentalRepository.searchActiveByTerm(q);
    }

    public Rental getById(Long id) {
        return rentalRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Rental not found: " + id));
    }

    public Rental getByIdWithCar(Long id) {
        return rentalRepository.findByIdWithCar(id)
                .orElseThrow(() -> new IllegalArgumentException("Rental not found: " + id));
    }

    public List<AvailableCarOption> listAvailableCarsForPeriod(LocalDate startDate, LocalDate endDate) {
        validatePeriod(startDate, endDate);
        return carService.listAll().stream()
                .filter(car -> !hasOverlappingBooking(car.getId(), startDate, endDate))
                .sorted(Comparator.comparing(Car::getRegistrationNumber, String.CASE_INSENSITIVE_ORDER))
                .map(car -> new AvailableCarOption(
                        car.getId(),
                        car.getRegistrationNumber(),
                        car.getModelName(),
                        car.getPassengerCount(),
                        car.getRentalPricePerDay()))
                .collect(Collectors.toList());
    }

    public boolean hasOverlappingBooking(Long carId, LocalDate startDate, LocalDate endDate) {
        validatePeriod(startDate, endDate);
        return rentalRepository.findBlockingRentalsWithCar(BLOCKING_STATUSES).stream()
                .filter(r -> r.getCar().getId().equals(carId))
                .anyMatch(r -> RentalPeriodHelper.overlaps(
                        RentalPeriodHelper.startDate(r),
                        RentalPeriodHelper.endDate(r),
                        startDate,
                        endDate));
    }

    @Transactional
    public Rental createRental(
            Long carId,
            LocalDate startDate,
            LocalDate endDate,
            String customerName,
            String customerAddress,
            String customerContact) {
        validatePeriod(startDate, endDate);
        Car car = carService.getById(carId);
        if (hasOverlappingBooking(carId, startDate, endDate)) {
            throw new IllegalStateException(
                    "This vehicle is already booked for part of the selected dates. Choose different dates or another car.");
        }
        int days = RentalPeriodHelper.inclusiveDays(startDate, endDate);
        LocalDate today = LocalDate.now();

        Rental rental = new Rental();
        rental.setCar(car);
        rental.setNumberOfDays(days);
        rental.setCustomerName(customerName);
        rental.setCustomerAddress(customerAddress);
        rental.setCustomerContact(customerContact);
        rental.setHireDate(today);
        rental.setPickupDate(startDate);
        rental.setReturnDate(endDate);
        rental.setRentalStatus(RentalStatus.ACTIVE);
        rentalRepository.save(rental);

        syncCarAvailability(car, today);
        return rental;
    }

    @Transactional
    public Rental completeRental(
            Long rentalId,
            LocalDate returnDate,
            Integer returnMileageKm,
            boolean documentReturned) {
        Rental rental = getByIdWithCar(rentalId);
        if (rental.getRentalStatus() != RentalStatus.ACTIVE) {
            throw new IllegalStateException("Rental is already completed.");
        }
        if (returnDate == null) {
            throw new IllegalArgumentException("Return date is required.");
        }
        if (returnMileageKm == null || returnMileageKm < 0) {
            throw new IllegalArgumentException("Return mileage is required.");
        }

        Car car = rental.getCar();
        LocalDate pickupDate = RentalPeriodHelper.startDate(rental);
        RentalPricingHelper.PriceBreakdown pricing =
                RentalPricingHelper.calculate(car, pickupDate, returnDate, returnMileageKm);

        rental.setReturnDate(returnDate);
        rental.setNumberOfDays(pricing.getDays());
        rental.setExtraKm(BigDecimal.valueOf(pricing.getBillableExtraKm()));
        rental.setTotalPrice(pricing.getTotal());
        rental.setDocumentReturned(documentReturned);
        rental.setRentalStatus(RentalStatus.COMPLETED);
        rental.setCompletedDate(LocalDate.now());
        rentalRepository.save(rental);

        car.setMileageKm(returnMileageKm);
        carService.save(car);

        syncCarAvailability(car, LocalDate.now());
        return rental;
    }

    private void syncCarAvailability(Car car, LocalDate today) {
        boolean onHireToday = rentalRepository.findBlockingRentalsWithCar(BLOCKING_STATUSES).stream()
                .filter(r -> r.getCar().getId().equals(car.getId()))
                .anyMatch(r -> RentalPeriodHelper.includesToday(
                        RentalPeriodHelper.startDate(r),
                        RentalPeriodHelper.endDate(r),
                        today));
        car.setStatus(onHireToday ? CarStatus.UNAVAILABLE : CarStatus.AVAILABLE);
        carService.save(car);
    }

    private static void validatePeriod(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start and end dates are required.");
        }
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date must be on or after the start date.");
        }
    }
}
