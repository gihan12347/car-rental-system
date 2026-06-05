/**
 * Auth navigation guard for FleetDesk.
 * Uses /api/auth/status so Back to /login cannot bypass a live server session.
 */
(function () {
    'use strict';

    var ACTIVE_SESSION = 'fleetDeskActiveSession';
    var STATUS_URL = '/api/auth/status';

    function dashboardUrl() {
        var meta = document.querySelector('meta[name="fleetdesk-dashboard-url"]');
        return (meta && meta.content) ? meta.content : '/dashboard';
    }

    function loginUrl() {
        var meta = document.querySelector('meta[name="fleetdesk-login-url"]');
        return (meta && meta.content) ? meta.content : '/login';
    }

    function statusUrl() {
        var meta = document.querySelector('meta[name="fleetdesk-auth-status-url"]');
        return (meta && meta.content) ? meta.content : STATUS_URL;
    }

    function isAppPage() {
        return document.body && document.body.classList.contains('app-body');
    }

    function isLoginPage() {
        return document.body && document.body.classList.contains('login-page');
    }

    function isLoginPath() {
        var path = window.location.pathname || '';
        return path.endsWith('/login');
    }

    function currentUrl() {
        return window.location.pathname + window.location.search + window.location.hash;
    }

    function shouldTrapLoginHistory() {
        var params = new URLSearchParams(window.location.search);
        return params.has('signedOut') || params.has('expired');
    }

    function fetchAuthStatus() {
        return fetch(statusUrl(), {
            method: 'GET',
            credentials: 'same-origin',
            cache: 'no-store',
            headers: { Accept: 'application/json' }
        }).then(function (response) {
            if (!response.ok) {
                return { authenticated: false };
            }
            return response.json();
        }).catch(function () {
            return { authenticated: false };
        });
    }

    /** If server session is active, leave login immediately (works with bfcache). */
    function redirectIfAuthenticatedOnLoginPage() {
        if (!isLoginPage()) {
            return Promise.resolve(false);
        }
        return fetchAuthStatus().then(function (data) {
            if (data && data.authenticated) {
                sessionStorage.setItem(ACTIVE_SESSION, '1');
                var target = (data.landingUrl && data.landingUrl.length > 0)
                    ? data.landingUrl
                    : dashboardUrl();
                window.location.replace(target);
                return true;
            }
            sessionStorage.removeItem(ACTIVE_SESSION);
            return false;
        });
    }

    function trapLoginHistoryAfterSignOut() {
        var loginUrl = currentUrl();
        history.replaceState({ fleetDeskAuth: 'login' }, document.title, loginUrl);
        history.pushState({ fleetDeskAuth: 'login' }, document.title, loginUrl);
        window.addEventListener('popstate', function () {
            if (!isLoginPath()) {
                window.location.replace(loginUrl);
                return;
            }
            history.pushState({ fleetDeskAuth: 'login' }, document.title, loginUrl);
        });
    }

    function wireLoginForm() {
        if (!isLoginPage()) {
            return;
        }
        var form = document.getElementById('loginForm');
        if (!form) {
            return;
        }
        form.addEventListener('submit', function () {
            sessionStorage.setItem(ACTIVE_SESSION, '1');
            history.replaceState(null, document.title, loginUrl());
        });
    }

    function wireLogoutForms() {
        document.querySelectorAll('form[action*="logout"]').forEach(function (form) {
            form.addEventListener('submit', function () {
                sessionStorage.removeItem(ACTIVE_SESSION);
            });
        });
    }

    window.addEventListener('pageshow', function (event) {
        if (isLoginPage()) {
            redirectIfAuthenticatedOnLoginPage().then(function (redirected) {
                if (!redirected && event.persisted) {
                    window.location.reload();
                }
            });
            return;
        }
        if (event.persisted && isAppPage()) {
            window.location.reload();
        }
    });

    function init() {
        wireLoginForm();
        wireLogoutForms();

        if (isLoginPage()) {
            redirectIfAuthenticatedOnLoginPage().then(function (redirected) {
                if (redirected) {
                    return;
                }
                if (shouldTrapLoginHistory()) {
                    trapLoginHistoryAfterSignOut();
                }
            });
            return;
        }

        if (isAppPage()) {
            sessionStorage.setItem(ACTIVE_SESSION, '1');
        }
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();
