(function () {
    'use strict';

    function initDashboardCharts() {
        if (typeof FleetCharts === 'undefined' || typeof Chart === 'undefined') {
            return;
        }
        if (!document.getElementById('chartRevenue')) {
            return;
        }

        var chartsRow = document.querySelector('.dashboard-revenue-charts');
        if (chartsRow) {
            chartsRow.classList.add('dashboard-revenue-charts--animating');
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

        var revenueAnim = { baseDelay: 120, stagger: 90, duration: 1600 };
        var doughnutAnim = { baseDelay: 200, stagger: 120, duration: 1700 };

        if (data.revenueChart && data.revenueChart.length) {
            var isDaily = data.revenueChartTitle && data.revenueChartTitle.indexOf('Daily') === 0;
            if (isDaily) {
                FleetCharts.lineChart('chartRevenue', data.revenueChart, 'chartRevenueEmpty', revenueAnim);
            } else {
                FleetCharts.barChart('chartRevenue', data.revenueChart, 'chartRevenueEmpty', revenueAnim);
            }
        } else {
            FleetCharts.barChart('chartRevenue', [], 'chartRevenueEmpty', revenueAnim);
        }

        window.setTimeout(function () {
            if (data.revenueByVehicleType && data.revenueByVehicleType.length) {
                FleetCharts.doughnutChart('chartRevenueByType', data.revenueByVehicleType, 'chartRevenueByTypeEmpty', doughnutAnim);
            } else {
                FleetCharts.doughnutChart('chartRevenueByType', [], 'chartRevenueByTypeEmpty', doughnutAnim);
            }
        }, 220);
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initDashboardCharts);
    } else {
        initDashboardCharts();
    }
})();
