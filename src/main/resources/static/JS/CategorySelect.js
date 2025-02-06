document.addEventListener("DOMContentLoaded", function () {
    let categoryLinks = document.querySelectorAll(".category-link"); // All category links
    let categoryButton = document.getElementById("categoriesBtnyoon"); // Category button
    let selectedCategoryText = document.getElementById("selectedCategoryText"); // Button text
    let selectedCategoryID = document.getElementById("selectedCategoryID"); // Hidden input

    categoryLinks.forEach(link => {
        link.addEventListener("click", function (event) {
            event.preventDefault(); // Prevent page reload

            let categoryName = this.textContent; // Get selected category name
            let categoryID = this.getAttribute("data-id"); // Get category ID

            selectedCategoryText.textContent = categoryName; // Update button text
            selectedCategoryID.value = categoryID; // Set hidden input value
        });
    });
});
