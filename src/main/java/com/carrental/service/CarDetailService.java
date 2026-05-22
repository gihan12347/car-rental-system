package com.carrental.service;

import com.carrental.car.CarDetailData;
import com.carrental.car.CarDetailData.CalendarCell;
import com.carrental.car.CarDetailData.ChartPoint;
import com.carrental.model.Car;
import com.carrental.model.MaintenanceRecord;
import com.carrental.model.Rental;
import com.carrental.model.RentalStatus;
import com.carrental.repository.MaintenanceRecordRepository;
import com.carrental.repository.RentalRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class CarDetailService {

    private static final DateTimeFormatter MONTH_PARAM = DateTimeFormatter.ofPattern("yyyy-MM");
    private static final DateTimeFormatter MONTH_LABEL = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH);
    private static final int CHART_MONTHS = 6;

    private final CarService carService;
    private final RentalRepository rentalRepository;
    private final MaintenanceRecordRepository maintenanceRecordRepository;

    public CarDetailService(
            CarService carService,
            RentalRepository rentalRepository,
            MaintenanceRecordRepository maintenanceRecordRepository) {
        this.carService = carService;
        this.rentalRepository = rentalRepository;
        this.maintenanceRecordRepository = maintenanceRecordRepository;
    }

    public CarDetailData build(Long carId, String monthParam) {
        Car car = carService.getById(carId);
        YearMonth month = parseMonth(monthParam);
        List<Rental> rentals = rentalRepository.findByCarIdOrderByPickupDateDesc(carId);
        List<MaintenanceRecord> maintenance = maintenanceRecordRepository.findByCarIdOrderByMaintenanceDateDesc(carId);

        CarDetailData data = new CarDetailData();
        data.setCar(car);
        data.setRentals(rentals);
        data.setMaintenanceRecords(maintenance);
        data.setCalendarMonthParam(month.format(MONTH_PARAM));
        data.setCalendarMonthLabel(month.format(MONTH_LABEL));
        data.setPrevMonthParam(month.minusMonths(1).format(MONTH_PARAM));
        data.setNextMonthParam(month.plusMonths(1).format(MONTH_PARAM));
        data.setCalendarCells(buildCalendar(month, rentals, maintenance));

        data.setLifetimeIncome(sumLifetimeIncome(rentals, car));
        data.setLifetimeMaintenance(sumMaintenance(maintenance));
        data.setTotalIncome(sumIncomeInMonth(rentals, car, month));
        data.setTotalMaintenanceCost(sumMaintenanceInMonth(maintenance, month));
        data.setIncomeChart(buildIncomeChart(rentals, car));
        data.setExpenseChart(buildExpenseChart(maintenance));
        return data;
    }

    public static YearMonth parseMonth(String monthParam) {
        if (monthParam == null || monthParam.trim().isEmpty()) {
            return YearMonth.now();
        }
        try {
            return YearMonth.parse(monthParam.trim(), MONTH_PARAM);
        } catch (DateTimeParseException e) {
            return YearMonth.now();
        }
    }

    private List<CalendarCell> buildCalendar(
            YearMonth month,
            List<Rental> rentals,
            List<MaintenanceRecord> maintenance) {
        List<CalendarCell> cells = new ArrayList<CalendarCell>();
        LocalDate first = month.atDay(1);
        int leadingBlanks = first.getDayOfWeek().getValue() % 7;
        LocalDate today = LocalDate.now();

        for (int i = 0; i < leadingBlanks; i++) {
            CalendarCell blank = new CalendarCell();
            blank.setBlank(true);
            cells.add(blank);
        }

        int daysInMonth = month.lengthOfMonth();
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = month.atDay(day);
            CalendarCell cell = new CalendarCell();
            cell.setDay(day);
            cell.setToday(date.equals(today));
            cell.setStatus(resolveDayStatus(date, rentals, maintenance));
            cells.add(cell);
        }
        return cells;
    }

    private String resolveDayStatus(
            LocalDate date,
            List<Rental> rentals,
            List<MaintenanceRecord> maintenance) {
        boolean rented = false;
        boolean service = false;

        for (Rental rental : rentals) {
            if (rental.getRentalStatus() == RentalStatus.CANCELLED) {
                continue;
            }
            LocalDate start = RentalPeriodHelper.startDate(rental);
            LocalDate end = RentalPeriodHelper.endDate(rental);
            if (RentalPeriodHelper.includesToday(start, end, date)) {
                rented = true;
                break;
            }
        }

        for (MaintenanceRecord record : maintenance) {
            if (date.equals(record.getMaintenanceDate())) {
                service = true;
                break;
            }
        }

        if (rented && service) {
            return "RENTED_MAINTENANCE";
        }
        if (rented) {
            return "RENTED";
        }
        if (service) {
            return "MAINTENANCE";
        }
        return "AVAILABLE";
    }

    private List<ChartPoint> buildIncomeChart(List<Rental> rentals, Car car) {
        List<ChartPoint> points = new ArrayList<ChartPoint>();
        YearMonth end = YearMonth.now();
        for (int i = CHART_MONTHS - 1; i >= 0; i--) {
            YearMonth month = end.minusMonths(i);
            BigDecimal amount = sumIncomeInMonth(rentals, car, month);
            points.add(new ChartPoint(month.format(DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH)), amount));
        }
        return points;
    }

    private List<ChartPoint> buildExpenseChart(List<MaintenanceRecord> maintenance) {
        List<ChartPoint> points = new ArrayList<ChartPoint>();
        YearMonth end = YearMonth.now();
        for (int i = CHART_MONTHS - 1; i >= 0; i--) {
            YearMonth month = end.minusMonths(i);
            BigDecimal amount = sumMaintenanceInMonth(maintenance, month);
            points.add(new ChartPoint(month.format(DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH)), amount));
        }
        return points;
    }

    private BigDecimal sumLifetimeIncome(List<Rental> rentals, Car car) {
        BigDecimal total = BigDecimal.ZERO;
        for (Rental rental : rentals) {
            if (rental.getRentalStatus() == RentalStatus.COMPLETED) {
                total = total.add(rentalIncome(rental, car));
            }
        }
        return total;
    }

    private BigDecimal sumIncomeInMonth(List<Rental> rentals, Car car, YearMonth month) {
        BigDecimal total = BigDecimal.ZERO;
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();
        for (Rental rental : rentals) {
            if (rental.getRentalStatus() != RentalStatus.COMPLETED) {
                continue;
            }
            LocalDate booked = incomeDate(rental);
            if (booked != null && !booked.isBefore(start) && !booked.isAfter(end)) {
                total = total.add(rentalIncome(rental, car));
            }
        }
        return total;
    }

    private LocalDate incomeDate(Rental rental) {
        if (rental.getCompletedDate() != null) {
            return rental.getCompletedDate();
        }
        if (rental.getReturnDate() != null) {
            return rental.getReturnDate();
        }
        return rental.getHireDate();
    }

    private BigDecimal rentalIncome(Rental rental, Car car) {
        if (rental.getTotalPrice() != null && rental.getTotalPrice().compareTo(BigDecimal.ZERO) > 0) {
            return rental.getTotalPrice();
        }
        return car.getRentalPricePerDay()
                .multiply(BigDecimal.valueOf(Math.max(1, rental.getNumberOfDays())))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal sumMaintenance(List<MaintenanceRecord> records) {
        BigDecimal total = BigDecimal.ZERO;
        for (MaintenanceRecord record : records) {
            if (record.getCost() != null) {
                total = total.add(record.getCost());
            }
        }
        return total;
    }

    private BigDecimal sumMaintenanceInMonth(List<MaintenanceRecord> records, YearMonth month) {
        BigDecimal total = BigDecimal.ZERO;
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();
        for (MaintenanceRecord record : records) {
            LocalDate d = record.getMaintenanceDate();
            if (d != null && !d.isBefore(start) && !d.isAfter(end) && record.getCost() != null) {
                total = total.add(record.getCost());
            }
        }
        return total;
    }
}
