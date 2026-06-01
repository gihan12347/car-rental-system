/**
 * Shared Bootstrap confirmation modal for deletes and other destructive POST actions.
 *
 * Trigger attributes (on button or [data-app-confirm]):
 *   data-app-confirm-title, data-app-confirm-message, data-app-confirm-action
 *   data-app-confirm-submit, data-app-confirm-cancel, data-app-confirm-loading
 *   data-app-confirm-variant: danger | warning
 *   data-app-confirm-summary: vehicle | list | none
 *   data-app-confirm-subject — bold name in vehicle delete copy
 *   data-app-confirm-count-rentals, data-app-confirm-count-maintenance
 *   data-app-confirm-summary-items — "iconClass|label;iconClass|label" for custom bullets
 *
 * Optional hidden POST fields: sibling <template class="app-confirm-extra-fields"> inside
 * .app-confirm-delete-trigger wrapper.
 *
 * Legacy vehicle attrs (data-delete-car-*, #deleteCarModal) are still supported.
 */
(function () {
    'use strict';

    var MODAL_ID = 'appConfirmDeleteModal';
    var LEGACY_MODAL_ID = 'deleteCarModal';

    function readTrigger(event) {
        var related = event.relatedTarget;
        if (related && (related.hasAttribute('data-app-confirm') || related.hasAttribute('data-delete-car-id'))) {
            return related;
        }
        var active = document.activeElement;
        if (active && (active.hasAttribute('data-app-confirm') || active.hasAttribute('data-delete-car-id'))) {
            return active;
        }
        return null;
    }

    function attr(trigger, primary, legacy) {
        if (!trigger) {
            return '';
        }
        var value = trigger.getAttribute(primary);
        if (value !== null && value !== '') {
            return value;
        }
        return legacy ? (trigger.getAttribute(legacy) || '') : '';
    }

    function setText(id, value) {
        var el = document.getElementById(id);
        if (el) {
            el.textContent = value || '';
        }
    }

    function showEl(el, visible) {
        if (!el) {
            return;
        }
        el.classList.toggle('d-none', !visible);
    }

    function applyVariant(variant) {
        var content = document.getElementById('appConfirmDeleteModalContent');
        var header = document.getElementById('appConfirmDeleteModalHeader');
        var title = document.getElementById('appConfirmDeleteModalLabel');
        var submitBtn = document.getElementById('appConfirmDeleteSubmitBtn');
        var icon = document.getElementById('appConfirmDeleteModalIcon');
        var submitIcon = document.getElementById('appConfirmDeleteSubmitIcon');
        var isWarning = variant === 'warning';

        if (content) {
            content.classList.toggle('border-danger', !isWarning);
            content.classList.toggle('border-opacity-25', !isWarning);
            content.classList.toggle('border-warning', isWarning);
        }
        if (header) {
            header.classList.toggle('border-danger', !isWarning);
            header.classList.toggle('border-opacity-25', !isWarning);
            header.classList.toggle('border-warning', isWarning);
        }
        if (title) {
            title.classList.toggle('text-danger', !isWarning);
            title.classList.toggle('text-warning', isWarning);
        }
        if (submitBtn) {
            submitBtn.classList.toggle('btn-danger', !isWarning);
            submitBtn.classList.toggle('btn-warning', isWarning);
        }
        if (icon) {
            icon.className = 'bi me-2 ' + (isWarning ? 'bi-x-circle' : 'bi-exclamation-triangle-fill');
        }
        if (submitIcon) {
            submitIcon.className = 'bi ' + (isWarning ? 'bi-x-circle' : 'bi-trash');
        }
    }

    function renderSummaryList(itemsSpec) {
        var list = document.getElementById('appConfirmDeleteModalSummaryList');
        if (!list) {
            return;
        }
        list.innerHTML = '';
        if (!itemsSpec || !itemsSpec.trim()) {
            showEl(list, false);
            return;
        }
        itemsSpec.split(';').forEach(function (part) {
            var piece = part.trim();
            if (!piece) {
                return;
            }
            var sep = piece.indexOf('|');
            var icon = sep >= 0 ? piece.slice(0, sep).trim() : 'bi-dot';
            var label = sep >= 0 ? piece.slice(sep + 1).trim() : piece;
            var li = document.createElement('li');
            li.className = 'd-flex align-items-center gap-2 py-2 border-bottom border-opacity-10';
            li.innerHTML = '<i class="bi ' + icon + ' text-danger" aria-hidden="true"></i><span>' + label + '</span>';
            list.appendChild(li);
        });
        var last = list.lastElementChild;
        if (last) {
            last.classList.remove('border-bottom', 'border-opacity-10');
        }
        showEl(list, list.children.length > 0);
    }

    function applyExtraFields(trigger) {
        var container = document.getElementById('appConfirmDeleteExtraFields');
        if (!container) {
            return;
        }
        container.innerHTML = '';
        var wrap = trigger.closest('.app-confirm-delete-trigger');
        if (!wrap) {
            return;
        }
        var tmpl = wrap.querySelector('template.app-confirm-extra-fields');
        if (!tmpl || !tmpl.content) {
            return;
        }
        container.appendChild(tmpl.content.cloneNode(true));
    }

    function bindModal(modalEl) {
        modalEl.addEventListener('show.bs.modal', function (event) {
            var trigger = readTrigger(event);
            if (!trigger) {
                return;
            }

            var isLegacyCar = trigger.hasAttribute('data-delete-car-id');
            var title = attr(trigger, 'data-app-confirm-title', null) || (isLegacyCar ? 'Delete vehicle' : 'Confirm');
            var message = attr(trigger, 'data-app-confirm-message', null);
            var action = attr(trigger, 'data-app-confirm-action', 'data-delete-car-action');
            var submitLabel = attr(trigger, 'data-app-confirm-submit', null) || 'Delete permanently';
            var cancelLabel = attr(trigger, 'data-app-confirm-cancel', null) || 'Cancel';
            var loading = attr(trigger, 'data-app-confirm-loading', null) || 'Processing…';
            var variant = attr(trigger, 'data-app-confirm-variant', null) || 'danger';
            var summaryType = attr(trigger, 'data-app-confirm-summary', null) || (isLegacyCar ? 'vehicle' : 'none');

            if (!message && isLegacyCar) {
                var registration = attr(trigger, 'data-app-confirm-subject', 'data-delete-car-registration') || 'this vehicle';
                message = 'Permanently remove ' + registration + ' and all linked records from the system. This cannot be undone.';
            }

            setText('appConfirmDeleteModalTitleText', title);
            setText('appConfirmDeleteModalMessage', message);
            setText('appConfirmDeleteCancelBtn', cancelLabel);
            setText('appConfirmDeleteSubmitText', submitLabel);

            applyVariant(variant);

            var vehicleSummary = document.getElementById('appConfirmDeleteVehicleSummary');
            var listSummary = document.getElementById('appConfirmDeleteModalSummaryList');
            showEl(vehicleSummary, false);
            showEl(listSummary, false);

            if (summaryType === 'vehicle') {
                var subject = attr(trigger, 'data-app-confirm-subject', 'data-delete-car-registration') || 'this vehicle';
                if (!attr(trigger, 'data-app-confirm-message', null) && !isLegacyCar) {
                    setText('appConfirmDeleteModalMessage',
                        'Permanently remove ' + subject + ' and all linked records from the system. This cannot be undone.');
                }
                setText('appConfirmDeleteRentalCount',
                    attr(trigger, 'data-app-confirm-count-rentals', 'data-delete-car-rentals') || '0');
                setText('appConfirmDeleteMaintenanceCount',
                    attr(trigger, 'data-app-confirm-count-maintenance', 'data-delete-car-maintenance') || '0');
                showEl(vehicleSummary, true);
            } else if (summaryType === 'list') {
                renderSummaryList(attr(trigger, 'data-app-confirm-summary-items', null));
            }

            var form = document.getElementById('appConfirmDeleteForm');
            if (form && action) {
                form.setAttribute('action', action);
                form.setAttribute('data-loading-message', loading);
            }

            applyExtraFields(trigger);

            var legacyQ = attr(trigger, 'data-app-confirm-param-q', 'data-delete-car-search-q');
            if (legacyQ && form) {
                var container = document.getElementById('appConfirmDeleteExtraFields');
                if (container && !container.querySelector('input[name="q"]')) {
                    var input = document.createElement('input');
                    input.type = 'hidden';
                    input.name = 'q';
                    input.value = legacyQ;
                    container.appendChild(input);
                }
            }
        });
    }

    function init() {
        var modalEl = document.getElementById(MODAL_ID) || document.getElementById(LEGACY_MODAL_ID);
        if (!modalEl) {
            return;
        }
        bindModal(modalEl);
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();
