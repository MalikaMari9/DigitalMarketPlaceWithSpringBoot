document.addEventListener("DOMContentLoaded", () => {
    const decreaseButton = document.querySelector(".decrease");
    const increaseButton = document.querySelector(".increase");
    const quantityInput = document.querySelector(".quantity-input");

    // Event listener for the decrease button
    decreaseButton.addEventListener("click", () => {
        const currentValue = parseInt(quantityInput.value, 10);
        if (currentValue > 0) {
            quantityInput.value = currentValue - 1;
        }
    });

    // Event listener for the increase button
    increaseButton.addEventListener("click", () => {
        const currentValue = parseInt(quantityInput.value, 10);
        quantityInput.value = currentValue + 1;
    });
});
