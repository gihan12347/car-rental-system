(function () {
    'use strict';

    function prepareNewExpense() {
        var form = document.getElementById('expenseFormEl');
        if (!form) {
            return;
        }
        var createAction = form.getAttribute('data-create-action');
        if (createAction) {
            form.action = createAction;
        }
        var title = document.getElementById('expenseModalLabel');
        if (title) {
            title.textContent = 'Add office expense';
        }
    }

    document.addEventListener('DOMContentLoaded', function () {
        var form = document.getElementById('expenseFormEl');
        if (form) {
            form.setAttribute('data-create-action', form.getAttribute('action'));
        }
    });

    window.prepareNewExpense = prepareNewExpense;
})();
