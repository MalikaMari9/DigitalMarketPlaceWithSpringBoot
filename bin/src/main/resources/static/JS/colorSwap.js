// Wait for the DOM to load
document.addEventListener("DOMContentLoaded", function () {
    // Get all color buttons
    const colorButtons = document.querySelectorAll(".clothing-buttons button");
    // Get the main image element
    const mainImage = document.getElementById("mainImage");

    // Add a click event listener to each button
    colorButtons.forEach(button => {
        button.addEventListener("click", function () {
            // Get the color from the button's data-color attribute
            const color = button.getAttribute("data-color");
            // Set the main image's source based on the color
            mainImage.src = `Image/clothes/cloth-${color}.png`;

            // Optional: Add an active state to highlight the selected button
            colorButtons.forEach(btn => btn.classList.remove("active"));
            button.classList.add("active");
        });
    });
});
