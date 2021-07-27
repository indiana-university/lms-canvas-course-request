$(document).ready(function() {
    var errorFocusElements = $(".dummy-class-for-accessibility-focus");

    if(errorFocusElements.length > 0) {
        //always focus the first error element
        errorFocusElements.first().parents(".controls").children().first().focus();

        $.each(errorFocusElements, function(index, value) {

            var inputOrSelectElement = $(this).parents(".controls").children().first();
            inputOrSelectElement.attr("aria-invalid", "true");

            var errorId = inputOrSelectElement.siblings("div.ic-Form-message--error").first().attr("id");
            inputOrSelectElement.attr("aria-describedby", errorId);
        });
    }

    // this will prevent form from submitting twice
    $('#new_course_form').submit(function() {
        $('#courseSubmit').prop('disabled', true);
    });

});