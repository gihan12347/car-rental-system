(function () {
    'use strict';

    function prepareNewEmployee() {
        var form = document.getElementById('employeeFormEl');
        if (!form) {
            return;
        }
        form.action = form.getAttribute('data-create-action') || '/employees';
        var title = document.getElementById('employeeModalLabel');
        if (title) {
            title.textContent = 'Add employee';
        }
    }

    function prepareNewPayment() {
        var form = document.getElementById('paymentFormEl');
        if (!form) {
            return;
        }
        form.action = form.getAttribute('data-create-action') || '/employees/payments';
        var title = document.getElementById('paymentModalLabel');
        if (title) {
            title.textContent = 'Record payment';
        }
    }

    document.addEventListener('DOMContentLoaded', function () {
        var empForm = document.getElementById('employeeFormEl');
        if (empForm) {
            empForm.setAttribute('data-create-action', empForm.getAttribute('action'));
        }
        var payForm = document.getElementById('paymentFormEl');
        if (payForm) {
            payForm.setAttribute('data-create-action', payForm.getAttribute('action'));
        }
    });

    function prepareNewPaymentForEmployee() {
        var form = document.getElementById('paymentFormEl');
        if (!form) {
            return;
        }
        var createAction = form.getAttribute('data-create-action');
        if (createAction) {
            form.action = createAction;
        }
        var title = document.getElementById('paymentModalLabel');
        if (title) {
            title.textContent = 'Add payment';
        }
        var idInput = form.querySelector('[name="id"]');
        if (idInput) {
            idInput.value = '';
        }
    }

    window.prepareNewEmployee = prepareNewEmployee;
    window.prepareNewPayment = prepareNewPayment;
    window.prepareNewPaymentForEmployee = prepareNewPaymentForEmployee;
})();
