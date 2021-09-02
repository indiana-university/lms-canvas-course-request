$(document).ready(function() {
    var errorFocusElements = $(".rvt-validation-danger");

    if(errorFocusElements.length > 0) {
        //always focus the first error element
        errorFocusElements.first().focus();
    }

    // this will prevent form from submitting twice
    $('#new_course_form').submit(function() {
        $('#courseSubmit').prop('disabled', true);
    });

});