package com.carrental.service;

import com.carrental.model.Car;
import com.carrental.model.CarStatus;
import com.carrental.model.HireType;
import com.carrental.model.Rental;
import com.carrental.model.RentalStatus;
import com.carrental.util.NicNormalizer;
import com.carrental.repository.RentalRepository;
import com.carrental.web.SearchQuery;
import com.carrental.web.dto.AvailableCarOption;
import com.carrental.web.dto.RentalOverdueAlert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class RentalService {

    public static final int RENTALS_PAGE_SIZE = 5;
    private static final int RENTALS_MAX_PAGE_SIZE = 50;

    private static final List<RentalStatus> BLOCKING_STATUSES =
            Arrays.asList(RentalStatus.ACTIVE, RentalStatus.PENDING);

    private static final List<RentalStatus> OVERDUE_STATUSES =
            Arrays.asList(RentalStatus.ACTIVE, RentalStatus.PENDING);

    private final RentalRepository rentalRepository;
    private final CarService carService;
    private final BlacklistedCustomerService blacklistedCustomerService;
    private final EmployeeService employeeService;

    public RentalService(
            RentalRepository rentalRepository,
            CarService carService,
            BlacklistedCustomerService blacklistedCustomerService,
            EmployeeService employeeService) {
        this.rentalRepository = rentalRepository;
        this.carService = carService;
        this.blacklistedCustomerService = blacklistedCustomerService;
        this.employeeService = employeeService;
    }

    public List<Rental> listActive() {
        return rentalRepository.findByRentalStatusOrderByHireDateDesc(RentalStatus.ACTIVE);
    }

    public Page<Rental> searchAll(String query, int page, int size) {
        String q = SearchQuery.normalize(query);
        Pageable pageable = PageRequest.of(
                Math.max(0, page),
                normalizePageSize(size),
                Sort.by(Sort.Direction.DESC, "hireDate"));
        if (q.isEmpty()) {
            return rentalRepository.findAllByRentalStatusNotOrderByHireDateDesc(RentalStatus.CANCELLED, pageable);
        }
        return rentalRepository.searchAllByTerm(q, pageable);
    }

    private static int normalizePageSize(int size) {
        if (size < 1) {
            return RENTALS_PAGE_SIZE;
        }
        return Math.min(size, RENTALS_MAX_PAGE_SIZE);
    }

    public List<Rental> searchActive(String query) {
        String q = SearchQuery.normalize(query);
        if (q.isEmpty()) {
            return listActive();
        }
        return rentalRepository.searchActiveByTerm(q);
    }

    public List<Rental> listOverdue() {
        LocalDate today = LocalDate.now();
        return rentalRepository.findBlockingRentalsWithCar(OVERDUE_STATUSES).stream()
                .filter(r -> RentalPeriodHelper.isOverdue(r, today))
                .sorted(Comparator.comparing(RentalPeriodHelper::endDate))
                .collect(Collectors.toList());
    }

    public List<Rental> searchOverdue(String query) {
        String q = SearchQuery.normalize(query);
        List<Rental> overdue = listOverdue();
        if (q.isEmpty()) {
            return overdue;
        }
        String lower = q.toLowerCase(Locale.ENGLISH);
        return overdue.stream()
                .filter(r -> matchesSearchTerm(r, lower))
                .collect(Collectors.toList());
    }

    public long countOverdue() {
        return listOverdue().size();
    }

    public List<RentalOverdueAlert> findOverdueAlerts() {
        LocalDate today = LocalDate.now();
        return listOverdue().stream()
                .map(rental -> toOverdueAlert(rental, today))
                .collect(Collectors.toList());
    }

    private static RentalOverdueAlert toOverdueAlert(Rental rental, LocalDate today) {
        return new RentalOverdueAlert(
                rental.getId(),
                rental.getCar().getRegistrationNumber(),
                rental.getCustomerName(),
                RentalPeriodHelper.endDate(rental),
                RentalPeriodHelper.daysOverdue(rental, today));
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
                        car.getRentalPricePerDay(),
                        CarPricingHelper.resolveWeekPrice(car),
                        CarPricingHelper.resolveMonthPrice(car),
                        car.getExtraPricePerHour()))
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
            HireType hireType,
            LocalDate startDate,
            LocalTime startTime,
            LocalDate endDate,
            LocalTime endTime,
            String customerName,
            String customerAddress,
            String customerContact,
            String customerIdNumber,
            String travelLocation) {
        LocalTime pickupTime = startTime != null ? startTime : LocalTime.of(9, 0);
        LocalTime plannedReturnTime = endTime != null ? endTime : pickupTime;
        LocalDateTime pickupDateTime = RentalDurationHelper.combine(startDate, pickupTime);
        LocalDateTime endDateTime = RentalDurationHelper.combine(endDate, plannedReturnTime);
        RentalDurationHelper.Split duration = RentalDurationHelper.split24h(pickupDateTime, endDateTime);

        validatePeriod(startDate, endDate);
        blacklistedCustomerService.ensureNotBlacklisted(customerIdNumber);
        Car car = carService.getById(carId);
        if (hasOverlappingBooking(carId, startDate, endDate)) {
            throw new IllegalStateException(
                    "This vehicle is already booked for part of the selected dates. Choose different dates or another car.");
        }
        LocalDate today = LocalDate.now();

        Rental rental = new Rental();
        rental.setCar(car);
        rental.setNumberOfDays(duration.getFullDays());
        rental.setBillableExtraHours(duration.getExtraHours());
        rental.setHireType(hireType != null ? hireType : HireType.PER_DAY);
        rental.setCustomerName(customerName);
        rental.setCustomerAddress(customerAddress);
        rental.setCustomerContact(customerContact);
        rental.setCustomerIdNumber(customerIdNumber.trim());
        rental.setTravelLocation(travelLocation);
        rental.setHireDate(today);
        rental.setPickupDate(startDate);
        rental.setPickupTime(pickupTime);
        rental.setReturnDate(endDate);
        rental.setReturnTime(plannedReturnTime);
        rental.setRentalStatus(RentalStatus.ACTIVE);
        applyEmployeeHireIfMatched(rental, customerIdNumber);
        rentalRepository.save(rental);

        syncCarAvailability(car, today);
        return rental;
    }

    @Transactional
    public Rental completeRental(
            Long rentalId,
            LocalDate returnDate,
            LocalTime returnTime,
            Integer returnMileageKm,
            boolean documentReturned,
            boolean blacklistCustomer,
            String blacklistReason,
            BigDecimal discount,
            String completionComment) {
        Rental rental = getByIdWithCar(rentalId);
        if (rental.getRentalStatus() != RentalStatus.ACTIVE) {
            throw new IllegalStateException("Rental is already completed.");
        }
        if (returnDate == null) {
            throw new IllegalArgumentException("Return date is required.");
        }
        if (returnTime == null) {
            throw new IllegalArgumentException("Return time is required.");
        }
        if (returnMileageKm == null || returnMileageKm < 0) {
            throw new IllegalArgumentException("Return mileage is required.");
        }

        Car car = rental.getCar();
        LocalDateTime pickupDateTime = RentalPeriodHelper.pickupDateTime(rental);
        LocalDateTime plannedReturnDateTime = RentalPeriodHelper.plannedReturnDateTime(rental);
        LocalDateTime actualReturnDateTime = RentalDurationHelper.combine(returnDate, returnTime);
        applyEmployeeHireIfMatched(rental, rental.getCustomerIdNumber());
        boolean employeeHire = Boolean.TRUE.equals(rental.getEmployeeHire());

        HireType hireType = rental.getHireType() != null ? rental.getHireType() : HireType.PER_DAY;
        RentalPricingHelper.PriceBreakdown pricing = employeeHire
                ? RentalPricingHelper.calculateWaivedForCompletion(
                        car, hireType, pickupDateTime, plannedReturnDateTime, actualReturnDateTime, returnMileageKm)
                : RentalPricingHelper.calculateForCompletion(
                        car, hireType, pickupDateTime, plannedReturnDateTime, actualReturnDateTime, returnMileageKm);

        BigDecimal discountAmount = normalizeCompletionDiscount(discount);
        BigDecimal subtotal = pricing.getTotal();
        if (discountAmount.compareTo(subtotal) > 0) {
            throw new IllegalArgumentException("Discount cannot exceed the calculated total of " + subtotal + ".");
        }

        rental.setReturnDate(returnDate);
        rental.setReturnTime(returnTime);
        rental.setNumberOfDays(pricing.getDays());
        rental.setBillableExtraHours(pricing.getExtraHours());
        rental.setExtraKm(BigDecimal.valueOf(pricing.getBillableExtraKm()));
        rental.setCompletionDiscount(discountAmount);
        rental.setCompletionComment(normalizeCompletionComment(completionComment));
        rental.setTotalPrice(subtotal.subtract(discountAmount));
        rental.setDocumentReturned(documentReturned);
        rental.setRentalStatus(RentalStatus.COMPLETED);
        rental.setCompletedDate(LocalDate.now());
        rentalRepository.save(rental);

        car.setMileageKm(returnMileageKm);
        carService.save(car);

        syncCarAvailability(car, LocalDate.now());

        if (blacklistCustomer && !employeeHire) {
            blacklistedCustomerService.blacklistFromRental(rental, blacklistReason);
        }
        return rental;
    }

    @Transactional
    public Rental cancelRental(Long rentalId) {
        Rental rental = getByIdWithCar(rentalId);
        RentalStatus status = rental.getRentalStatus();
        if (status != RentalStatus.ACTIVE && status != RentalStatus.PENDING) {
            throw new IllegalStateException("Only active or pending rentals can be cancelled.");
        }
        rental.setRentalStatus(RentalStatus.CANCELLED);
        rentalRepository.save(rental);
        syncCarAvailability(rental.getCar(), LocalDate.now());
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

    private static boolean matchesSearchTerm(Rental rental, String lowerQuery) {
        Car car = rental.getCar();
        return containsIgnoreCase(car.getRegistrationNumber(), lowerQuery)
                || containsIgnoreCase(car.getModelName(), lowerQuery)
                || containsIgnoreCase(rental.getCustomerName(), lowerQuery)
                || containsIgnoreCase(rental.getCustomerAddress(), lowerQuery)
                || containsIgnoreCase(rental.getCustomerContact(), lowerQuery)
                || containsIgnoreCase(rental.getCustomerIdNumber(), lowerQuery)
                || containsIgnoreCase(rental.getTravelLocation(), lowerQuery)
                || (rental.getRentalStatus() != null
                && rental.getRentalStatus().name().toLowerCase(Locale.ENGLISH).contains(lowerQuery))
                || String.valueOf(rental.getNumberOfDays()).contains(lowerQuery);
    }

    private static boolean containsIgnoreCase(String value, String lowerQuery) {
        return value != null && value.toLowerCase(Locale.ENGLISH).contains(lowerQuery);
    }

    private void applyEmployeeHireIfMatched(Rental rental, String customerIdNumber) {
        employeeService.findByNic(customerIdNumber).ifPresent(employee -> {
            rental.setEmployeeHire(Boolean.TRUE);
            rental.setEmployee(employee);
            rental.setCustomerIdNumber(NicNormalizer.normalize(customerIdNumber));
        });
    }

    private static void validatePeriod(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start and end dates are required.");
        }
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date must be on or after the start date.");
        }
    }

    public static BigDecimal normalizeCompletionDiscount(BigDecimal discount) {
        if (discount == null || discount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return discount;
    }

    private static String normalizeCompletionComment(String completionComment) {
        if (completionComment == null) {
            return null;
        }
        String trimmed = completionComment.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
