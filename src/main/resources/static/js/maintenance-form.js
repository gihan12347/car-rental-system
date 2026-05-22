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
        var submitBtn = document.getElementById('maintenanceSubmitBtn');

        if (!dateInput || !descInput || !costInput || !submitBtn) {
            return;
        }

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
        updateSubmit();
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initMaintenanceForm);
    } else {
        initMaintenanceForm();
    }
})();
