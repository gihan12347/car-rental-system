/**
 * Blur curtain + circular logo loader on POST/GET form submissions and in-app navigation.
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
            '  <p class="app-loading-curtain__title" id="appLoadingCurtainTitle">Loading…</p>' +
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

    function shouldSkipLoading(el) {
        return !!(el && el.dataset && el.dataset.skipLoading === 'true');
    }

    function formMethod(form) {
        return (form.getAttribute('method') || 'get').trim().toLowerCase();
    }

    function isNavigatingForm(form) {
        if (!form || form.tagName !== 'FORM') {
            return false;
        }
        if (shouldSkipLoading(form)) {
            return false;
        }
        var method = formMethod(form);
        return method === 'post' || method === 'get';
    }

    function isPostForm(form) {
        return isNavigatingForm(form) && formMethod(form) === 'post';
    }

    function isGetForm(form) {
        return isNavigatingForm(form) && formMethod(form) === 'get';
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
        if (isGetForm(form)) {
            if (form.classList.contains('dashboard-period-form')) {
                if (document.body.classList.contains('dashboard-page')) {
                    return 'Updating dashboard…';
                }
                return 'Updating period…';
            }
            if (form.getAttribute('role') === 'search' || form.classList.contains('app-page-search-form')) {
                return 'Searching…';
            }
            return 'Loading page…';
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
        if (form.classList.contains('dashboard-period-form') && document.body.classList.contains('dashboard-page')) {
            return 'Refreshing charts and stats…';
        }
        return 'Please wait';
    }

    function submitForm(form) {
        if (!form) {
            return;
        }
        if (isNavigatingForm(form)) {
            show(messageForForm(form), {
                hint: hintForForm(form)
            });
        }
        if (typeof form.requestSubmit === 'function') {
            form.requestSubmit();
            return;
        }
        form.submit();
    }

    function isModifiedNavigation(event) {
        return event.defaultPrevented
            || event.button !== 0
            || event.metaKey
            || event.ctrlKey
            || event.shiftKey
            || event.altKey;
    }

    function resolveUrl(href) {
        try {
            return new URL(href, window.location.href);
        } catch (error) {
            return null;
        }
    }

    function shouldShowLinkLoading(anchor, event) {
        if (!anchor || anchor.tagName !== 'A') {
            return false;
        }
        if (isModifiedNavigation(event)) {
            return false;
        }
        if (shouldSkipLoading(anchor)) {
            return false;
        }

        var rawHref = anchor.getAttribute('href');
        if (!rawHref || rawHref.charAt(0) === '#') {
            return false;
        }
        if (/^javascript:/i.test(rawHref.trim())) {
            return false;
        }
        if (anchor.hasAttribute('download')) {
            return false;
        }

        var target = (anchor.getAttribute('target') || '').toLowerCase();
        if (target === '_blank' || target === '_parent' || target === '_top') {
            return false;
        }
        if (anchor.hasAttribute('data-bs-toggle')
            || anchor.hasAttribute('data-bs-dismiss')
            || anchor.hasAttribute('data-app-open-modal')) {
            return false;
        }

        var url = resolveUrl(anchor.href);
        if (!url || url.origin !== window.location.origin) {
            return false;
        }

        return url.pathname + url.search + url.hash !== window.location.pathname
            + window.location.search
            + window.location.hash;
    }

    function messageForLink(anchor) {
        if (anchor.dataset && anchor.dataset.loadingMessage) {
            return anchor.dataset.loadingMessage;
        }
        return 'Loading page…';
    }

    function hintForLink(anchor) {
        if (anchor.dataset && anchor.dataset.loadingHint) {
            return anchor.dataset.loadingHint;
        }
        return 'Please wait';
    }

    function onFormSubmit(event) {
        var form = event.target;
        if (!isNavigatingForm(form)) {
            return;
        }
        show(messageForForm(form), {
            hint: hintForForm(form)
        });
    }

    function onLinkClick(event) {
        var anchor = event.target.closest('a');
        if (!shouldShowLinkLoading(anchor, event)) {
            return;
        }
        show(messageForLink(anchor), {
            hint: hintForLink(anchor)
        });
    }

    function init() {
        buildOverlay();
        document.addEventListener('submit', onFormSubmit, true);
        document.addEventListener('click', onLinkClick, true);
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

    global.FleetDeskLoadingCurtain = { show: show, hide: hide, submitForm: submitForm };
})(typeof window !== 'undefined' ? window : this);
