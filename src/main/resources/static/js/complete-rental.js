(function () {
    'use strict';

    var MINUTES_PER_DAY = 24 * 60;

    function parseNum(value, fallback) {
        var n = parseFloat(String(value).replace(',', '.'));
        return isNaN(n) ? fallback : n;
    }

    function formatMoney(n) {
        return n.toFixed(2);
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

    function parseTimeInput(value) {
        if (!value) {
            return null;
        }
        var parts = value.split(':');
        if (parts.length < 2) {
            return null;
        }
        return {
            h: parseInt(parts[0], 10),
            m: parseInt(parts[1], 10)
        };
    }

    function combineDateTime(dateVal, timeVal) {
        var date = parseDateInput(dateVal);
        var time = parseTimeInput(timeVal);
        if (!date || !time || isNaN(time.h) || isNaN(time.m)) {
            return null;
        }
        return new Date(date.getFullYear(), date.getMonth(), date.getDate(), time.h, time.m, 0, 0);
    }

    function split24h(startDt, endDt) {
        if (!startDt || !endDt || endDt <= startDt) {
            return null;
        }
        var totalMinutes = Math.floor((endDt.getTime() - startDt.getTime()) / 60000);
        var fullDays = Math.floor(totalMinutes / MINUTES_PER_DAY);
        var remainderMinutes = totalMinutes % MINUTES_PER_DAY;
        var extraHours = Math.round(remainderMinutes / 60 * 100) / 100;
        return { fullDays: fullDays, extraHours: extraHours, totalMinutes: totalMinutes };
    }

    function formatTimeAmPm(h, m) {
        var ampm = h >= 12 ? 'PM' : 'AM';
        var h12 = h % 12;
        if (h12 === 0) {
            h12 = 12;
        }
        return h12 + ':' + String(m).padStart(2, '0') + ' ' + ampm;
    }

    function formatDateTimeLabel(dt) {
        if (!dt) {
            return '—';
        }
        return dt.toLocaleDateString(undefined, { year: 'numeric', month: 'short', day: 'numeric' })
            + ' ' + formatTimeAmPm(dt.getHours(), dt.getMinutes());
    }

    function formatDurationLabel(duration) {
        if (!duration) {
            return '—';
        }
        var parts = [];
        if (duration.fullDays > 0) {
            parts.push(duration.fullDays + (duration.fullDays === 1 ? ' day' : ' days'));
        }
        if (duration.extraHours > 0) {
            parts.push(duration.extraHours + ' hr');
        }
        if (parts.length === 0) {
            return 'under 1 hour';
        }
        return parts.join(' + ');
    }

    function setText(id, text) {
        var el = document.getElementById(id);
        if (el) {
            el.textContent = text;
        }
    }

    function initBlacklistPanel() {
        var checkbox = document.getElementById('blacklistCustomer');
        var reasonWrap = document.getElementById('blacklistReasonWrap');
        if (!checkbox || !reasonWrap) {
            return;
        }
        function syncReasonVisibility() {
            reasonWrap.classList.toggle('d-none', !checkbox.checked);
        }
        checkbox.addEventListener('change', syncReasonVisibility);
        syncReasonVisibility();
    }

    function initCompleteRentalPreview() {
        var form = document.getElementById('completeRentalForm');
        if (!form) {
            return;
        }

        initBlacklistPanel();

        var pickupDateStr = form.getAttribute('data-pickup-date');
        var pickupTimeStr = form.getAttribute('data-pickup-time') || '00:00';
        var plannedReturnDateStr = form.getAttribute('data-planned-return-date');
        var plannedReturnTimeStr = form.getAttribute('data-planned-return-time') || pickupTimeStr;
        var pickupDateTime = combineDateTime(pickupDateStr, pickupTimeStr);
        var plannedReturnDateTime = combineDateTime(plannedReturnDateStr, plannedReturnTimeStr);
        var startMileage = parseNum(form.getAttribute('data-start-mileage'), 0);
        var dailyRate = parseNum(form.getAttribute('data-daily-rate'), 0);
        var hourRate = parseNum(form.getAttribute('data-hour-rate'), 0);
        var hireType = form.getAttribute('data-hire-type') || 'PER_DAY';
        var pricePerDay = parseNum(form.getAttribute('data-price-per-day'), 0);
        var pricePerWeek = parseNum(form.getAttribute('data-price-per-week'), 0);
        var pricePerMonth = parseNum(form.getAttribute('data-price-per-month'), 0);
        var kmRate = parseNum(form.getAttribute('data-km-rate'), 0);
        var freeKmPerDay = parseNum(form.getAttribute('data-free-km-per-day'), 0);
        var employeeHire = form.getAttribute('data-employee-hire') === 'true';

        function rateForHireType() {
            if (hireType === 'PER_WEEK') {
                return pricePerWeek;
            }
            if (hireType === 'PER_MONTH') {
                return pricePerMonth;
            }
            return pricePerDay;
        }

        function computeDailyCharge(days) {
            if (days <= 0) {
                return 0;
            }
            return rateForHireType() * days;
        }

        function computeHourCharge(extraHours) {
            if (extraHours <= 0) {
                return 0;
            }
            return hourRate * extraHours;
        }

        function dailyChargeFormula(days) {
            var dayLabel = days === 1 ? 'day' : 'days';
            return formatMoney(rateForHireType()) + ' / day × ' + days + ' ' + dayLabel;
        }

        function hourChargeFormula(extraHours) {
            return formatMoney(hourRate) + ' / hr × ' + extraHours + ' hr';
        }

        function resolveBillingEnd(plannedDt, actualDt) {
            if (!plannedDt || !actualDt) {
                return actualDt || plannedDt;
            }
            return actualDt.getTime() > plannedDt.getTime() ? actualDt : plannedDt;
        }

        function usesPlannedPeriodPrice(plannedDt, actualDt) {
            if (!plannedDt || !actualDt) {
                return false;
            }
            return actualDt.getTime() <= plannedDt.getTime();
        }

        var returnDateInput = document.getElementById('returnDate');
        var returnTimeInput = document.getElementById('returnTime');
        var returnMileageInput = document.getElementById('returnMileageKm');
        var discountInput = document.getElementById('discount');
        var submitBtn = document.getElementById('completeSubmitBtn');

        if (!returnDateInput || !returnMileageInput || !returnTimeInput) {
            return;
        }

        function updateSubmitState() {
            if (!submitBtn) {
                return;
            }
            var returnDateTime = combineDateTime(returnDateInput.value, returnTimeInput.value);
            var mileageRaw = returnMileageInput.value.trim();
            var returnMileage = parseNum(returnMileageInput.value, -1);
            var durationOk = split24h(pickupDateTime, returnDateTime) !== null;
            var mileageOk = mileageRaw !== '' && returnMileage >= startMileage;
            submitBtn.disabled = !(durationOk && mileageOk);
        }

        function formatRatePerDay() {
            return formatMoney(rateForHireType()) + ' / day';
        }

        function formatRatePerHour() {
            return formatMoney(hourRate) + ' / hr (beyond full days)';
        }

        function formatRatePerKm() {
            return formatMoney(kmRate) + ' / km (over allowance)';
        }

        function setStaticRates() {
            setText('previewDailyRate', formatRatePerDay());
            setText('previewHourRate', formatRatePerHour());
            setText('previewKmRate', formatRatePerKm());
            setText('previewFreeKmPerDay', Math.max(0, Math.floor(freeKmPerDay)) + ' km / day');
        }

        function parseDiscount() {
            if (!discountInput || employeeHire) {
                return 0;
            }
            var raw = discountInput.value.trim();
            if (raw === '') {
                return 0;
            }
            var amount = parseNum(discountInput.value, 0);
            return amount < 0 ? 0 : amount;
        }

        function applyDiscountToTotal(subtotal) {
            var discount = parseDiscount();
            if (discount > subtotal) {
                discount = subtotal;
            }
            return {
                subtotal: subtotal,
                discount: discount,
                total: subtotal - discount
            };
        }

        function updateDiscountRow(subtotal, discount, finalTotal) {
            var discountRow = document.getElementById('previewDiscountRow');
            if (!discountRow) {
                return;
            }
            if (discount > 0) {
                discountRow.classList.remove('d-none');
                setText('previewDiscountAmount', '−' + formatMoney(discount));
            } else {
                discountRow.classList.add('d-none');
                setText('previewDiscountAmount', '—');
            }
            setText('previewSubtotal', formatMoney(subtotal));
            setText('previewTotal', formatMoney(finalTotal));
            var badge = document.getElementById('previewTotalBadge');
            if (badge) {
                badge.textContent = 'Total: ' + formatMoney(finalTotal);
            }
        }

        function setPeriodPreview(pickupDt, actualReturnDt, billingDuration, bookedRateApplied) {
            setText('previewPeriodFrom', 'From ' + formatDateTimeLabel(pickupDt));
            setText('previewPeriodTo', 'To ' + formatDateTimeLabel(actualReturnDt));
            var durationEl = document.getElementById('previewPeriodDuration');
            if (durationEl) {
                var durationText = formatDurationLabel(billingDuration);
                if (bookedRateApplied) {
                    durationText += ' · booked rate';
                }
                durationEl.textContent = durationText;
                durationEl.classList.remove('d-none');
            }
        }

        function clearPeriodPreview() {
            setText('previewPeriodFrom', '—');
            setText('previewPeriodTo', '—');
            var durationEl = document.getElementById('previewPeriodDuration');
            if (durationEl) {
                durationEl.textContent = '';
                durationEl.classList.add('d-none');
            }
        }

        function clearPreview(message) {
            clearPeriodPreview();
            setText('previewStartMileage', startMileage + ' km');
            setText('previewReturnMileage', '—');
            setText('previewTripKm', '—');
            setText('previewTripKmInlineValue', '—');
            setStaticRates();
            setText('previewIncludedKm', '—');
            setText('previewBillableExtraKm', '—');
            setText('previewDailyFormula', '');
            setText('previewHourFormula', '');
            setText('previewKmFormula', '');
            setText('previewDailyCharge', '—');
            setText('previewHourCharge', '—');
            setText('previewKmCharge', '—');
            setText('previewSubtotal', '—');
            var discountRow = document.getElementById('previewDiscountRow');
            if (discountRow) {
                discountRow.classList.add('d-none');
            }
            setText('previewDiscountAmount', '—');
            setText('previewTotal', '—');
            var badge = document.getElementById('previewTotalBadge');
            if (badge) {
                badge.textContent = 'Total: —';
            }
            setText('previewHint', message || 'Enter return date, time, and return odometer to calculate the total.');
            updateSubmitState();
        }

        function updatePreview() {
            var returnDateTime = combineDateTime(returnDateInput.value, returnTimeInput.value);
            var returnMileage = parseNum(returnMileageInput.value, -1);

            setText('previewStartMileage', startMileage + ' km');
            setStaticRates();

            if (!returnDateTime || !pickupDateTime) {
                clearPreview('Select return date and time after the rental start.');
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

            var duration = split24h(pickupDateTime, returnDateTime);
            if (!duration) {
                clearPreview('Return date/time must be after the rental start.');
                return;
            }

            var billingEnd = resolveBillingEnd(plannedReturnDateTime, returnDateTime);
            var billingDuration = split24h(pickupDateTime, billingEnd);
            if (!billingDuration) {
                clearPreview('Could not calculate the booked rental period.');
                return;
            }
            var bookedRateApplied = usesPlannedPeriodPrice(plannedReturnDateTime, returnDateTime);

            var days = billingDuration.fullDays;
            var extraHours = billingDuration.extraHours;
            var tripKm = returnMileage - startMileage;
            var freePerDay = Math.max(0, Math.floor(freeKmPerDay));
            var includedKm = freePerDay * days;
            var billableExtraKm = Math.max(0, tripKm - includedKm);
            var dailyCharge = employeeHire ? 0 : computeDailyCharge(days);
            var hourCharge = employeeHire ? 0 : computeHourCharge(extraHours);
            var kmCharge = employeeHire ? 0 : kmRate * billableExtraKm;
            var subtotal = employeeHire ? 0 : dailyCharge + hourCharge + kmCharge;
            var priced = applyDiscountToTotal(subtotal);
            var dayLabel = days === 1 ? 'day' : 'days';

            setPeriodPreview(pickupDateTime, returnDateTime, billingDuration, bookedRateApplied);
            setText('previewReturnMileage', returnMileage + ' km');
            setText('previewTripKm', tripKm + ' km');
            setText('previewTripKmInlineValue', tripKm + ' km');
            setText('previewIncludedKm', includedKm + ' km (' + freePerDay + ' × ' + days + ' ' + dayLabel + ')');
            setText('previewBillableExtraKm', billableExtraKm + ' km');
            if (employeeHire) {
                setText('previewDailyFormula', 'Employee hire — waived');
                setText('previewHourFormula', 'Employee hire — waived');
                setText('previewKmFormula', 'Employee hire — waived');
            } else {
                if (days > 0) {
                    setText('previewDailyFormula', dailyChargeFormula(days));
                } else {
                    setText('previewDailyFormula', 'No full 24-hour days');
                }
                if (extraHours > 0) {
                    setText('previewHourFormula', hourChargeFormula(extraHours));
                } else {
                    setText('previewHourFormula', 'No extra hours beyond full days');
                }
                if (billableExtraKm > 0) {
                    setText('previewKmFormula', formatMoney(kmRate) + ' × ' + billableExtraKm + ' km');
                } else {
                    setText('previewKmFormula', 'Within free allowance — no extra km charge');
                }
            }
            setText('previewDailyCharge', formatMoney(dailyCharge));
            setText('previewHourCharge', formatMoney(hourCharge));
            setText('previewKmCharge', formatMoney(kmCharge));
            updateDiscountRow(priced.subtotal, priced.discount, priced.total);

            setText('previewHint', employeeHire
                ? 'Employee vehicle hire — total charge will be 0.00.'
                : bookedRateApplied
                    ? 'Booked period charged (return on or before planned end). Extra km still applies.'
                    : 'Includes extra time beyond the booked return date/time.');
            updateSubmitState();
        }

        returnDateInput.addEventListener('change', updatePreview);
        returnDateInput.addEventListener('input', updatePreview);
        returnTimeInput.addEventListener('change', updatePreview);
        returnTimeInput.addEventListener('input', updatePreview);
        returnMileageInput.addEventListener('change', updatePreview);
        returnMileageInput.addEventListener('input', updatePreview);
        if (discountInput) {
            discountInput.addEventListener('change', updatePreview);
            discountInput.addEventListener('input', updatePreview);
        }
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
