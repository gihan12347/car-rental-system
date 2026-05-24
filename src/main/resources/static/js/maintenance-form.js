(function () {
    'use strict';

    function initMaintenanceForm() {
        var form = document.querySelector('#addMaintenanceModal form');
        if (!form) {
            return;
        }

        var dateInput = document.getElementById('m-date');
        var descInput = document.getElementById('m-desc');
        var costInput = document.getElementById('m-cost');
        var typeInputs = form.querySelectorAll('input[name="maintenanceType"]');
        var serviceFields = document.getElementById('m-service-fields');
        var nextServiceInput = document.getElementById('m-next-service');
        var submitBtn = document.getElementById('maintenanceSubmitBtn');

        if (!dateInput || !descInput || !costInput || !submitBtn) {
            return;
        }

        var defaultInterval = serviceFields
            ? parseInt(serviceFields.getAttribute('data-default-interval'), 10) || 5000
            : 5000;
        var currentOdometer = serviceFields
            ? parseInt(serviceFields.getAttribute('data-current-odometer'), 10) || 0
            : 0;

        function isServiceType() {
            var checked = form.querySelector('input[name="maintenanceType"]:checked');
            return checked && checked.value === 'SERVICE';
        }

        function suggestNextServiceKm() {
            if (!nextServiceInput || !isServiceType()) {
                return;
            }
            if (nextServiceInput.dataset.userEdited === 'true') {
                return;
            }
            if (currentOdometer > 0) {
                nextServiceInput.value = String(currentOdometer + defaultInterval);
            }
        }

        function toggleServiceFields() {
            var show = isServiceType();
            if (serviceFields) {
                serviceFields.classList.toggle('d-none', !show);
            }
            if (!show && nextServiceInput) {
                nextServiceInput.value = '';
                nextServiceInput.dataset.userEdited = '';
            } else {
                suggestNextServiceKm();
            }
        }

        if (nextServiceInput) {
            nextServiceInput.addEventListener('input', function () {
                nextServiceInput.dataset.userEdited = 'true';
            });
        }

        typeInputs.forEach(function (input) {
            input.addEventListener('change', toggleServiceFields);
        });

        function isComplete() {
            var cost = costInput.value.trim();
            return dateInput.value !== ''
                && descInput.value.trim() !== ''
                && cost !== ''
                && !isNaN(parseFloat(cost))
                && parseFloat(cost) >= 0;
        }

        function updateSubmit() {
            submitBtn.disabled = !isComplete();
        }

        dateInput.addEventListener('input', updateSubmit);
        dateInput.addEventListener('change', updateSubmit);
        descInput.addEventListener('input', updateSubmit);
        costInput.addEventListener('input', updateSubmit);
        costInput.addEventListener('change', updateSubmit);

        toggleServiceFields();
        updateSubmit();
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initMaintenanceForm);
    } else {
        initMaintenanceForm();
    }
})();
