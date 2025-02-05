

function clearForm(button) {
       // Locate the closest parent form
       const form = button.closest('form');
       if (form) {
           // Use the reset() method to clear all fields
           form.reset();

           // Additionally clear any placeholder-like fields
           form.querySelectorAll('input, select').forEach((field) => {
               if (field.tagName === 'SELECT') {
                   field.selectedIndex = 0; // Reset dropdowns
               } else {
                   field.value = ''; // Clear text fields
               }
           });
       }
   }

   document.getElementById("productPhotos").addEventListener("change", function () {
       const previewContainer = document.getElementById("previewContainer");
       previewContainer.innerHTML = ""; // Clear previous previews
       const files = this.files;

       for (let file of files) {
           const reader = new FileReader();
           reader.onload = function (e) {
               const img = document.createElement("img");
               img.src = e.target.result;
               previewContainer.appendChild(img);
           };
           reader.readAsDataURL(file);
       }
   });

   document.getElementById("closeButton").addEventListener("click", function () {
       document.getElementById("itemUploadForm").reset();
       document.getElementById("previewContainer").innerHTML = "";
   });

