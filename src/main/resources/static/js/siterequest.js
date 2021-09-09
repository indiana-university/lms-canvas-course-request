$(document).ready(function() {
    // this will prevent form from submitting twice
    $('#new_course_form').submit(function() {
        $('#courseSubmit').prop('disabled', true);
    });

});