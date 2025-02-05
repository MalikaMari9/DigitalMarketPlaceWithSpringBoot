// JS/ImageSwap.js

// Wait for the DOM to load
document.addEventListener("DOMContentLoaded", function () {
    // Get all thumbnail images
    const thumbnails = document.querySelectorAll(".thumbnail");
    // Get the main image element
    const mainImage = document.getElementById("mainImage");

    // Add a click event listener to each thumbnail
    thumbnails.forEach(thumbnail => {
        thumbnail.addEventListener("click", function () {
            // Get the full-size image source from the data-fullsize attribute
            const fullSizeImage = thumbnail.getAttribute("data-fullsize");
            // Set the main image's source to the clicked thumbnail's full-size image
            mainImage.src = fullSizeImage;
        });
    });
});
