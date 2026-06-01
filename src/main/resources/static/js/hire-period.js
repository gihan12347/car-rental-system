(function () {
    'use strict';

    function byId(id) {
        return document.getElementById(id);
    }

    function parseDate(value) {
        if (!value) {
            return null;
        }
        var parts = value.split('-');
        if (parts.length !== 3) {
            return null;
        }
        var y = parseInt(parts[0], 10);
        var m = parseInt(parts[1], 10) - 1;
        var d = parseInt(parts[2], 10);
        var dt = new Date(y, m, d);
        if (dt.getFullYear() !== y || dt.getMonth() !== m || dt.getDate() !== d) {
            return null;
        }
        return dt;
    }

    function inclusiveDays(startVal, endVal) {
        var start = parseDate(startVal);
        var end = parseDate(endVal);
        if (!start || !end || end < start) {
            return 0;
        }
        var ms = end.getTime() - start.getTime();
        return Math.floor(ms / 86400000) + 1;
    }

    function formatDays(n) {
        return n === 1 ? '1 day' : n + ' days';
    }

    function parseMoney(value) {
        var n = parseFloat(String(value));
        return isNaN(n) ? 0 : n;
    }

    function formatMoney(n) {
        return n.toFixed(2);
    }

    function getSelectedHireType(form) {
        var checked = form.querySelector('input[name="hireType"]:checked');
        return checked ? checked.value : 'PER_DAY';
    }

    function dailyRateForHireType(car, hireType) {
        if (hireType === 'PER_WEEK') {
            return parseMoney(car.rentalPricePerWeek);
        }
        if (hireType === 'PER_MONTH') {
            return parseMoney(car.rentalPricePerMonth);
        }
        return parseMoney(car.rentalPricePerDay);
    }

    function buildCarLabel(car, hireType) {
        var name = car.modelName && car.modelName.trim() !== '' ? car.modelName : car.registrationNumber;
        var rate = dailyRateForHireType(car, hireType);
        return name + ' · ' + car.registrationNumber + ' · ' + car.passengerCount + ' seats · '
            + formatMoney(rate) + ' / day';
    }

    function computeDailyCharge(car, hireType, days) {
        if (days <= 0) {
            return 0;
        }
        return dailyRateForHireType(car, hireType) * days;
    }

    function initHirePeriodForm() {
        var form = byId('newHireForm');
        if (!form) {
            return;
        }

        var startInput = byId('nh-start');
        var endInput = byId('nh-end');
        var carSelect = byId('nh-car');
        var carWrap = byId('nh-car-wrap');
        var emptyMsg = byId('nh-no-cars');
        var pickDatesMsg = byId('nh-pick-dates');
        var daysHint = byId('nh-days-hint');
        var submitBtn = form.closest('.modal-content')
            ? form.closest('.modal-content').querySelector('[form="newHireForm"]')
            : null;
        var nameInput = byId('nh-name');
        var idInput = byId('nh-id');
        var addressInput = byId('nh-address');
        var contactInput = byId('nh-contact');
        var travelInput = byId('nh-travel');

        if (!startInput || !endInput) {
            return;
        }

        function isCustomerComplete() {
            return nameInput && nameInput.value.trim() !== ''
                && idInput && idInput.value.trim() !== ''
                && addressInput && addressInput.value.trim() !== ''
                && contactInput && contactInput.value.trim() !== ''
                && travelInput && travelInput.value.trim() !== '';
        }

        function isHireReady() {
            var days = inclusiveDays(startInput.value, endInput.value);
            return days > 0
                && carSelect && carSelect.value !== ''
                && !carSelect.disabled
                && isCustomerComplete();
        }

        function refreshSubmit() {
            setSubmitEnabled(isHireReady());
        }

        var apiUrl = form.getAttribute('data-available-cars-url');
        var preservedCarId = carSelect ? carSelect.getAttribute('data-selected-car-id') : '';
        var loadTimer = null;
        var carsById = {};
        var rateHint = byId('nh-rate-hint');
        var hireTypeGroup = byId('nh-hire-type-group');

        function setSubmitEnabled(enabled) {
            if (submitBtn) {
                submitBtn.disabled = !enabled;
            }
        }

        function showPickDates() {
            refreshSubmit();
            if (carWrap) {
                carWrap.classList.add('d-none');
            }
            if (emptyMsg) {
                emptyMsg.classList.add('d-none');
            }
            if (pickDatesMsg) {
                pickDatesMsg.classList.remove('d-none');
            }
            if (carSelect) {
                carSelect.innerHTML = '';
                carSelect.disabled = true;
            }
            setSubmitEnabled(false);
            refreshSubmit();
        }

        function showNoCars() {
            if (carWrap) {
                carWrap.classList.add('d-none');
            }
            if (pickDatesMsg) {
                pickDatesMsg.classList.add('d-none');
            }
            if (emptyMsg) {
                emptyMsg.classList.remove('d-none');
            }
            if (carSelect) {
                carSelect.innerHTML = '';
                carSelect.disabled = true;
            }
            setSubmitEnabled(false);
            refreshSubmit();
        }

        function updateRateHint() {
            if (!rateHint) {
                return;
            }
            var hireType = getSelectedHireType(form);
            var days = inclusiveDays(startInput.value, endInput.value);
            var carId = carSelect ? carSelect.value : '';
            var car = carId ? carsById[carId] : null;
            if (!car || days <= 0) {
                rateHint.textContent = 'Select dates and a vehicle to see the rate for this hire type.';
                return;
            }
            var rate = dailyRateForHireType(car, hireType);
            var charge = rate * days;
            rateHint.textContent = formatMoney(rate) + ' / day × ' + days + ' day'
                + (days === 1 ? '' : 's') + ' = ' + formatMoney(charge)
                + ' time charge (excl. extra km).';
        }

        function refreshCarOptions() {
            var hireType = getSelectedHireType(form);
            if (!carSelect) {
                return;
            }
            var current = carSelect.value;
            Array.prototype.forEach.call(carSelect.options, function (opt) {
                if (!opt.value) {
                    return;
                }
                var car = carsById[opt.value];
                if (car) {
                    opt.textContent = buildCarLabel(car, hireType);
                }
            });
            updateRateHint();
        }

        function showCars(cars) {
            if (pickDatesMsg) {
                pickDatesMsg.classList.add('d-none');
            }
            if (emptyMsg) {
                emptyMsg.classList.add('d-none');
            }
            if (!carSelect) {
                return;
            }
            carsById = {};
            carSelect.innerHTML = '';
            var placeholder = document.createElement('option');
            placeholder.value = '';
            placeholder.textContent = 'Choose a vehicle…';
            carSelect.appendChild(placeholder);

            var hireType = getSelectedHireType(form);
            cars.forEach(function (car) {
                carsById[String(car.id)] = car;
                var opt = document.createElement('option');
                opt.value = String(car.id);
                opt.textContent = buildCarLabel(car, hireType);
                carSelect.appendChild(opt);
            });

            if (preservedCarId) {
                carSelect.value = preservedCarId;
                preservedCarId = '';
            }

            carSelect.disabled = false;
            if (carWrap) {
                carWrap.classList.remove('d-none');
            }
            refreshCarOptions();
            refreshSubmit();
        }

        function updateDaysHint() {
            if (!daysHint) {
                return;
            }
            var days = inclusiveDays(startInput.value, endInput.value);
            if (days > 0) {
                daysHint.textContent = formatDays(days);
                daysHint.classList.remove('text-danger');
            } else if (startInput.value && endInput.value) {
                daysHint.textContent = 'End date must be on or after start date.';
                daysHint.classList.add('text-danger');
            } else {
                daysHint.textContent = 'Select start and end dates.';
                daysHint.classList.remove('text-danger');
            }
            updateRateHint();
        }

        function loadCars() {
            updateDaysHint();
            refreshSubmit();
            var start = startInput.value;
            var end = endInput.value;
            var days = inclusiveDays(start, end);
            if (!apiUrl || days <= 0) {
                showPickDates();
                return;
            }

            if (endInput.min !== start) {
                endInput.min = start;
            }
            if (start && endInput.value && endInput.value < start) {
                endInput.value = start;
                end = start;
            }

            var url = apiUrl + '?start=' + encodeURIComponent(start) + '&end=' + encodeURIComponent(end);
            fetch(url, { headers: { Accept: 'application/json' }, credentials: 'same-origin' })
                .then(function (res) {
                    if (!res.ok) {
                        throw new Error('Failed to load vehicles');
                    }
                    return res.json();
                })
                .then(function (cars) {
                    if (!cars || cars.length === 0) {
                        showNoCars();
                    } else {
                        showCars(cars);
                    }
                })
                .catch(function () {
                    showNoCars();
                });
        }

        function scheduleLoad() {
            clearTimeout(loadTimer);
            loadTimer = setTimeout(loadCars, 200);
        }

        startInput.addEventListener('change', scheduleLoad);
        endInput.addEventListener('change', scheduleLoad);
        startInput.addEventListener('input', scheduleLoad);
        endInput.addEventListener('input', scheduleLoad);

        if (carSelect) {
            carSelect.addEventListener('change', function () {
                refreshSubmit();
                updateRateHint();
            });
        }
        if (hireTypeGroup) {
            hireTypeGroup.addEventListener('change', function () {
                refreshCarOptions();
                refreshSubmit();
            });
        }
        [nameInput, idInput, addressInput, contactInput, travelInput].forEach(function (input) {
            if (input) {
                input.addEventListener('input', refreshSubmit);
            }
        });

        function initEmployeeNicLookup() {
            if (!idInput) {
                return;
            }
            var lookupUrl = idInput.getAttribute('data-lookup-nic-url');
            var hint = byId('nh-employee-hire-hint');
            var hintText = byId('nh-employee-hire-hint-text');
            if (!lookupUrl || !hint || !hintText) {
                return;
            }

            var lookupTimer = null;

            function hideHint() {
                hint.classList.add('d-none');
                hintText.textContent = '';
            }

            function checkNic() {
                var nic = idInput.value.trim();
                if (!nic) {
                    hideHint();
                    return;
                }
                fetch(lookupUrl + '?nic=' + encodeURIComponent(nic), {
                    headers: { 'Accept': 'application/json' }
                })
                    .then(function (res) {
                        return res.ok ? res.json() : { matched: false };
                    })
                    .then(function (data) {
                        if (data && data.matched) {
                            hintText.textContent = 'Employee match: ' + data.name
                                + ' — this hire will be zero charge.';
                            hint.classList.remove('d-none');
                        } else {
                            hideHint();
                        }
                    })
                    .catch(function () {
                        hideHint();
                    });
            }

            idInput.addEventListener('input', function () {
                clearTimeout(lookupTimer);
                lookupTimer = setTimeout(checkNic, 350);
            });
            idInput.addEventListener('blur', checkNic);
        }

        var modal = byId('newHireModal');
        if (modal) {
            modal.addEventListener('shown.bs.modal', loadCars);
        }

        initEmployeeNicLookup();
        showPickDates();
        loadCars();
        refreshSubmit();
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initHirePeriodForm);
    } else {
        initHirePeriodForm();
    }
})();
