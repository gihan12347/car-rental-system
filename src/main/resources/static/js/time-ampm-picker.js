/**
 * 12-hour time picker (hour, minute, AM/PM) synced to a hidden HH:mm input for Spring LocalTime binding.
 */
(function (global) {
    'use strict';

    function pad2(n) {
        return String(n).padStart(2, '0');
    }

    function parse24h(value) {
        if (!value || typeof value !== 'string') {
            return null;
        }
        var parts = value.trim().split(':');
        if (parts.length < 2) {
            return null;
        }
        var hour24 = parseInt(parts[0], 10);
        var minute = parseInt(parts[1].replace(/\D.*$/, ''), 10);
        if (isNaN(hour24) || isNaN(minute) || hour24 < 0 || hour24 > 23 || minute < 0 || minute > 59) {
            return null;
        }
        var ampm = hour24 >= 12 ? 'PM' : 'AM';
        var hour12 = hour24 % 12;
        if (hour12 === 0) {
            hour12 = 12;
        }
        return {
            hour: String(hour12),
            minute: pad2(minute),
            ampm: ampm
        };
    }

    function to24h(hour12, minute, ampm) {
        var h = parseInt(hour12, 10);
        var m = parseInt(minute, 10);
        if (isNaN(h) || isNaN(m) || h < 1 || h > 12 || m < 0 || m > 59) {
            return '';
        }
        var hour24 = h % 12;
        if (ampm === 'PM') {
            hour24 += 12;
        }
        return pad2(hour24) + ':' + pad2(m);
    }

    function notifyChange(hidden) {
        hidden.dispatchEvent(new Event('input', { bubbles: true }));
        hidden.dispatchEvent(new Event('change', { bubbles: true }));
    }

    function syncFromPicker(wrap) {
        var targetId = wrap.getAttribute('data-time-target');
        var hidden = targetId ? document.getElementById(targetId) : null;
        if (!hidden) {
            return;
        }
        var hourSel = wrap.querySelector('.time-ampm-picker__hour');
        var minSel = wrap.querySelector('.time-ampm-picker__minute');
        var ampmSel = wrap.querySelector('.time-ampm-picker__ampm');
        if (!hourSel || !minSel || !ampmSel) {
            return;
        }
        var next = to24h(hourSel.value, minSel.value, ampmSel.value);
        if (next && hidden.value !== next) {
            hidden.value = next;
            notifyChange(hidden);
        }
    }

    function syncToPicker(wrap) {
        var targetId = wrap.getAttribute('data-time-target');
        var hidden = targetId ? document.getElementById(targetId) : null;
        if (!hidden) {
            return;
        }
        var hourSel = wrap.querySelector('.time-ampm-picker__hour');
        var minSel = wrap.querySelector('.time-ampm-picker__minute');
        var ampmSel = wrap.querySelector('.time-ampm-picker__ampm');
        if (!hourSel || !minSel || !ampmSel) {
            return;
        }
        var parsed = parse24h(hidden.value);
        if (!parsed) {
            var form = wrap.closest('form');
            var fallback = form && form.getAttribute('data-planned-return-time');
            parsed = parse24h(fallback);
        }
        if (!parsed) {
            parsed = { hour: '9', minute: '00', ampm: 'AM' };
        }
        hourSel.value = parsed.hour;
        minSel.value = parsed.minute;
        ampmSel.value = parsed.ampm;
        if (!parse24h(hidden.value)) {
            var synced = to24h(parsed.hour, parsed.minute, parsed.ampm);
            if (synced) {
                hidden.value = synced;
            }
        }
    }

    function ensureMinuteOptions(minSel) {
        if (!minSel || minSel.options.length > 0) {
            return;
        }
        for (var m = 0; m < 60; m += 1) {
            var opt = document.createElement('option');
            opt.value = pad2(m);
            opt.textContent = pad2(m);
            minSel.appendChild(opt);
        }
    }

    function initPicker(wrap) {
        if (wrap.dataset.timeAmPmReady === 'true') {
            syncToPicker(wrap);
            return;
        }
        wrap.dataset.timeAmPmReady = 'true';
        ensureMinuteOptions(wrap.querySelector('.time-ampm-picker__minute'));
        syncToPicker(wrap);

        var hourSel = wrap.querySelector('.time-ampm-picker__hour');
        var minSel = wrap.querySelector('.time-ampm-picker__minute');
        var ampmSel = wrap.querySelector('.time-ampm-picker__ampm');
        [hourSel, minSel, ampmSel].forEach(function (sel) {
            if (sel) {
                sel.addEventListener('change', function () {
                    syncFromPicker(wrap);
                });
            }
        });

        var targetId = wrap.getAttribute('data-time-target');
        var hidden = targetId ? document.getElementById(targetId) : null;
        if (hidden) {
            hidden.addEventListener('change', function () {
                syncToPicker(wrap);
            });
        }

        var form = wrap.closest('form');
        if (form) {
            form.addEventListener('submit', function () {
                syncFromPicker(wrap);
            });
        }
    }

    function initAll(root) {
        var scope = root || document;
        scope.querySelectorAll('.time-ampm-picker').forEach(initPicker);
    }

    global.FleetDeskTimeAmPm = {
        initAll: initAll,
        parse24h: parse24h,
        to24h: to24h
    };

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', function () {
            initAll(document);
        });
    } else {
        initAll(document);
    }

    document.addEventListener('shown.bs.modal', function (event) {
        if (event.target) {
            initAll(event.target);
        }
    });
})(typeof window !== 'undefined' ? window : this);
