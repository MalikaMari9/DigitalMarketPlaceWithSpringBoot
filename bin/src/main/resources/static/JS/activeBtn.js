document.addEventListener("DOMContentLoaded", () => {
    const sizeButtons = document.querySelectorAll(".size-buttons button");

    sizeButtons.forEach(button => {
        button.addEventListener("click", () => {
            // Remove active class from all buttons
            sizeButtons.forEach(btn => btn.classList.remove("active"));
            // Add active class to the clicked button
            button.classList.add("active");
        });
    });
});
