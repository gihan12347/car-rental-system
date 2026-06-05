(function () {
    'use strict';

    function wireForm(formId, fieldIds, submitId, isValid) {
        var form = document.getElementById(formId);
        if (!form) {
            return;
        }
        var submitBtn = document.getElementById(submitId);
        if (!submitBtn) {
            return;
        }
        var fields = fieldIds.map(function (id) { return document.getElementById(id); }).filter(Boolean);
        if (fields.length !== fieldIds.length) {
            return;
        }

        function updateSubmit() {
            submitBtn.disabled = !isValid(fields);
        }

        fields.forEach(function (el) {
            el.addEventListener('input', updateSubmit);
            el.addEventListener('change', updateSubmit);
        });
        updateSubmit();
    }

    function passwordPairValid(fields) {
        var current = fields[0].value;
        var next = fields[1].value;
        var confirm = fields[2].value;
        return current.length > 0
            && next.length >= 6
            && confirm.length >= 6
            && next === confirm;
    }

    function createUserValid(fields) {
        var username = fields[0].value.trim();
        var role = fields[1].value;
        var password = fields[2].value;
        var confirm = fields[3].value;
        return username.length >= 3
            && role.length > 0
            && password.length >= 6
            && confirm.length >= 6
            && password === confirm;
    }

    function init() {
        wireForm(
            'changePasswordForm',
            ['currentPassword', 'newPassword', 'confirmNewPassword'],
            'changePasswordSubmitBtn',
            passwordPairValid);
        wireForm(
            'createUserForm',
            ['newUsername', 'newUserRole', 'createUserPassword', 'createUserConfirmPassword'],
            'createUserSubmitBtn',
            createUserValid);
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();
