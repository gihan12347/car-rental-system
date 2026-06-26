package com.carrental.dashboard;

import com.carrental.web.dto.RentalOverdueAlert;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DashboardData {

    private String periodLabel;
    private String rangeLabel;

    private long totalFleet;
    private long availableCars;
    private long activeRentals;
    private int averageFleetUtilization;
    private BigDecimal totalRevenue = BigDecimal.ZERO;
    private BigDecimal outstandingBalances = BigDecimal.ZERO;
    private BigDecimal averageBookingValue = BigDecimal.ZERO;
    private String mostProfitableVehicle = "—";
    private String highestEarningMonth = "—";

    private long totalBookings;

    private BigDecimal totalEmployeePayments = BigDecimal.ZERO;
    private long employeePaymentCount;
    private BigDecimal totalOfficeExpenses = BigDecimal.ZERO;
    private long officeExpenseRecordCount;
    private String periodFromIso;
    private String periodToIso;

    private long repeatCustomers;
    private long newCustomers;
    private long blacklistedCustomers;
    private long customerComplaints;
    private BigDecimal customerRetentionRate = BigDecimal.ZERO;
    private BigDecimal averageRentalDuration = BigDecimal.ZERO;

    private String revenueChartTitle = "Revenue";
    private List<ChartPoint> revenueChart = new ArrayList<ChartPoint>();
    private List<ChartPoint> revenueByVehicleType = new ArrayList<ChartPoint>();

    private List<VehicleUtilizationRow> vehicleUtilization = new ArrayList<VehicleUtilizationRow>();
    private List<NamedValueRow> idleVehicles = new ArrayList<NamedValueRow>();
    private List<NamedValueRow> frequentlyRented = new ArrayList<NamedValueRow>();
    private List<NamedValueRow> lowPerformingVehicles = new ArrayList<NamedValueRow>();
    private List<NamedValueRow> currentlyRented = new ArrayList<NamedValueRow>();
    private List<NamedValueRow> nearingService = new ArrayList<NamedValueRow>();
    private List<NamedValueRow> serviceOverdue = new ArrayList<NamedValueRow>();
    private List<RentalOverdueAlert> rentalOverdue = new ArrayList<RentalOverdueAlert>();
    private List<NamedValueRow> topCustomers = new ArrayList<NamedValueRow>();

    public String getPeriodLabel() {
        return periodLabel;
    }

    public void setPeriodLabel(String periodLabel) {
        this.periodLabel = periodLabel;
    }

    public String getRangeLabel() {
        return rangeLabel;
    }

    public void setRangeLabel(String rangeLabel) {
        this.rangeLabel = rangeLabel;
    }

    public long getTotalFleet() {
        return totalFleet;
    }

    public void setTotalFleet(long totalFleet) {
        this.totalFleet = totalFleet;
    }

    public long getAvailableCars() {
        return availableCars;
    }

    public void setAvailableCars(long availableCars) {
        this.availableCars = availableCars;
    }

    public long getActiveRentals() {
        return activeRentals;
    }

    public void setActiveRentals(long activeRentals) {
        this.activeRentals = activeRentals;
    }

    public int getAverageFleetUtilization() {
        return averageFleetUtilization;
    }

    public void setAverageFleetUtilization(int averageFleetUtilization) {
        this.averageFleetUtilization = averageFleetUtilization;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public BigDecimal getOutstandingBalances() {
        return outstandingBalances;
    }

    public void setOutstandingBalances(BigDecimal outstandingBalances) {
        this.outstandingBalances = outstandingBalances;
    }

    public BigDecimal getAverageBookingValue() {
        return averageBookingValue;
    }

    public void setAverageBookingValue(BigDecimal averageBookingValue) {
        this.averageBookingValue = averageBookingValue;
    }

    public String getMostProfitableVehicle() {
        return mostProfitableVehicle;
    }

    public void setMostProfitableVehicle(String mostProfitableVehicle) {
        this.mostProfitableVehicle = mostProfitableVehicle;
    }

    public String getHighestEarningMonth() {
        return highestEarningMonth;
    }

    public void setHighestEarningMonth(String highestEarningMonth) {
        this.highestEarningMonth = highestEarningMonth;
    }

    public long getTotalBookings() {
        return totalBookings;
    }

    public void setTotalBookings(long totalBookings) {
        this.totalBookings = totalBookings;
    }

    public BigDecimal getTotalEmployeePayments() {
        return totalEmployeePayments;
    }

    public void setTotalEmployeePayments(BigDecimal totalEmployeePayments) {
        this.totalEmployeePayments = totalEmployeePayments;
    }

    public long getEmployeePaymentCount() {
        return employeePaymentCount;
    }

    public void setEmployeePaymentCount(long employeePaymentCount) {
        this.employeePaymentCount = employeePaymentCount;
    }

    public BigDecimal getTotalOfficeExpenses() {
        return totalOfficeExpenses;
    }

    public void setTotalOfficeExpenses(BigDecimal totalOfficeExpenses) {
        this.totalOfficeExpenses = totalOfficeExpenses;
    }

    public long getOfficeExpenseRecordCount() {
        return officeExpenseRecordCount;
    }

    public void setOfficeExpenseRecordCount(long officeExpenseRecordCount) {
        this.officeExpenseRecordCount = officeExpenseRecordCount;
    }

    public String getPeriodFromIso() {
        return periodFromIso;
    }

    public void setPeriodFromIso(String periodFromIso) {
        this.periodFromIso = periodFromIso;
    }

    public String getPeriodToIso() {
        return periodToIso;
    }

    public void setPeriodToIso(String periodToIso) {
        this.periodToIso = periodToIso;
    }

    public long getRepeatCustomers() {
        return repeatCustomers;
    }

    public void setRepeatCustomers(long repeatCustomers) {
        this.repeatCustomers = repeatCustomers;
    }

    public long getNewCustomers() {
        return newCustomers;
    }

    public void setNewCustomers(long newCustomers) {
        this.newCustomers = newCustomers;
    }

    public long getBlacklistedCustomers() {
        return blacklistedCustomers;
    }

    public void setBlacklistedCustomers(long blacklistedCustomers) {
        this.blacklistedCustomers = blacklistedCustomers;
    }

    public long getCustomerComplaints() {
        return customerComplaints;
    }

    public void setCustomerComplaints(long customerComplaints) {
        this.customerComplaints = customerComplaints;
    }

    public BigDecimal getCustomerRetentionRate() {
        return customerRetentionRate;
    }

    public void setCustomerRetentionRate(BigDecimal customerRetentionRate) {
        this.customerRetentionRate = customerRetentionRate;
    }

    public BigDecimal getAverageRentalDuration() {
        return averageRentalDuration;
    }

    public void setAverageRentalDuration(BigDecimal averageRentalDuration) {
        this.averageRentalDuration = averageRentalDuration;
    }

    public String getRevenueChartTitle() {
        return revenueChartTitle;
    }

    public void setRevenueChartTitle(String revenueChartTitle) {
        this.revenueChartTitle = revenueChartTitle;
    }

    public List<ChartPoint> getRevenueChart() {
        return revenueChart;
    }

    public void setRevenueChart(List<ChartPoint> revenueChart) {
        this.revenueChart = revenueChart;
    }

    public List<ChartPoint> getRevenueByVehicleType() {
        return revenueByVehicleType;
    }

    public void setRevenueByVehicleType(List<ChartPoint> revenueByVehicleType) {
        this.revenueByVehicleType = revenueByVehicleType;
    }

    public List<VehicleUtilizationRow> getVehicleUtilization() {
        return vehicleUtilization;
    }

    public void setVehicleUtilization(List<VehicleUtilizationRow> vehicleUtilization) {
        this.vehicleUtilization = vehicleUtilization;
    }

    public List<NamedValueRow> getIdleVehicles() {
        return idleVehicles;
    }

    public void setIdleVehicles(List<NamedValueRow> idleVehicles) {
        this.idleVehicles = idleVehicles;
    }

    public List<NamedValueRow> getFrequentlyRented() {
        return frequentlyRented;
    }

    public void setFrequentlyRented(List<NamedValueRow> frequentlyRented) {
        this.frequentlyRented = frequentlyRented;
    }

    public List<NamedValueRow> getLowPerformingVehicles() {
        return lowPerformingVehicles;
    }

    public void setLowPerformingVehicles(List<NamedValueRow> lowPerformingVehicles) {
        this.lowPerformingVehicles = lowPerformingVehicles;
    }

    public List<NamedValueRow> getCurrentlyRented() {
        return currentlyRented;
    }

    public void setCurrentlyRented(List<NamedValueRow> currentlyRented) {
        this.currentlyRented = currentlyRented;
    }

    public List<NamedValueRow> getNearingService() {
        return nearingService;
    }

    public void setNearingService(List<NamedValueRow> nearingService) {
        this.nearingService = nearingService;
    }

    public List<NamedValueRow> getServiceOverdue() {
        return serviceOverdue;
    }

    public void setServiceOverdue(List<NamedValueRow> serviceOverdue) {
        this.serviceOverdue = serviceOverdue;
    }

    public List<RentalOverdueAlert> getRentalOverdue() {
        return rentalOverdue;
    }

    public void setRentalOverdue(List<RentalOverdueAlert> rentalOverdue) {
        this.rentalOverdue = rentalOverdue;
    }

    public List<NamedValueRow> getTopCustomers() {
        return topCustomers;
    }

    public void setTopCustomers(List<NamedValueRow> topCustomers) {
        this.topCustomers = topCustomers;
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

    public static class NamedValueRow {
        private String name;
        private String registration;
        private String detail;
        private BigDecimal value = BigDecimal.ZERO;
        private int count;

        public NamedValueRow() {
        }

        public NamedValueRow(String name, String detail, BigDecimal value) {
            this.name = name;
            this.detail = detail;
            this.value = value;
        }

        public NamedValueRow(String name, String detail, BigDecimal value, int count) {
            this.name = name;
            this.detail = detail;
            this.value = value;
            this.count = count;
        }

        public NamedValueRow(String name, String registration, String detail, BigDecimal value) {
            this.name = name;
            this.registration = registration;
            this.detail = detail;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getRegistration() {
            return registration;
        }

        public void setRegistration(String registration) {
            this.registration = registration;
        }

        public String getDetail() {
            return detail;
        }

        public void setDetail(String detail) {
            this.detail = detail;
        }

        public BigDecimal getValue() {
            return value;
        }

        public void setValue(BigDecimal value) {
            this.value = value;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }
    }

    public static class VehicleUtilizationRow {
        private String vehicle;
        private String registration;
        private int utilizationPercent;
        private String detail;

        public VehicleUtilizationRow() {
        }

        public VehicleUtilizationRow(String vehicle, String registration, int utilizationPercent, String detail) {
            this.vehicle = vehicle;
            this.registration = registration;
            this.utilizationPercent = utilizationPercent;
            this.detail = detail;
        }

        public String getVehicle() {
            return vehicle;
        }

        public void setVehicle(String vehicle) {
            this.vehicle = vehicle;
        }

        public String getRegistration() {
            return registration;
        }

        public void setRegistration(String registration) {
            this.registration = registration;
        }

        public int getUtilizationPercent() {
            return utilizationPercent;
        }

        public void setUtilizationPercent(int utilizationPercent) {
            this.utilizationPercent = utilizationPercent;
        }

        public String getDetail() {
            return detail;
        }

        public void setDetail(String detail) {
            this.detail = detail;
        }
    }

    public List<ChartPoint> chartLabelsValues(List<ChartPoint> points) {
        return points == null ? Collections.<ChartPoint>emptyList() : points;
    }
}
