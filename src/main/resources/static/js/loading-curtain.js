/**
 * Blur curtain + circular logo loader on POST form submissions (login and app-wide).
 */
(function (global) {
    'use strict';

    var OVERLAY_ID = 'appLoadingCurtain';
    var shown = false;

    function logoUrl() {
        if (global.FleetDeskLoading && global.FleetDeskLoading.logoUrl) {
            return global.FleetDeskLoading.logoUrl;
        }
        return '/images/loading-logo.png';
    }

    function loaderMarkup() {
        return (
            '<div class="app-loading-curtain__loader" aria-hidden="true">' +
            '  <div class="app-loading-curtain__logo-ring">' +
            '    <img class="app-loading-curtain__logo" alt="" decoding="async" role="presentation"/>' +
            '  </div>' +
            '</div>'
        );
    }

    function refreshLoader(overlay) {
        if (!overlay) {
            return;
        }
        var logo = overlay.querySelector('.app-loading-curtain__logo');
        if (!logo) {
            return;
        }
        var nextSrc = logoUrl();
        if (logo.getAttribute('src') !== nextSrc) {
            logo.setAttribute('src', nextSrc);
        }
    }

    function buildOverlay() {
        var existing = document.getElementById(OVERLAY_ID);
        if (existing) {
            refreshLoader(existing);
            return existing;
        }

        var root = document.createElement('div');
        root.id = OVERLAY_ID;
        root.className = 'app-loading-curtain';
        root.setAttribute('role', 'alertdialog');
        root.setAttribute('aria-modal', 'true');
        root.setAttribute('aria-labelledby', 'appLoadingCurtainTitle');
        root.setAttribute('aria-hidden', 'true');
        root.hidden = true;

        root.innerHTML =
            '<div class="app-loading-curtain__blur" aria-hidden="true"></div>' +
            '<div class="app-loading-curtain__stage">' +
                 loaderMarkup() +
            '  <p class="app-loading-curtain__title" id="appLoadingCurtainTitle">Saving your changes…</p>' +
            '  <p class="app-loading-curtain__hint">Please wait</p>' +
            '  <div class="app-loading-curtain__progress" aria-hidden="true">' +
            '    <span class="app-loading-curtain__progress-bar"></span>' +
            '  </div>' +
            '</div>';

        document.body.appendChild(root);
        refreshLoader(root);
        return root;
    }

    function show(message, options) {
        options = options || {};
        if (shown) {
            return;
        }
        shown = true;

        var overlay = buildOverlay();
        refreshLoader(overlay);

        var title = overlay.querySelector('.app-loading-curtain__title');
        if (title && message) {
            title.textContent = message;
        }

        var hint = overlay.querySelector('.app-loading-curtain__hint');
        if (hint) {
            hint.textContent = options.hint || 'Please wait';
        }

        overlay.hidden = false;
        overlay.setAttribute('aria-hidden', 'false');
        document.body.classList.add('app-loading-curtain-open');

        requestAnimationFrame(function () {
            overlay.classList.add('is-active');
        });
    }

    function hide() {
        shown = false;
        var overlay = document.getElementById(OVERLAY_ID);
        if (!overlay) {
            return;
        }
        overlay.classList.remove('is-active');
        overlay.setAttribute('aria-hidden', 'true');
        document.body.classList.remove('app-loading-curtain-open');

        window.setTimeout(function () {
            if (!overlay.classList.contains('is-active')) {
                overlay.hidden = true;
            }
        }, 350);
    }

    function isPostForm(form) {
        if (!form || form.tagName !== 'FORM') {
            return false;
        }
        if (form.dataset && form.dataset.skipLoading === 'true') {
            return false;
        }
        var method = (form.getAttribute('method') || 'get').trim().toLowerCase();
        return method === 'post';
    }

    function messageForForm(form) {
        if (form.dataset && form.dataset.loadingMessage) {
            return form.dataset.loadingMessage;
        }
        var submitter = form.querySelector('[type="submit"]');
        if (submitter && submitter.dataset && submitter.dataset.loadingMessage) {
            return submitter.dataset.loadingMessage;
        }
        if (form.id === 'loginForm' || (form.action && form.action.indexOf('login') !== -1)) {
            return 'Signing in…';
        }
        return 'Saving your changes…';
    }

    function hintForForm(form) {
        if (form.dataset && form.dataset.loadingHint) {
            return form.dataset.loadingHint;
        }
        if (form.id === 'loginForm' || (form.action && form.action.indexOf('login') !== -1)) {
            return 'Opening your dashboard…';
        }
        return 'Please wait';
    }

    function onFormSubmit(event) {
        var form = event.target;
        if (!isPostForm(form)) {
            return;
        }
        show(messageForForm(form), {
            hint: hintForForm(form)
        });
    }

    function init() {
        buildOverlay();
        document.addEventListener('submit', onFormSubmit, true);
        window.addEventListener('pageshow', function (event) {
            if (event.persisted) {
                hide();
            }
        });
        window.addEventListener('pagehide', hide);
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }

    global.FleetDeskLoadingCurtain = { show: show, hide: hide };
})(typeof window !== 'undefined' ? window : this);
