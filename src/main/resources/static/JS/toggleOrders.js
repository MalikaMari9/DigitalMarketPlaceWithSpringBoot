document.addEventListener("DOMContentLoaded", function () {
    const orderHeaders = document.querySelectorAll(".order-header");

    orderHeaders.forEach(header => {
        header.addEventListener("click", function () {
            const orderContent = header.nextElementSibling.nextElementSibling; // Targets `.order-content`
            const icon = header.querySelector(".toggle-icon");

            if (orderContent.style.display === "none" || orderContent.style.display === "") {
                orderContent.style.display = "block";
                icon.classList.remove("fa-chevron-down");
                icon.classList.add("fa-chevron-up"); // Change icon
            } else {
                orderContent.style.display = "none";
                icon.classList.remove("fa-chevron-up");
                icon.classList.add("fa-chevron-down");
            }
        });
    });
});
