/**
 * Opens FleetDesk modals safely (avoids Bootstrap errors when markup is missing
 * or when offcanvas + modal toggles conflict on the same button).
 */
(function (global) {
    'use strict';

    function openModal(selector) {
        if (!selector || typeof global.bootstrap === 'undefined') {
            return false;
        }
        var el = document.querySelector(selector);
        if (!el || !el.classList.contains('modal')) {
            return false;
        }
        global.bootstrap.Modal.getOrCreateInstance(el).show();
        return true;
    }

    function handleOpenClick(event) {
        var btn = event.target.closest('[data-app-open-modal]');
        if (!btn) {
            return;
        }
        var selector = btn.getAttribute('data-app-open-modal');
        if (!selector || !document.querySelector(selector)) {
            return;
        }
        event.preventDefault();

        var offcanvasEl = btn.closest('.offcanvas');
        if (offcanvasEl && global.bootstrap && global.bootstrap.Offcanvas) {
            var instance = global.bootstrap.Offcanvas.getInstance(offcanvasEl);
            if (instance) {
                offcanvasEl.addEventListener('hidden.bs.offcanvas', function onHidden() {
                    offcanvasEl.removeEventListener('hidden.bs.offcanvas', onHidden);
                    openModal(selector);
                }, { once: true });
                instance.hide();
                return;
            }
        }

        openModal(selector);
    }

    document.addEventListener('click', handleOpenClick, true);

    global.FleetDeskModals = { open: openModal };
})(typeof window !== 'undefined' ? window : this);
