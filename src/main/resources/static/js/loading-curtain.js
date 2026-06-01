/**
 * Full-screen curtain loader on POST form submissions.
 * Video: /media/car-trunk-loading.mp4 (white backdrop removed via blend on dark stage).
 */
(function (global) {
    'use strict';

    var OVERLAY_ID = 'appLoadingCurtain';
    var shown = false;

    function mediaUrl() {
        if (global.FleetDeskLoading && global.FleetDeskLoading.mediaUrl) {
            return global.FleetDeskLoading.mediaUrl;
        }
        return '/media/car-trunk-loading.mp4';
    }

    function buildOverlay() {
        var existing = document.getElementById(OVERLAY_ID);
        if (existing) {
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
            '<div class="app-loading-curtain__veil" aria-hidden="true"></div>' +
            '<div class="app-loading-curtain__panel app-loading-curtain__panel--top" aria-hidden="true"></div>' +
            '<div class="app-loading-curtain__panel app-loading-curtain__panel--bottom" aria-hidden="true"></div>' +
            '<div class="app-loading-curtain__stage">' +
            '  <div class="app-loading-curtain__glow" aria-hidden="true"></div>' +
            '  <div class="app-loading-curtain__video-wrap">' +
            '    <video class="app-loading-curtain__video" muted loop playsinline preload="auto" aria-hidden="true"></video>' +
            '  </div>' +
            '  <p class="app-loading-curtain__title" id="appLoadingCurtainTitle">Saving your changes…</p>' +
            '  <p class="app-loading-curtain__hint">Please wait</p>' +
            '  <div class="app-loading-curtain__progress" aria-hidden="true">' +
            '    <span class="app-loading-curtain__progress-bar"></span>' +
            '  </div>' +
            '</div>';

        var video = root.querySelector('.app-loading-curtain__video');
        if (video) {
            video.src = mediaUrl();
        }

        document.body.appendChild(root);
        return root;
    }

    function playVideo(overlay) {
        var video = overlay.querySelector('.app-loading-curtain__video');
        if (!video) {
            return;
        }
        video.currentTime = 0;
        var playPromise = video.play();
        if (playPromise && typeof playPromise.catch === 'function') {
            playPromise.catch(function () { /* autoplay blocked */ });
        }
    }

    function pauseVideo(overlay) {
        var video = overlay.querySelector('.app-loading-curtain__video');
        if (video) {
            video.pause();
        }
    }

    function show(message) {
        if (shown) {
            return;
        }
        shown = true;

        var overlay = buildOverlay();
        var title = overlay.querySelector('.app-loading-curtain__title');
        if (title && message) {
            title.textContent = message;
        }

        overlay.hidden = false;
        overlay.setAttribute('aria-hidden', 'false');
        document.body.classList.add('app-loading-curtain-open');

        requestAnimationFrame(function () {
            overlay.classList.add('is-active');
            playVideo(overlay);
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
        pauseVideo(overlay);

        window.setTimeout(function () {
            if (!overlay.classList.contains('is-active')) {
                overlay.hidden = true;
            }
        }, 450);
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
        if (form.id === 'loginForm' || form.action && form.action.indexOf('login') !== -1) {
            return 'Signing in…';
        }
        return 'Saving your changes…';
    }

    function onFormSubmit(event) {
        var form = event.target;
        if (!isPostForm(form)) {
            return;
        }
        show(messageForForm(form));
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
