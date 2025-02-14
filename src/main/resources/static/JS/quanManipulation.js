document.addEventListener("DOMContentLoaded", () => {
    document.querySelectorAll(".quantity-control").forEach((control) => {
        const decreaseButton = control.querySelector(".decrease");
        const increaseButton = control.querySelector(".increase");
        const quantityInput = control.querySelector(".quantity-input");

        if (!quantityInput) return;

        let maxStock = parseInt(quantityInput.getAttribute("data-max-stock"), 10) || 1; // Ensure a valid stock limit

        // ✅ Prevent duplicate event listeners
        decreaseButton.addEventListener("click", (event) => {
            event.preventDefault(); // ✅ Prevent unintended form submissions

            let currentValue = parseInt(quantityInput.value, 10) || 1;
            if (currentValue > 1) {
                quantityInput.value = currentValue - 1;
            }
        });

        increaseButton.addEventListener("click", (event) => {
            event.preventDefault(); // ✅ Prevent unintended form submissions

            let currentValue = parseInt(quantityInput.value, 10) || 1;
            if (currentValue < maxStock) {
                quantityInput.value = currentValue + 1;
            } else {
                alert("⚠️ Maximum stock limit reached!");
            }
        });
    });
});
