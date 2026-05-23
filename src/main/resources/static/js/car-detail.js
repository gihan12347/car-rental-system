(function () {
    'use strict';

    function statusClassSuffix(status) {
        if (!status) {
            return 'available';
        }
        return String(status).toLowerCase();
    }

    function escapeHtml(text) {
        if (text == null) {
            return '';
        }
        return String(text)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;');
    }

    function rentalStatusBadgeClass(status) {
        if (status === 'ACTIVE') {
            return 'text-bg-warning text-dark';
        }
        if (status === 'COMPLETED') {
            return 'text-bg-success';
        }
        return 'text-bg-secondary';
    }

    function detailRow(label, value) {
        if (!value) {
            return '';
        }
        return '<div class="car-cal-detail__row">'
            + '<span class="car-cal-detail__row-label">' + escapeHtml(label) + '</span>'
            + '<span class="car-cal-detail__row-value">' + escapeHtml(value) + '</span>'
            + '</div>';
    }

    function buildRentalCard(r) {
        var html = '<article class="car-cal-detail__item car-cal-detail__item--rental">'
            + '<div class="d-flex align-items-start justify-content-between gap-2 mb-2">'
            + '<span class="fw-semibold">' + escapeHtml(r.customerName) + '</span>'
            + '<span class="badge badge-status flex-shrink-0 ' + rentalStatusBadgeClass(r.rentalStatus) + '">'
            + escapeHtml(r.rentalStatus) + '</span></div>'
            + '<div class="car-cal-detail__rows">';

        html += detailRow('Hire period', r.period);
        if (r.pickupDate && r.returnDate) {
            html += detailRow('Pickup', r.pickupDate);
            html += detailRow('Return', r.returnDate);
        }
        if (r.numberOfDays != null) {
            html += detailRow('Days', String(r.numberOfDays));
        }
        html += detailRow('Customer ID', r.customerIdNumber);
        html += detailRow('Phone', r.customerContact);
        html += detailRow('Address', r.customerAddress);
        html += detailRow('Travel', r.travelLocation);
        if (r.totalPrice != null && r.totalPrice !== '') {
            html += detailRow('Total price', Number(r.totalPrice).toFixed(2));
        }

        html += '</div></article>';
        return html;
    }

    function buildMaintenanceCard(m) {
        var html = '<article class="car-cal-detail__item car-cal-detail__item--maintenance">'
            + '<div class="fw-semibold mb-2">' + escapeHtml(m.description) + '</div>'
            + '<div class="car-cal-detail__rows">';

        html += detailRow('Date', m.maintenanceDate);
        if (m.cost != null) {
            html += detailRow('Cost', Number(m.cost).toFixed(2));
        }
        if (m.mileageKm != null) {
            html += detailRow('Mileage', m.mileageKm + ' km');
        }

        html += '</div></article>';
        return html;
    }

    function buildDayBodyHtml(day) {
        var hasRentals = day.rentals && day.rentals.length > 0;
        var hasMaintenance = day.maintenance && day.maintenance.length > 0;

        if (!hasRentals && !hasMaintenance) {
            return '<p class="car-cal-day-modal__empty text-muted text-center mb-0">'
                + 'No rental or maintenance on this day — vehicle is available.'
                + '</p>';
        }

        var html = '<div class="car-cal-day-modal__sections">';

        if (hasRentals) {
            html += '<section class="car-cal-day-modal__block car-cal-day-modal__block--rental">'
                + '<h6 class="car-cal-detail__section-title">'
                + '<i class="bi bi-calendar-check me-1"></i> Rental details</h6>';
            day.rentals.forEach(function (r) {
                html += buildRentalCard(r);
            });
            html += '</section>';
        }

        if (hasMaintenance) {
            html += '<section class="car-cal-day-modal__block car-cal-day-modal__block--maintenance">'
                + '<h6 class="car-cal-detail__section-title">'
                + '<i class="bi bi-wrench-adjustable me-1"></i> Maintenance details</h6>';
            day.maintenance.forEach(function (m) {
                html += buildMaintenanceCard(m);
            });
            html += '</section>';
        }

        html += '</div>';
        return html;
    }

    function initCalendarDayDetail(calendarDays) {
        var grid = document.getElementById('carAvailabilityCalendar');
        var modalEl = document.getElementById('carCalDayModal');
        var modalTitle = document.getElementById('carCalDayModalLabel');
        var modalStatus = document.getElementById('carCalDayModalStatus');
        var modalBody = document.getElementById('carCalDayModalBody');
        if (!grid || !modalEl || !modalTitle || !modalStatus || !modalBody) {
            return;
        }

        var modalInstance = null;
        if (typeof bootstrap !== 'undefined') {
            modalInstance = bootstrap.Modal.getOrCreateInstance(modalEl);
        }

        function openDayPopup(iso, btn) {
            var day = calendarDays[iso];
            if (!day) {
                return;
            }

            grid.querySelectorAll('.car-cal-day--selected').forEach(function (el) {
                el.classList.remove('car-cal-day--selected');
            });
            if (btn) {
                btn.classList.add('car-cal-day--selected');
            }

            modalTitle.textContent = day.dateLabel;
            var statusKey = statusClassSuffix(day.status);
            modalStatus.textContent = day.statusLabel;
            modalStatus.className = 'car-cal-detail__status car-cal-detail__status--' + statusKey;
            modalBody.innerHTML = buildDayBodyHtml(day);

            if (modalInstance) {
                modalInstance.show();
            }
        }

        modalEl.addEventListener('hidden.bs.modal', function () {
            grid.querySelectorAll('.car-cal-day--selected').forEach(function (el) {
                el.classList.remove('car-cal-day--selected');
            });
        });

        grid.querySelectorAll('.car-cal-day--clickable').forEach(function (btn) {
            btn.addEventListener('click', function () {
                openDayPopup(btn.getAttribute('data-date'), btn);
            });
        });
    }

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

    function initCarDetailPage() {
        var payloadEl = document.getElementById('carDetailChartPayload');
        var data = {};
        if (payloadEl) {
            try {
                data = JSON.parse(payloadEl.textContent || '{}');
            } catch (e) {
                console.error('Car detail payload parse failed', e);
            }
        }
        initCalendarDayDetail(data.calendarDays || {});
        initCarFinanceChart();
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initCarDetailPage);
    } else {
        initCarDetailPage();
    }
})();
