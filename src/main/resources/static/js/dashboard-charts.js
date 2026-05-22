(function () {
    'use strict';

    function initDashboardCharts() {
        if (typeof FleetCharts === 'undefined' || typeof Chart === 'undefined') {
            return;
        }

        var data = {};
        try {
            var payloadEl = document.getElementById('dashboardChartPayload');
            if (payloadEl && payloadEl.textContent) {
                data = JSON.parse(payloadEl.textContent);
            }
        } catch (e) {
            console.error('Dashboard chart data parse failed', e);
            return;
        }

        if (data.revenueChart && data.revenueChart.length) {
            var isDaily = data.revenueChartTitle && data.revenueChartTitle.indexOf('Daily') === 0;
            if (isDaily) {
                FleetCharts.lineChart('chartRevenue', data.revenueChart, 'chartRevenueEmpty');
            } else {
                FleetCharts.barChart('chartRevenue', data.revenueChart, 'chartRevenueEmpty');
            }
        } else {
            FleetCharts.barChart('chartRevenue', [], 'chartRevenueEmpty');
        }

        if (data.revenueByVehicleType && data.revenueByVehicleType.length) {
            FleetCharts.doughnutChart('chartRevenueByType', data.revenueByVehicleType, 'chartRevenueByTypeEmpty');
        } else {
            FleetCharts.doughnutChart('chartRevenueByType', [], 'chartRevenueByTypeEmpty');
        }
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initDashboardCharts);
    } else {
        initDashboardCharts();
    }
})();
