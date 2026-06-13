/**
 * Shared Chart.js theme and helpers for FleetDesk.
 */
(function (global) {
    'use strict';

    if (typeof Chart === 'undefined') {
        return;
    }

    var COLORS = {
        primary: '#4f46e5',
        primaryRgb: '79, 70, 229',
        success: '#059669',
        successRgb: '5, 150, 105',
        warning: '#d97706',
        warningRgb: '217, 119, 6',
        sky: '#0ea5e9',
        pink: '#ec4899',
        violet: '#7c3aed',
        grid: '#e8eef4',
        muted: '#64748b',
        text: '#334155',
        surface: '#ffffff'
    };

    var PALETTE = [
        COLORS.primary,
        COLORS.success,
        COLORS.warning,
        COLORS.sky,
        COLORS.pink,
        COLORS.violet
    ];

    function applyDefaults() {
        Chart.defaults.font.family = '"Plus Jakarta Sans", system-ui, sans-serif';
        Chart.defaults.font.size = 12;
        Chart.defaults.color = COLORS.muted;
        Chart.defaults.plugins.tooltip.backgroundColor = '#0f172a';
        Chart.defaults.plugins.tooltip.titleColor = '#f8fafc';
        Chart.defaults.plugins.tooltip.bodyColor = '#e2e8f0';
        Chart.defaults.plugins.tooltip.borderColor = 'rgba(255,255,255,0.08)';
        Chart.defaults.plugins.tooltip.borderWidth = 1;
        Chart.defaults.plugins.tooltip.padding = 12;
        Chart.defaults.plugins.tooltip.cornerRadius = 8;
        Chart.defaults.plugins.tooltip.displayColors = true;
        Chart.defaults.plugins.tooltip.boxPadding = 4;
        Chart.defaults.animation.duration = 900;
        Chart.defaults.animation.easing = 'easeOutQuart';
        Chart.defaults.transitions.active.animation.duration = 350;
        Chart.defaults.transitions.resize.animation.duration = 400;
    }

    function prefersReducedMotion() {
        return typeof window !== 'undefined'
            && window.matchMedia
            && window.matchMedia('(prefers-reduced-motion: reduce)').matches;
    }

    function buildAnimation(type, animOpts) {
        animOpts = animOpts || {};
        if (prefersReducedMotion()) {
            return { animation: { duration: 0 }, animations: {} };
        }

        var baseDelay = animOpts.baseDelay || 0;
        var stagger = animOpts.stagger != null ? animOpts.stagger : 85;
        var duration = animOpts.duration || 1500;

        function delayFn(context) {
            if (context.type === 'data' && context.mode === 'default') {
                return baseDelay + context.dataIndex * stagger;
            }
            return baseDelay;
        }

        if (type === 'doughnut') {
            return {
                animation: {
                    duration: duration,
                    easing: 'easeOutQuart',
                    delay: delayFn
                },
                animations: {
                    circumference: {
                        type: 'number',
                        from: 0,
                        duration: duration,
                        easing: 'easeOutQuart',
                        delay: delayFn
                    }
                }
            };
        }

        if (type === 'line') {
            return {
                animation: {
                    duration: duration,
                    easing: 'easeOutQuart',
                    delay: delayFn
                },
                animations: {
                    y: {
                        type: 'number',
                        from: 0,
                        duration: duration,
                        easing: 'easeOutQuart',
                        delay: delayFn
                    },
                    radius: {
                        type: 'number',
                        from: 0,
                        duration: duration * 0.65,
                        easing: 'easeOutQuart',
                        delay: delayFn
                    },
                    x: false
                }
            };
        }

        return {
            animation: {
                duration: duration,
                easing: 'easeOutQuart',
                delay: delayFn
            },
            animations: {
                y: {
                    type: 'number',
                    from: 0,
                    duration: duration,
                    easing: 'easeOutQuart',
                    delay: delayFn
                },
                x: false
            }
        };
    }

    function applyAnimationToOptions(options, type, animOpts) {
        var anim = buildAnimation(type, animOpts);
        options.animation = Object.assign({}, options.animation || {}, anim.animation);
        options.animations = Object.assign({}, options.animations || {}, anim.animations);
        return options;
    }

    function markChartDrawing(canvasId) {
        var canvas = document.getElementById(canvasId);
        var shell = canvas ? canvas.closest('.dashboard-chart-shell') : null;
        if (shell) {
            shell.classList.add('is-chart-drawing');
        }
    }

    function markChartReady(canvasId) {
        var canvas = document.getElementById(canvasId);
        var shell = canvas ? canvas.closest('.dashboard-chart-shell') : null;
        if (shell) {
            shell.classList.add('is-chart-ready');
        }
    }

    function withReadyCallback(canvasId, options) {
        if (!options.animation || typeof options.animation !== 'object') {
            options.animation = {};
        }
        var userComplete = options.animation.onComplete;
        options.animation.onComplete = function () {
            markChartReady(canvasId);
            if (typeof userComplete === 'function') {
                userComplete.apply(this, arguments);
            }
        };
        return options;
    }

    applyDefaults();

    function formatNumber(value) {
        var n = typeof value === 'number' ? value : parseFloat(String(value));
        if (isNaN(n)) {
            return '0';
        }
        return n.toLocaleString(undefined, { maximumFractionDigits: 2 });
    }

    function formatCurrency(value) {
        return formatNumber(value);
    }

    function formatCompact(value) {
        var n = typeof value === 'number' ? value : parseFloat(String(value));
        if (isNaN(n)) {
            return '0';
        }
        if (Math.abs(n) >= 1000000) {
            return (n / 1000000).toFixed(1).replace(/\.0$/, '') + 'M';
        }
        if (Math.abs(n) >= 1000) {
            return (n / 1000).toFixed(1).replace(/\.0$/, '') + 'K';
        }
        return formatNumber(n);
    }

    function labels(points) {
        return (points || []).map(function (p) { return p.label; });
    }

    function values(points) {
        return (points || []).map(function (p) {
            var v = p.value;
            if (v == null) {
                return 0;
            }
            if (typeof v === 'number') {
                return v;
            }
            return parseFloat(String(v)) || 0;
        });
    }

    function hasData(points) {
        if (!points || !points.length) {
            return false;
        }
        return values(points).some(function (v) { return v > 0; });
    }

    function createGradient(ctx, rgb, topAlpha, bottomAlpha) {
        var g = ctx.createLinearGradient(0, 0, 0, ctx.canvas.height || 200);
        g.addColorStop(0, 'rgba(' + rgb + ', ' + topAlpha + ')');
        g.addColorStop(1, 'rgba(' + rgb + ', ' + bottomAlpha + ')');
        return g;
    }

    function currencyTooltip() {
        return {
            callbacks: {
                label: function (ctx) {
                    var label = ctx.dataset.label ? ctx.dataset.label + ': ' : '';
                    var val = ctx.parsed.y != null ? ctx.parsed.y : ctx.parsed;
                    if (typeof val === 'object' && val !== null) {
                        val = val.y != null ? val.y : 0;
                    }
                    return label + formatCurrency(val);
                }
            }
        };
    }

    function baseCartesianOptions(extra, chartType, animOpts) {
        var opts = {
            responsive: true,
            maintainAspectRatio: false,
            interaction: { mode: 'index', intersect: false },
            plugins: {
                legend: { display: false },
                tooltip: currencyTooltip()
            },
            scales: {
                x: {
                    grid: { display: false },
                    border: { display: false },
                    ticks: {
                        color: COLORS.muted,
                        maxRotation: 45,
                        minRotation: 0,
                        padding: 6,
                        font: { size: 11, weight: '500' }
                    }
                },
                y: {
                    beginAtZero: true,
                    border: { display: false, dash: [4, 4] },
                    grid: { color: COLORS.grid, drawBorder: false },
                    ticks: {
                        color: COLORS.muted,
                        padding: 8,
                        font: { size: 11 },
                        callback: function (v) { return formatCompact(v); }
                    }
                }
            },
            animation: { duration: 1500, easing: 'easeOutQuart' }
        };
        applyAnimationToOptions(opts, chartType || 'bar', animOpts);
        if (extra) {
            Object.keys(extra).forEach(function (key) {
                opts[key] = extra[key];
            });
        }
        return opts;
    }

    function toggleEmpty(canvasId, emptyId, show) {
        var canvas = document.getElementById(canvasId);
        var empty = emptyId ? document.getElementById(emptyId) : null;
        var shell = canvas ? canvas.closest('.dashboard-chart-shell') : null;
        if (shell) {
            shell.classList.toggle('dashboard-chart-shell--empty', !!show);
        }
        if (empty) {
            empty.hidden = !show;
        }
    }

    function lineChart(canvasId, points, emptyId, animOpts) {
        var el = document.getElementById(canvasId);
        if (!el) {
            return null;
        }
        if (!hasData(points)) {
            toggleEmpty(canvasId, emptyId, true);
            markChartReady(canvasId);
            return null;
        }
        toggleEmpty(canvasId, emptyId, false);
        var ctx = el.getContext('2d');
        var vals = values(points);
        markChartDrawing(canvasId);
        return new Chart(el, {
            type: 'line',
            data: {
                labels: labels(points),
                datasets: [{
                    label: 'Revenue',
                    data: vals,
                    borderColor: COLORS.primary,
                    backgroundColor: createGradient(ctx, COLORS.primaryRgb, 0.28, 0.02),
                    fill: true,
                    tension: 0.42,
                    borderWidth: 2.5,
                    pointRadius: vals.map(function (v) { return v > 0 ? 5 : 0; }),
                    pointHoverRadius: 8,
                    pointBackgroundColor: COLORS.surface,
                    pointBorderColor: COLORS.primary,
                    pointBorderWidth: 2,
                    pointHitRadius: 12,
                    spanGaps: true
                }]
            },
            options: withReadyCallback(canvasId, baseCartesianOptions(null, 'line', animOpts))
        });
    }

    function barChart(canvasId, points, emptyId, animOpts) {
        var el = document.getElementById(canvasId);
        if (!el) {
            return null;
        }
        if (!hasData(points)) {
            toggleEmpty(canvasId, emptyId, true);
            markChartReady(canvasId);
            return null;
        }
        toggleEmpty(canvasId, emptyId, false);
        var ctx = el.getContext('2d');
        var vals = values(points);
        markChartDrawing(canvasId);
        return new Chart(el, {
            type: 'bar',
            data: {
                labels: labels(points),
                datasets: [{
                    label: 'Revenue',
                    data: vals,
                    backgroundColor: vals.map(function (_, i) {
                        return i % 2 === 0
                            ? createGradient(ctx, COLORS.primaryRgb, 0.95, 0.55)
                            : createGradient(ctx, COLORS.primaryRgb, 0.75, 0.4);
                    }),
                    borderRadius: 8,
                    borderSkipped: false,
                    maxBarThickness: 52
                }]
            },
            options: withReadyCallback(canvasId, baseCartesianOptions({
                scales: {
                    x: {
                        grid: { display: false },
                        border: { display: false },
                        ticks: {
                            color: COLORS.muted,
                            maxRotation: 45,
                            minRotation: 0,
                            padding: 6,
                            font: { size: 11, weight: '500' }
                        }
                    },
                    y: {
                        beginAtZero: true,
                        border: { display: false },
                        grid: { color: COLORS.grid },
                        ticks: {
                            color: COLORS.muted,
                            padding: 8,
                            callback: function (v) { return formatCompact(v); }
                        }
                    }
                }
            }, 'bar', animOpts))
        });
    }

    function doughnutChart(canvasId, points, emptyId, animOpts) {
        var el = document.getElementById(canvasId);
        if (!el) {
            return null;
        }
        if (!hasData(points)) {
            toggleEmpty(canvasId, emptyId, true);
            markChartReady(canvasId);
            return null;
        }
        toggleEmpty(canvasId, emptyId, false);
        var vals = values(points);
        var total = vals.reduce(function (a, b) { return a + b; }, 0);
        if (total <= 0) {
            toggleEmpty(canvasId, emptyId, true);
            markChartReady(canvasId);
            return null;
        }
        markChartDrawing(canvasId);
        return new Chart(el, {
            type: 'doughnut',
            data: {
                labels: labels(points),
                datasets: [{
                    data: vals,
                    backgroundColor: PALETTE.slice(0, vals.length),
                    borderColor: COLORS.surface,
                    borderWidth: 3,
                    hoverBorderColor: COLORS.surface,
                    hoverOffset: 8
                }]
            },
            options: withReadyCallback(canvasId, applyAnimationToOptions({
                responsive: true,
                maintainAspectRatio: false,
                cutout: '62%',
                plugins: {
                    legend: {
                        position: 'bottom',
                        labels: {
                            color: COLORS.text,
                            boxWidth: 10,
                            boxHeight: 10,
                            padding: 14,
                            usePointStyle: true,
                            pointStyle: 'circle',
                            font: { size: 11, weight: '500' }
                        }
                    },
                    tooltip: {
                        callbacks: {
                            label: function (ctx) {
                                var val = ctx.parsed || 0;
                                var pct = total > 0 ? ((val / total) * 100).toFixed(1) : '0';
                                return ctx.label + ': ' + formatCurrency(val) + ' (' + pct + '%)';
                            }
                        }
                    }
                }
            }, 'doughnut', animOpts))
        });
    }

    function groupedBarChart(canvasId, incomePoints, expensePoints, emptyId, animOpts) {
        var el = document.getElementById(canvasId);
        if (!el) {
            return null;
        }
        var incomeVals = values(incomePoints);
        var expenseVals = values(expensePoints);
        var hasIncome = incomeVals.some(function (v) { return v > 0; });
        var hasExpense = expenseVals.some(function (v) { return v > 0; });
        if (!hasIncome && !hasExpense) {
            toggleEmpty(canvasId, emptyId, true);
            markChartReady(canvasId);
            return null;
        }
        toggleEmpty(canvasId, emptyId, false);
        var ctx = el.getContext('2d');
        var chartLabels = labels(incomePoints.length ? incomePoints : expensePoints);
        markChartDrawing(canvasId);
        return new Chart(el, {
            type: 'bar',
            data: {
                labels: chartLabels,
                datasets: [
                    {
                        label: 'Income',
                        data: incomeVals,
                        backgroundColor: createGradient(ctx, COLORS.primaryRgb, 0.9, 0.5),
                        borderRadius: 6,
                        borderSkipped: false,
                        maxBarThickness: 40
                    },
                    {
                        label: 'Maintenance',
                        data: expenseVals,
                        backgroundColor: createGradient(ctx, COLORS.warningRgb, 0.9, 0.5),
                        borderRadius: 6,
                        borderSkipped: false,
                        maxBarThickness: 40
                    }
                ]
            },
            options: withReadyCallback(canvasId, applyAnimationToOptions({
                responsive: true,
                maintainAspectRatio: false,
                interaction: { mode: 'index', intersect: false },
                plugins: {
                    legend: {
                        position: 'bottom',
                        labels: {
                            color: COLORS.text,
                            boxWidth: 10,
                            usePointStyle: true,
                            pointStyle: 'circle',
                            padding: 14,
                            font: { size: 11, weight: '600' }
                        }
                    },
                    tooltip: currencyTooltip()
                },
                scales: {
                    x: {
                        grid: { display: false },
                        border: { display: false },
                        ticks: { color: COLORS.muted, maxRotation: 45, font: { size: 11 } }
                    },
                    y: {
                        beginAtZero: true,
                        border: { display: false },
                        grid: { color: COLORS.grid },
                        ticks: {
                            color: COLORS.muted,
                            callback: function (v) { return formatCompact(v); }
                        }
                    }
                }
            }, 'bar', animOpts))
        });
    }

    global.FleetCharts = {
        colors: COLORS,
        lineChart: lineChart,
        barChart: barChart,
        doughnutChart: doughnutChart,
        groupedBarChart: groupedBarChart,
        hasData: hasData,
        formatCurrency: formatCurrency
    };
})(typeof window !== 'undefined' ? window : this);
