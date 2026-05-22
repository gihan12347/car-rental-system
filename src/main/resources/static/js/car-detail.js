(function () {
    'use strict';

    function initCarFinanceChart() {
        if (typeof FleetCharts === 'undefined' || typeof Chart === 'undefined') {
            return;
        }

        var payloadEl = document.getElementById('carDetailChartPayload');
        if (!payloadEl) {
            return;
        }

        var data;
        try {
            data = JSON.parse(payloadEl.textContent || '{}');
        } catch (e) {
            console.error('Car detail chart parse failed', e);
            return;
        }

        FleetCharts.groupedBarChart(
            'chartCarFinance',
            data.incomeChart || [],
            data.expenseChart || [],
            'chartCarFinanceEmpty');
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initCarFinanceChart);
    } else {
        initCarFinanceChart();
    }
})();
