package com.carrental.car;

import com.carrental.model.Car;
import com.carrental.model.MaintenanceRecord;
import com.carrental.model.Rental;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CarDetailData {

    private Car car;
    private String calendarMonthLabel;
    private String calendarMonthParam;
    private String prevMonthParam;
    private String nextMonthParam;
    private List<CalendarCell> calendarCells = new ArrayList<CalendarCell>();
    private List<Rental> rentals = new ArrayList<Rental>();
    private List<MaintenanceRecord> maintenanceRecords = new ArrayList<MaintenanceRecord>();
    private BigDecimal totalIncome = BigDecimal.ZERO;
    private BigDecimal totalMaintenanceCost = BigDecimal.ZERO;
    private BigDecimal lifetimeIncome = BigDecimal.ZERO;
    private BigDecimal lifetimeMaintenance = BigDecimal.ZERO;
    private List<ChartPoint> incomeChart = new ArrayList<ChartPoint>();
    private List<ChartPoint> expenseChart = new ArrayList<ChartPoint>();

    public Car getCar() {
        return car;
    }

    public void setCar(Car car) {
        this.car = car;
    }

    public String getCalendarMonthLabel() {
        return calendarMonthLabel;
    }

    public void setCalendarMonthLabel(String calendarMonthLabel) {
        this.calendarMonthLabel = calendarMonthLabel;
    }

    public String getCalendarMonthParam() {
        return calendarMonthParam;
    }

    public void setCalendarMonthParam(String calendarMonthParam) {
        this.calendarMonthParam = calendarMonthParam;
    }

    public String getPrevMonthParam() {
        return prevMonthParam;
    }

    public void setPrevMonthParam(String prevMonthParam) {
        this.prevMonthParam = prevMonthParam;
    }

    public String getNextMonthParam() {
        return nextMonthParam;
    }

    public void setNextMonthParam(String nextMonthParam) {
        this.nextMonthParam = nextMonthParam;
    }

    public List<CalendarCell> getCalendarCells() {
        return calendarCells;
    }

    public void setCalendarCells(List<CalendarCell> calendarCells) {
        this.calendarCells = calendarCells;
    }

    public List<Rental> getRentals() {
        return rentals;
    }

    public void setRentals(List<Rental> rentals) {
        this.rentals = rentals;
    }

    public List<MaintenanceRecord> getMaintenanceRecords() {
        return maintenanceRecords;
    }

    public void setMaintenanceRecords(List<MaintenanceRecord> maintenanceRecords) {
        this.maintenanceRecords = maintenanceRecords;
    }

    public BigDecimal getTotalIncome() {
        return totalIncome;
    }

    public void setTotalIncome(BigDecimal totalIncome) {
        this.totalIncome = totalIncome;
    }

    public BigDecimal getTotalMaintenanceCost() {
        return totalMaintenanceCost;
    }

    public void setTotalMaintenanceCost(BigDecimal totalMaintenanceCost) {
        this.totalMaintenanceCost = totalMaintenanceCost;
    }

    public BigDecimal getLifetimeIncome() {
        return lifetimeIncome;
    }

    public void setLifetimeIncome(BigDecimal lifetimeIncome) {
        this.lifetimeIncome = lifetimeIncome;
    }

    public BigDecimal getLifetimeMaintenance() {
        return lifetimeMaintenance;
    }

    public void setLifetimeMaintenance(BigDecimal lifetimeMaintenance) {
        this.lifetimeMaintenance = lifetimeMaintenance;
    }

    public List<ChartPoint> getIncomeChart() {
        return incomeChart;
    }

    public void setIncomeChart(List<ChartPoint> incomeChart) {
        this.incomeChart = incomeChart;
    }

    public List<ChartPoint> getExpenseChart() {
        return expenseChart;
    }

    public void setExpenseChart(List<ChartPoint> expenseChart) {
        this.expenseChart = expenseChart;
    }

    public static class CalendarCell {
        private boolean blank;
        private Integer day;
        private String isoDate;
        private String status = "AVAILABLE";
        private boolean today;
        private int rentalCount;
        private int maintenanceCount;

        public boolean isBlank() {
            return blank;
        }

        public void setBlank(boolean blank) {
            this.blank = blank;
        }

        public Integer getDay() {
            return day;
        }

        public void setDay(Integer day) {
            this.day = day;
        }

        public String getIsoDate() {
            return isoDate;
        }

        public void setIsoDate(String isoDate) {
            this.isoDate = isoDate;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public boolean isToday() {
            return today;
        }

        public void setToday(boolean today) {
            this.today = today;
        }

        public int getRentalCount() {
            return rentalCount;
        }

        public void setRentalCount(int rentalCount) {
            this.rentalCount = rentalCount;
        }

        public int getMaintenanceCount() {
            return maintenanceCount;
        }

        public void setMaintenanceCount(int maintenanceCount) {
            this.maintenanceCount = maintenanceCount;
        }
    }

    public static class ChartPoint {
        private String label;
        private BigDecimal value = BigDecimal.ZERO;

        public ChartPoint() {
        }

        public ChartPoint(String label, BigDecimal value) {
            this.label = label;
            this.value = value;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public BigDecimal getValue() {
            return value;
        }

        public void setValue(BigDecimal value) {
            this.value = value;
        }
    }
}
