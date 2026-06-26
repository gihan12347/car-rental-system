package com.carrental.dashboard;

import com.carrental.model.*;
import com.carrental.repository.BlacklistedCustomerRepository;
import com.carrental.repository.CarRepository;
import com.carrental.repository.RentalRepository;
import com.carrental.service.*;
import com.carrental.web.dto.RentalOverdueAlert;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.TreeMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.time.temporal.TemporalAdjusters;
import static com.carrental.model.ApplicationParameterCode.LOW_UTILIZATION_THRESHOLD;
import static com.carrental.model.ApplicationParameterCode.TOP_CUSTOMERS_LIMIT;

@Service
public class DashboardAnalyticsService {

    private static final DateTimeFormatter DAY_FMT = DateTimeFormatter.ofPattern("d MMM", Locale.ENGLISH);
    private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH);

    private final CarRepository carRepository;
    private final RentalRepository rentalRepository;
    private final BlacklistedCustomerRepository blacklistedCustomerRepository;
    private final EmployeePaymentService employeePaymentService;
    private final OfficeExpenseService officeExpenseService;
    private final RentalService rentalService;
    private final ApplicationParameterService applicationParameterService;

    public DashboardAnalyticsService(
            CarRepository carRepository,
            RentalRepository rentalRepository,
            BlacklistedCustomerRepository blacklistedCustomerRepository,
            EmployeePaymentService employeePaymentService,
            OfficeExpenseService officeExpenseService,
            RentalService rentalService, ApplicationParameterService applicationParameterService) {
        this.carRepository = carRepository;
        this.rentalRepository = rentalRepository;
        this.blacklistedCustomerRepository = blacklistedCustomerRepository;
        this.employeePaymentService = employeePaymentService;
        this.officeExpenseService = officeExpenseService;
        this.rentalService = rentalService;
        this.applicationParameterService = applicationParameterService;
    }

    public DashboardData build(DateRange range, DashboardPeriod period) {
        LocalDate today = LocalDate.now();
        List<Car> cars = carRepository.findAllByOrderByRegistrationNumberAsc();
        List<Rental> rentals = rentalRepository.findAllWithCar();

        DashboardData data = new DashboardData();
        data.setPeriodLabel(period.getLabel());
        data.setRangeLabel(range.formatLabel());
        data.setTotalFleet(cars.size());
        data.setAvailableCars(cars.stream().filter(c -> c.getStatus() == CarStatus.AVAILABLE).count());
        data.setActiveRentals(rentals.stream().filter(r -> r.getRentalStatus() == RentalStatus.ACTIVE).count());

        populateRevenue(data, rentals, range, period);
        populateFleetUtilization(data, cars, rentals, range, today);
        populateRentalOverdue(data);
        data.setTotalBookings(countBookingsInPeriod(rentals, range));
        populateCustomers(data, rentals, range);
        populateOperatingCosts(data, range);

        return data;
    }

    private void populateOperatingCosts(DashboardData data, DateRange range) {
        EmployeePaymentPeriodFilter filter = EmployeePaymentPeriodFilter.fromDateRange(
                range,
                data.getRangeLabel());
        data.setPeriodFromIso(range.getStart().toString());
        data.setPeriodToIso(range.getEnd().toString());
        data.setTotalEmployeePayments(employeePaymentService.totalInPeriod(null, filter));
        data.setEmployeePaymentCount(employeePaymentService.countInPeriod(filter));
        data.setTotalOfficeExpenses(officeExpenseService.totalInPeriod(null, filter));
        data.setOfficeExpenseRecordCount(officeExpenseService.countInPeriod(filter));
    }

    private void populateRevenue(DashboardData data, List<Rental> rentals, DateRange range, DashboardPeriod period) {
        Map<LocalDate, BigDecimal> daily = new TreeMap<LocalDate, BigDecimal>();
        Map<String, BigDecimal> weekly = new LinkedHashMap<String, BigDecimal>();
        Map<String, BigDecimal> monthly = new LinkedHashMap<String, BigDecimal>();
        Map<VehicleType, BigDecimal> byType = new EnumMap<VehicleType, BigDecimal>(VehicleType.class);
        Map<Long, BigDecimal> byCar = new HashMap<Long, BigDecimal>();

        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal outstanding = BigDecimal.ZERO;
        int completedCount = 0;
        BigDecimal completedSum = BigDecimal.ZERO;

        for (Rental rental : rentals) {
            if (rental.getRentalStatus() == RentalStatus.CANCELLED
                || rental.getEmployeeHire()) {
                continue;
            }

            LocalDate revenueDate = revenueDate(rental);
            BigDecimal amount = revenueAmount(rental);

            if (rental.getRentalStatus() == RentalStatus.ACTIVE) {
                outstanding = outstanding.add(amount);
            }

            if (revenueDate != null && range.contains(revenueDate)) {
                if (rental.getRentalStatus() == RentalStatus.COMPLETED) {
                    totalRevenue = totalRevenue.add(amount);
                    completedCount++;
                    completedSum = completedSum.add(amount);

                    daily.put(revenueDate, sum(daily.get(revenueDate), amount));

                    String weekKey = weekLabel(revenueDate);
                    weekly.put(weekKey, sum(weekly.get(weekKey), amount));

                    String monthKey = revenueDate.format(MONTH_FMT);
                    monthly.put(monthKey, sum(monthly.get(monthKey), amount));

                    VehicleType type = rental.getCar().getVehicleType();
                    byType.put(type, sum(byType.get(type), amount));

                    Long carId = rental.getCar().getId();
                    byCar.put(carId, sum(byCar.get(carId), amount));
                }
            }
        }

        data.setTotalRevenue(totalRevenue);
        data.setOutstandingBalances(outstanding);
        data.setAverageBookingValue(completedCount == 0
                ? BigDecimal.ZERO
                : completedSum.divide(BigDecimal.valueOf(completedCount), 2, RoundingMode.HALF_UP));

        data.setMostProfitableVehicle(mostProfitableVehicle(carsById(rentals), byCar));
        data.setHighestEarningMonth(highestMonth(monthly));

        DashboardPeriod.RevenueGranularity granularity = period.getRevenueGranularity();
        data.setRevenueChartTitle(granularity.getChartTitle());
        switch (granularity) {
            case DAILY:
                data.setRevenueChart(toChartPoints(dailyLabels(range), daily, range));
                break;
            case WEEKLY:
                data.setRevenueChart(toChartPointsFromMap(weekly));
                break;
            case MONTHLY:
            default:
                data.setRevenueChart(toChartPointsFromMap(monthly));
                break;
        }
        data.setRevenueByVehicleType(enumChartPoints(byType));
    }

    private void populateFleetUtilization(
            DashboardData data,
            List<Car> cars,
            List<Rental> rentals,
            DateRange range,
            LocalDate today) {

        long periodDays = range.dayCount();
        int utilTotal = 0;
        List<DashboardData.VehicleUtilizationRow> utilizationRows = new ArrayList<DashboardData.VehicleUtilizationRow>();
        List<DashboardData.NamedValueRow> idle = new ArrayList<DashboardData.NamedValueRow>();
        List<DashboardData.NamedValueRow> frequent = new ArrayList<DashboardData.NamedValueRow>();
        List<DashboardData.NamedValueRow> low = new ArrayList<DashboardData.NamedValueRow>();
        List<DashboardData.NamedValueRow> rentedNow = new ArrayList<DashboardData.NamedValueRow>();
        List<DashboardData.NamedValueRow> serviceDueSoon = new ArrayList<DashboardData.NamedValueRow>();
        List<DashboardData.NamedValueRow> serviceOverdue = new ArrayList<DashboardData.NamedValueRow>();

        for (Car car : cars) {
            int rentedDays = rentedDaysForCar(car, rentals, range);
            int utilization = periodDays <= 0 ? 0 : (int) Math.min(100, Math.round((rentedDays * 100.0) / periodDays));
            int bookingCount = bookingCountForCar(car, rentals, range);

            String label = vehicleLabel(car);
            utilizationRows.add(new DashboardData.VehicleUtilizationRow(
                    label,
                    car.getRegistrationNumber(),
                    utilization,
                    bookingCount + " bookings in period"));
            utilTotal += utilization;

            if (car.getStatus() == CarStatus.AVAILABLE && utilization < 20) {
                idle.add(new DashboardData.NamedValueRow(
                        label, car.getRegistrationNumber(), null, BigDecimal.valueOf(utilization)));
            }
            if (bookingCount >= 2) {
                frequent.add(new DashboardData.NamedValueRow(
                        label, car.getRegistrationNumber(), bookingCount + " hires", BigDecimal.valueOf(bookingCount)));
            }
            if (utilization < applicationParameterService.passInt(LOW_UTILIZATION_THRESHOLD.name())) {
                low.add(new DashboardData.NamedValueRow(
                        label, car.getRegistrationNumber(), utilization + "% utilization", BigDecimal.valueOf(utilization)));
            }
            if (car.getStatus() == CarStatus.UNAVAILABLE) {
                rentedNow.add(new DashboardData.NamedValueRow(
                        label, car.getRegistrationNumber(), null, BigDecimal.ZERO));
            }
            if (FleetServiceAlertService.isServiceOverdue(car)) {
                serviceOverdue.add(new DashboardData.NamedValueRow(
                        label,
                        car.getRegistrationNumber(),
                        car.getMileageKm() + " / " + car.getNextServiceKm() + " km (overdue)",
                        BigDecimal.valueOf(car.getMileageKm() - car.getNextServiceKm())));
            } else if (FleetServiceAlertService.isServiceDueSoon(car)) {
                serviceDueSoon.add(new DashboardData.NamedValueRow(
                        label,
                        car.getRegistrationNumber(),
                        car.getMileageKm() + " / " + car.getNextServiceKm() + " km",
                        BigDecimal.ZERO));
            }
        }

        sortByValueDesc(frequent);
        sortByValueAsc(low);
        sortUtilizationDesc(utilizationRows);

        data.setAverageFleetUtilization(cars.isEmpty() ? 0 : utilTotal / cars.size());
        data.setVehicleUtilization(utilizationRows);
        data.setIdleVehicles(limit(idle, 5));
        data.setFrequentlyRented(limit(frequent, 5));
        data.setLowPerformingVehicles(limit(low, 5));
        data.setCurrentlyRented(rentedNow);
        sortByValueDesc(serviceOverdue);
        data.setNearingService(limit(serviceDueSoon, 5));
        data.setServiceOverdue(limit(serviceOverdue, 5));
    }

    private void populateRentalOverdue(DashboardData data) {
        List<RentalOverdueAlert> alerts = rentalService.findOverdueAlerts();
        if (alerts.size() > 5) {
            alerts = new ArrayList<RentalOverdueAlert>(alerts.subList(0, 5));
        }
        data.setRentalOverdue(alerts);
    }

    private long countBookingsInPeriod(List<Rental> rentals, DateRange range) {
        long total = 0;
        for (Rental rental : rentals) {
            if (range.contains(rental.getHireDate())
                    && rental.getRentalStatus() != RentalStatus.CANCELLED) {
                total++;
            }
        }
        return total;
    }

    private void populateCustomers(DashboardData data, List<Rental> rentals, DateRange range) {
        Map<String, List<Rental>> byContact = new HashMap<String, List<Rental>>();
        Map<String, LocalDate> firstSeen = new HashMap<String, LocalDate>();
        Map<String, BigDecimal> revenueByNic = new HashMap<String, BigDecimal>();
        Map<String, String> nameByNic = new HashMap<String, String>();
        Map<String, Integer> rentalsByNic = new HashMap<String, Integer>();

        long complaints = 0;
        int durationTotal = 0;
        int durationCount = 0;

        for (Rental rental : rentals) {
            if (rental.getRentalStatus() == RentalStatus.CANCELLED) {
                continue;
            }
            String contact = normalizeContact(rental.getCustomerContact());
            if (!byContact.containsKey(contact)) {
                byContact.put(contact, new ArrayList<Rental>());
            }
            byContact.get(contact).add(rental);

            LocalDate hire = rental.getHireDate();
            if (!firstSeen.containsKey(contact) || hire.isBefore(firstSeen.get(contact))) {
                firstSeen.put(contact, hire);
            }

            if (rental.getRentalStatus() == RentalStatus.COMPLETED && range.contains(revenueDate(rental))) {
                String nic = normalizeNic(rental.getCustomerIdNumber());
                if (!nic.isEmpty()) {
                    revenueByNic.put(nic, sum(revenueByNic.get(nic), revenueAmount(rental)));
                    nameByNic.put(nic, rental.getCustomerName());
                    rentalsByNic.put(nic, rentalsByNic.getOrDefault(nic, 0) + 1);
                }
            }

            if (Boolean.TRUE.equals(rental.getCustomerComplaint()) && range.contains(rental.getHireDate())) {
                complaints++;
            }

            if (range.contains(rental.getHireDate())) {
                durationTotal += rental.getNumberOfDays();
                durationCount++;
            }
        }

        long repeat = 0;
        long newInPeriod = 0;
        for (Map.Entry<String, List<Rental>> entry : byContact.entrySet()) {
            if (entry.getValue().size() > 1) {
                repeat++;
            }
            if (range.contains(firstSeen.get(entry.getKey()))) {
                newInPeriod++;
            }
        }

        long uniqueCustomers = byContact.size();
        BigDecimal retention = uniqueCustomers == 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(repeat * 100.0 / uniqueCustomers).setScale(1, RoundingMode.HALF_UP);

        data.setRepeatCustomers(repeat);
        data.setNewCustomers(newInPeriod);
        data.setBlacklistedCustomers(blacklistedCustomerRepository.count());
        data.setCustomerComplaints(complaints);
        data.setCustomerRetentionRate(retention);
        data.setAverageRentalDuration(durationCount == 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(durationTotal * 1.0 / durationCount).setScale(1, RoundingMode.HALF_UP));
        data.setTopCustomers(topCustomersByNic(nameByNic, revenueByNic, rentalsByNic));
    }

    private List<DashboardData.NamedValueRow> topCustomersByNic(
            Map<String, String> namesByNic,
            Map<String, BigDecimal> revenueByNic,
            Map<String, Integer> rentalsByNic) {
        List<DashboardData.NamedValueRow> rows = new ArrayList<DashboardData.NamedValueRow>();
        for (Map.Entry<String, BigDecimal> entry : revenueByNic.entrySet()) {
            String nic = entry.getKey();
            rows.add(new DashboardData.NamedValueRow(
                    namesByNic.get(nic),
                    nic,
                    entry.getValue(),
                    rentalsByNic.getOrDefault(nic, 0)));
        }
        sortByValueDesc(rows);
        return limit(rows, applicationParameterService.passInt(TOP_CUSTOMERS_LIMIT.name()));
    }

    private int rentedDaysForCar(Car car, List<Rental> rentals, DateRange range) {
        int days = 0;
        for (Rental rental : rentals) {
            if (!rental.getCar().getId().equals(car.getId())) {
                continue;
            }
            LocalDate start = rental.getHireDate().isBefore(range.getStart()) ? range.getStart() : rental.getPickupDate();
            LocalDate end = effectiveReturnDate(rental).isAfter(range.getEnd()) ? range.getEnd() : effectiveReturnDate(rental);
            if (!start.isAfter(end)) {
                  days += (int) ChronoUnit.DAYS.between(start, end);
            }
        }
        return days;
    }

    private int bookingCountForCar(Car car, List<Rental> rentals, DateRange range) {
        int count = 0;
        for (Rental rental : rentals) {
            if (rental.getCar().getId().equals(car.getId())
                    && rental.getRentalStatus() != RentalStatus.CANCELLED
                    && range.contains(rental.getHireDate())) {
                count++;
            }
        }
        return count;
    }

    private LocalDate revenueDate(Rental rental) {
        if (rental.getRentalStatus() == RentalStatus.COMPLETED) {
            return effectiveReturnDate(rental);
        }
        return null;
    }

    private LocalDate effectiveReturnDate(Rental rental) {
        if (rental.getReturnDate() != null) {
            return rental.getReturnDate();
        }
        return rental.getHireDate().plusDays(rental.getNumberOfDays());
    }

    private BigDecimal revenueAmount(Rental rental) {
        if (rental.getTotalPrice() != null) {
            return rental.getTotalPrice();
        }
        return getOutStandingBalanceByTripType(rental);
    }

    private String vehicleLabel(Car car) {
        if (car.getModelName() != null && !car.getModelName().trim().isEmpty()) {
            return car.getModelName();
        }
        return car.getRegistrationNumber();
    }

    private Map<Long, Car> carsById(List<Rental> rentals) {
        Map<Long, Car> map = new HashMap<Long, Car>();
        for (Rental rental : rentals) {
            map.put(rental.getCar().getId(), rental.getCar());
        }
        return map;
    }

    private String mostProfitableVehicle(Map<Long, Car> cars, Map<Long, BigDecimal> byCar) {
        Long bestId = null;
        BigDecimal best = BigDecimal.ZERO;
        for (Map.Entry<Long, BigDecimal> entry : byCar.entrySet()) {
            if (entry.getValue().compareTo(best) > 0) {
                best = entry.getValue();
                bestId = entry.getKey();
            }
        }
        if (bestId == null || cars.get(bestId) == null) {
            return "—";
        }
        return vehicleLabel(cars.get(bestId));
    }

    private String highestMonth(Map<String, BigDecimal> monthly) {
        String bestMonth = "—";
        BigDecimal best = BigDecimal.ZERO;
        for (Map.Entry<String, BigDecimal> entry : monthly.entrySet()) {
            if (entry.getValue().compareTo(best) > 0) {
                best = entry.getValue();
                bestMonth = entry.getKey();
            }
        }
        return bestMonth;
    }

    private String weekLabel(LocalDate date) {
        LocalDate weekStart = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        return "Wk " + weekStart.format(DAY_FMT);
    }

    private List<LocalDate> dailyLabels(DateRange range) {
        List<LocalDate> labels = new ArrayList<LocalDate>();
        LocalDate cursor = range.getStart();
        while (!cursor.isAfter(range.getEnd())) {
            labels.add(cursor);
            cursor = cursor.plusDays(1);
        }
        return labels;
    }

    private List<DashboardData.ChartPoint> toChartPoints(
            List<LocalDate> days,
            Map<LocalDate, BigDecimal> values,
            DateRange range) {
        List<DashboardData.ChartPoint> points = new ArrayList<DashboardData.ChartPoint>();
        for (LocalDate day : days) {
            BigDecimal value = values.containsKey(day) ? values.get(day) : BigDecimal.ZERO;
            points.add(new DashboardData.ChartPoint(day.format(DAY_FMT), value));
        }
        if (points.size() > 31) {
            return downsampleDaily(points);
        }
        return points;
    }

    private List<DashboardData.ChartPoint> downsampleDaily(List<DashboardData.ChartPoint> points) {
        List<DashboardData.ChartPoint> sampled = new ArrayList<DashboardData.ChartPoint>();
        int step = Math.max(1, points.size() / 15);
        for (int i = 0; i < points.size(); i += step) {
            sampled.add(points.get(i));
        }
        if (!points.isEmpty() && sampled.get(sampled.size() - 1) != points.get(points.size() - 1)) {
            sampled.add(points.get(points.size() - 1));
        }
        return sampled;
    }

    private List<DashboardData.ChartPoint> toChartPointsFromMap(Map<String, BigDecimal> values) {
        List<DashboardData.ChartPoint> points = new ArrayList<DashboardData.ChartPoint>();
        for (Map.Entry<String, BigDecimal> entry : values.entrySet()) {
            points.add(new DashboardData.ChartPoint(entry.getKey(), entry.getValue()));
        }
        return points;
    }

    private <E extends Enum<E>> List<DashboardData.ChartPoint> enumChartPoints(Map<E, BigDecimal> values) {
        List<DashboardData.ChartPoint> points = new ArrayList<DashboardData.ChartPoint>();
        for (Map.Entry<E, BigDecimal> entry : values.entrySet()) {
            points.add(new DashboardData.ChartPoint(formatEnum(entry.getKey()), entry.getValue()));
        }
        return points;
    }

    private List<DashboardData.ChartPoint> mapChartPoints(Map<String, BigDecimal> values) {
        List<DashboardData.ChartPoint> points = new ArrayList<DashboardData.ChartPoint>();
        for (Map.Entry<String, BigDecimal> entry : values.entrySet()) {
            points.add(new DashboardData.ChartPoint(entry.getKey(), entry.getValue()));
        }
        return points;
    }

    private BigDecimal sum(BigDecimal current, BigDecimal add) {
        if (add == null) {
            return current == null ? BigDecimal.ZERO : current;
        }
        return current == null ? add : current.add(add);
    }

    private String normalizeContact(String contact) {
        return contact == null ? "" : contact.replaceAll("\\s+", "").toLowerCase(Locale.ENGLISH);
    }

    private String normalizeNic(String nic) {
        if (nic == null) {
            return "";
        }
        return nic.replaceAll("\\s+", "").toUpperCase(Locale.ENGLISH);
    }

    private String formatEnum(Enum<?> value) {
        String raw = value.name().replace('_', ' ').toLowerCase(Locale.ENGLISH);
        return capitalize(raw);
    }

    private String capitalize(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        return value.substring(0, 1).toUpperCase(Locale.ENGLISH) + value.substring(1);
    }

    private void sortByValueDesc(List<DashboardData.NamedValueRow> rows) {
        Collections.sort(rows, new Comparator<DashboardData.NamedValueRow>() {
            @Override
            public int compare(DashboardData.NamedValueRow a, DashboardData.NamedValueRow b) {
                return b.getValue().compareTo(a.getValue());
            }
        });
    }

    private void sortByValueAsc(List<DashboardData.NamedValueRow> rows) {
        Collections.sort(rows, new Comparator<DashboardData.NamedValueRow>() {
            @Override
            public int compare(DashboardData.NamedValueRow a, DashboardData.NamedValueRow b) {
                return a.getValue().compareTo(b.getValue());
            }
        });
    }

    private void sortUtilizationDesc(List<DashboardData.VehicleUtilizationRow> rows) {
        Collections.sort(rows, new Comparator<DashboardData.VehicleUtilizationRow>() {
            @Override
            public int compare(DashboardData.VehicleUtilizationRow a, DashboardData.VehicleUtilizationRow b) {
                return Integer.compare(b.getUtilizationPercent(), a.getUtilizationPercent());
            }
        });
    }

    private List<DashboardData.NamedValueRow> limit(List<DashboardData.NamedValueRow> rows, int max) {
        if (rows.size() <= max) {
            return rows;
        }
        return new ArrayList<DashboardData.NamedValueRow>(rows.subList(0, max));
    }

    public static BigDecimal getOutStandingBalanceByTripType(Rental rental) {
        BigDecimal renalPricePerDay;
        switch (rental.getHireType()) {
            case PER_MONTH :
                renalPricePerDay =  rental.getCar().getRentalPricePerMonth();
                break;
            case PER_WEEK :
                renalPricePerDay = rental.getCar().getRentalPricePerWeek();
                break;
            default :
                renalPricePerDay = rental.getCar().getRentalPricePerDay();
        }
        return renalPricePerDay.multiply(BigDecimal.valueOf(rental.getNumberOfDays()));
    }
}
