$('#themeTable').DataTable({
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
   }
   });
