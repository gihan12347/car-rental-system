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
        var addressInput = byId('nh-address');
        var contactInput = byId('nh-contact');

        if (!startInput || !endInput) {
            return;
        }

        function isCustomerComplete() {
            return nameInput && nameInput.value.trim() !== ''
                && addressInput && addressInput.value.trim() !== ''
                && contactInput && contactInput.value.trim() !== '';
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
            carSelect.innerHTML = '';
            var placeholder = document.createElement('option');
            placeholder.value = '';
            placeholder.textContent = 'Choose a vehicle…';
            carSelect.appendChild(placeholder);

            cars.forEach(function (car) {
                var opt = document.createElement('option');
                opt.value = String(car.id);
                opt.textContent = car.label;
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
            carSelect.addEventListener('change', refreshSubmit);
        }
        if (nameInput) {
            nameInput.addEventListener('input', refreshSubmit);
        }
        if (addressInput) {
            addressInput.addEventListener('input', refreshSubmit);
        }
        if (contactInput) {
            contactInput.addEventListener('input', refreshSubmit);
        }

        var modal = byId('newHireModal');
        if (modal) {
            modal.addEventListener('shown.bs.modal', loadCars);
        }

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
