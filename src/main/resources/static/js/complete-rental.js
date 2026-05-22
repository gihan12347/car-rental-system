(function () {
    'use strict';

    function parseNum(value, fallback) {
        var n = parseFloat(String(value).replace(',', '.'));
        return isNaN(n) ? fallback : n;
    }

    function formatMoney(n) {
        return n.toFixed(2);
    }

    function inclusiveDays(start, end) {
        if (!start || !end || end < start) {
            return 0;
        }
        var ms = end.getTime() - start.getTime();
        return Math.floor(ms / 86400000) + 1;
    }

    function parseDateInput(value) {
        if (!value) {
            return null;
        }
        var parts = value.split('-');
        if (parts.length !== 3) {
            return null;
        }
        return new Date(parseInt(parts[0], 10), parseInt(parts[1], 10) - 1, parseInt(parts[2], 10));
    }

    function formatDateLabel(d) {
        if (!d) return '—';
        return d.toLocaleDateString(undefined, { year: 'numeric', month: 'short', day: 'numeric' });
    }

    function setText(id, text) {
        var el = document.getElementById(id);
        if (el) {
            el.textContent = text;
        }
    }

    function initCompleteRentalPreview() {
        var form = document.getElementById('completeRentalForm');
        if (!form) {
            return;
        }

        var pickupDate = parseDateInput(form.getAttribute('data-pickup-date'));
        var startMileage = parseNum(form.getAttribute('data-start-mileage'), 0);
        var dailyRate = parseNum(form.getAttribute('data-daily-rate'), 0);
        var kmRate = parseNum(form.getAttribute('data-km-rate'), 0);
        var freeKmPerDay = parseNum(form.getAttribute('data-free-km-per-day'), 0);

        var returnDateInput = document.getElementById('returnDate');
        var returnMileageInput = document.getElementById('returnMileageKm');
        var submitBtn = document.getElementById('completeSubmitBtn');

        if (!returnDateInput || !returnMileageInput) {
            return;
        }

        function updateSubmitState() {
            if (!submitBtn) {
                return;
            }
            var returnDate = parseDateInput(returnDateInput.value);
            var mileageRaw = returnMileageInput.value.trim();
            var returnMileage = parseNum(returnMileageInput.value, -1);
            var daysOk = returnDate && pickupDate && inclusiveDays(pickupDate, returnDate) > 0;
            var mileageOk = mileageRaw !== '' && returnMileage >= startMileage;
            submitBtn.disabled = !(daysOk && mileageOk);
        }

        function formatRatePerDay() {
            return formatMoney(dailyRate) + ' / day';
        }

        function formatRatePerKm() {
            return formatMoney(kmRate) + ' / km (over allowance)';
        }

        function setStaticRates() {
            setText('previewDailyRate', formatRatePerDay());
            setText('previewKmRate', formatRatePerKm());
            setText('previewFreeKmPerDay', Math.max(0, Math.floor(freeKmPerDay)) + ' km / day');
        }

        function clearPreview(message) {
            setText('previewPeriod', '—');
            setText('previewStartMileage', startMileage + ' km');
            setText('previewReturnMileage', '—');
            setText('previewTripKm', '—');
            setText('previewTripKmInlineValue', '—');
            setStaticRates();
            setText('previewIncludedKm', '—');
            setText('previewBillableExtraKm', '—');
            setText('previewDailyFormula', '');
            setText('previewKmFormula', '');
            setText('previewDailyCharge', '—');
            setText('previewKmCharge', '—');
            setText('previewTotal', '—');
            var badge = document.getElementById('previewTotalBadge');
            if (badge) {
                badge.textContent = 'Total: —';
            }
            setText('previewHint', message || 'Enter return date and return odometer to calculate the total.');
            updateSubmitState();
        }

        function updatePreview() {
            var returnDate = parseDateInput(returnDateInput.value);
            var returnMileage = parseNum(returnMileageInput.value, -1);

            setText('previewStartMileage', startMileage + ' km');
            setStaticRates();

            if (!returnDate || !pickupDate) {
                clearPreview('Select a return date on or after the rental start.');
                return;
            }

            if (returnMileage < 0 || returnMileageInput.value === '') {
                clearPreview('Enter the odometer reading after the trip.');
                return;
            }

            if (returnMileage < startMileage) {
                setText('previewReturnMileage', returnMileage + ' km');
                setText('previewTripKmInlineValue', '—');
                clearPreview('Return odometer must be at least ' + startMileage + ' km.');
                return;
            }

            var days = inclusiveDays(pickupDate, returnDate);
            if (days <= 0) {
                clearPreview('Return date cannot be before the rental start date.');
                return;
            }

            var tripKm = returnMileage - startMileage;
            var freePerDay = Math.max(0, Math.floor(freeKmPerDay));
            var includedKm = freePerDay * days;
            var billableExtraKm = Math.max(0, tripKm - includedKm);
            var dailyCharge = dailyRate * days;
            var kmCharge = kmRate * billableExtraKm;
            var total = dailyCharge + kmCharge;
            var dayLabel = days === 1 ? 'day' : 'days';

            setText('previewPeriod', formatDateLabel(pickupDate) + ' → ' + formatDateLabel(returnDate) + ' (' + days + ' ' + dayLabel + ')');
            setText('previewReturnMileage', returnMileage + ' km');
            setText('previewTripKm', tripKm + ' km');
            setText('previewTripKmInlineValue', tripKm + ' km');
            setText('previewIncludedKm', includedKm + ' km (' + freePerDay + ' × ' + days + ' ' + dayLabel + ')');
            setText('previewBillableExtraKm', billableExtraKm + ' km');
            setText('previewDailyFormula', formatMoney(dailyRate) + ' × ' + days + ' ' + dayLabel);
            if (billableExtraKm > 0) {
                setText('previewKmFormula', formatMoney(kmRate) + ' × ' + billableExtraKm + ' km');
            } else {
                setText('previewKmFormula', 'Within free allowance — no extra km charge');
            }
            setText('previewDailyCharge', formatMoney(dailyCharge));
            setText('previewKmCharge', formatMoney(kmCharge));
            setText('previewTotal', formatMoney(total));

            var badge = document.getElementById('previewTotalBadge');
            if (badge) {
                badge.textContent = 'Total: ' + formatMoney(total);
            }
            setText('previewHint', 'This total will be saved when you complete the rental.');
            updateSubmitState();
        }

        returnDateInput.addEventListener('change', updatePreview);
        returnDateInput.addEventListener('input', updatePreview);
        returnMileageInput.addEventListener('change', updatePreview);
        returnMileageInput.addEventListener('input', updatePreview);
        setStaticRates();
        updatePreview();
        updateSubmitState();
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initCompleteRentalPreview);
    } else {
        initCompleteRentalPreview();
    }
})();
