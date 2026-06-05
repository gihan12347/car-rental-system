/**
 * Full-screen curtain loader on POST form submissions.
 * Default: car-trunk video. Login: light see-through curtain + loader.
 */
(function (global) {
    'use strict';

    var OVERLAY_ID = 'appLoadingCurtain';
    var shown = false;
    var activeVariant = 'default';

    function mediaUrl() {
        if (global.FleetDeskLoading && global.FleetDeskLoading.mediaUrl) {
            return global.FleetDeskLoading.mediaUrl;
        }
        return '/media/car-trunk-loading.mp4';
    }

    function loginLoadingGifUrl() {
        if (global.FleetDeskLoading && global.FleetDeskLoading.loginGifUrl) {
            return global.FleetDeskLoading.loginGifUrl;
        }
        return '/images/login-loading.gif';
    }

    function loginLoaderMarkup() {
        return (
            '<div class="app-loading-curtain__login-loader" aria-hidden="true">' +
            '  <img class="app-loading-curtain__login-gif" alt="" decoding="async" role="presentation"/>' +
            '  <div class="login-dots-loader" role="presentation" hidden>' +
            '    <span class="login-dots-loader__dot"></span>' +
            '    <span class="login-dots-loader__dot"></span>' +
            '    <span class="login-dots-loader__dot"></span>' +
            '  </div>' +
            '</div>'
        );
    }

    function refreshLoginLoader(overlay) {
        if (!overlay) {
            return;
        }
        var img = overlay.querySelector('.app-loading-curtain__login-gif');
        var dots = overlay.querySelector('.login-dots-loader');
        if (!img) {
            return;
        }
        if (dots) {
            dots.hidden = true;
        }
        img.hidden = false;
        img.onload = function () {
            img.hidden = false;
            if (dots) {
                dots.hidden = true;
            }
        };
        img.onerror = function () {
            img.hidden = true;
            if (dots) {
                dots.hidden = false;
            }
        };
        var nextSrc = loginLoadingGifUrl();
        if (img.getAttribute('src') !== nextSrc) {
            img.setAttribute('src', nextSrc);
        } else if (img.complete && img.naturalWidth === 0) {
            img.onerror();
        }
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
            '  <div class="app-loading-curtain__media app-loading-curtain__media--default">' +
            '    <div class="app-loading-curtain__video-wrap">' +
            '      <video class="app-loading-curtain__video" muted loop playsinline preload="auto" aria-hidden="true"></video>' +
            '    </div>' +
            '  </div>' +
            '  <div class="app-loading-curtain__media app-loading-curtain__media--login" hidden>' +
                 loginLoaderMarkup() +
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

    function resolveVariant(form) {
        if (form && form.dataset && form.dataset.loadingVariant) {
            return form.dataset.loadingVariant;
        }
        if (document.body.classList.contains('login-page')) {
            return 'login';
        }
        return 'default';
    }

    function applyVariant(overlay, variant) {
        activeVariant = variant === 'login' ? 'login' : 'default';
        overlay.classList.toggle('app-loading-curtain--login', activeVariant === 'login');

        var defaultMedia = overlay.querySelector('.app-loading-curtain__media--default');
        var loginMedia = overlay.querySelector('.app-loading-curtain__media--login');
        if (defaultMedia) {
            if (activeVariant === 'login') {
                defaultMedia.setAttribute('hidden', 'hidden');
            } else {
                defaultMedia.removeAttribute('hidden');
            }
        }
        if (loginMedia) {
            if (activeVariant === 'login') {
                loginMedia.removeAttribute('hidden');
                refreshLoginLoader(overlay);
            } else {
                loginMedia.setAttribute('hidden', 'hidden');
            }
        }
    }

    function show(message, options) {
        options = options || {};
        if (shown) {
            return;
        }
        shown = true;

        var overlay = buildOverlay();
        applyVariant(overlay, options.variant || 'default');

        var title = overlay.querySelector('.app-loading-curtain__title');
        if (title && message) {
            title.textContent = message;
        }

        var hint = overlay.querySelector('.app-loading-curtain__hint');
        if (hint) {
            hint.textContent = options.hint || (activeVariant === 'login' ? 'Opening your dashboard…' : 'Please wait');
        }

        overlay.hidden = false;
        overlay.setAttribute('aria-hidden', 'false');
        document.body.classList.add('app-loading-curtain-open');

        requestAnimationFrame(function () {
            overlay.classList.add('is-active');
            if (activeVariant === 'login') {
                pauseVideo(overlay);
            } else {
                playVideo(overlay);
            }
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

    function hintForForm(form) {
        if (form.dataset && form.dataset.loadingHint) {
            return form.dataset.loadingHint;
        }
        if (resolveVariant(form) === 'login') {
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
            variant: resolveVariant(form),
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
