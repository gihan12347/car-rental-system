(function (global) {
    'use strict';

    function prefersReducedMotion() {
        return global.matchMedia && global.matchMedia('(prefers-reduced-motion: reduce)').matches;
    }

    function formatCountValue(value, decimals) {
        return value.toLocaleString(undefined, {
            minimumFractionDigits: decimals,
            maximumFractionDigits: decimals
        });
    }

    function renderCount(el, value) {
        var decimals = parseInt(el.getAttribute('data-count-decimals') || '0', 10);
        var suffix = el.getAttribute('data-count-suffix') || '';
        var prefix = el.getAttribute('data-count-prefix') || '';
        el.textContent = prefix + formatCountValue(value, decimals) + suffix;
    }

    function animateCountUp(el, target, opts) {
        opts = opts || {};
        var duration = opts.duration != null ? opts.duration : 1500;
        var delay = opts.delay != null ? opts.delay : 0;

        if (prefersReducedMotion() || duration <= 0) {
            renderCount(el, target);
            return;
        }

        var decimals = parseInt(el.getAttribute('data-count-decimals') || '0', 10);
        var suffix = el.getAttribute('data-count-suffix') || '';
        var prefix = el.getAttribute('data-count-prefix') || '';

        window.setTimeout(function () {
            el.classList.add('is-counting');
            var startTime = null;

            function step(timestamp) {
                if (!startTime) {
                    startTime = timestamp;
                }
                var progress = Math.min((timestamp - startTime) / duration, 1);
                var eased = 1 - Math.pow(1 - progress, 4);
                var current = target * eased;
                el.textContent = prefix + formatCountValue(current, decimals) + suffix;
                if (progress < 1) {
                    window.requestAnimationFrame(step);
                } else {
                    renderCount(el, target);
                    el.classList.remove('is-counting');
                    el.classList.add('is-counted');
                }
            }

            el.textContent = prefix + formatCountValue(0, decimals) + suffix;
            window.requestAnimationFrame(step);
        }, delay);
    }

    function initIn(root, options) {
        root = root || document;
        options = options || {};
        var duration = options.duration != null ? options.duration : 1500;
        var delayStep = options.delayStep != null ? options.delayStep : 90;
        var index = 0;

        root.querySelectorAll('[data-count-to]').forEach(function (el) {
            if (el.getAttribute('data-count-init') === '1') {
                return;
            }
            var target = parseFloat(el.getAttribute('data-count-to'));
            if (isNaN(target)) {
                return;
            }
            el.setAttribute('data-count-init', '1');
            animateCountUp(el, target, {
                duration: duration,
                delay: index * delayStep
            });
            index += 1;
        });
    }

    function autoInit() {
        initIn(document);
    }

    global.FleetCountUp = {
        init: initIn,
        initIn: initIn,
        animate: animateCountUp
    };

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', autoInit);
    } else {
        autoInit();
    }
})(typeof window !== 'undefined' ? window : this);
