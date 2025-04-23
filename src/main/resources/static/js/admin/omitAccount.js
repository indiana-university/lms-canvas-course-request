$('#omitAccountTable').DataTable({
   orderCellsTop: true,
   paging: false,
   order: [[0, 'asc']],
   language: {
       // Setting the text for the search label, mostly to remove the colon that is there by default
       search: 'Search',
       select: {
          aria: {
          }
       }
   },
    columnDefs: [
               { targets: ['.colDelete'], orderable: false }
           ]
   });

   jQuery(document).ready(function($) {
       let saveButton = $("#siterequest-omitaccount-edit-save");

       if (saveButton.length === 1) {
           $("#siterequest-omitaccount-edit-save").click(function(event) {
               let accountIdToOmitInput = $('#siterequest-omitaccount-account');

               if (accountIdToOmitInput.length === 1 && accountIdToOmitInput.first().val().trim().length === 0) {
                   $("#ui-omitaccount-id-error").removeClass("rvt-display-none");
                   event.preventDefault();
               }
           });
       }
   });