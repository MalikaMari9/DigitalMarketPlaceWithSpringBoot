document.addEventListener("DOMContentLoaded", () => {
    document.querySelectorAll(".addToCartBtn, .pg-cart-btn").forEach((button) => {
        button.addEventListener("click", async (event) => {
            event.preventDefault();

            if (button.dataset.processing === "true") return; // Prevent duplicate clicks

            button.dataset.processing = "true"; // ✅ Prevent multiple requests

            const itemId = button.getAttribute("data-item-id");
            const userId = button.getAttribute("data-user-id");

            // ✅ Try to find the quantity input field (for detailed pages)
            let quantityInput = button.closest("#normal-sale")?.querySelector(".quantity-input");
            let quantity = 1; // ✅ Default quantity is 1 if input is not found

            if (quantityInput) {
                let inputValue = parseInt(quantityInput.value, 10);
                if (!isNaN(inputValue) && inputValue > 0) {
                    quantity = inputValue; // ✅ Use input value if valid
                }
            }

            console.log("🛒 Sending request to add item to cart:", { itemId, userId, quantity });

            if (!userId) {
                alert("⚠️ Please log in to add items to your cart.");
                button.dataset.processing = "false";
                return;
            }

            try {
                const response = await fetch("/cart/add", {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({ itemId, userId, quantity })
                });

                const data = await response.json();
                if (data.success) {
                    console.log("✅ Item added successfully:", data);
                    const cartCountElement = document.getElementById("cartCountyoon");
                    if (cartCountElement) {
                        cartCountElement.textContent = data.cartCount;
                        cartCountElement.style.display = "inline";
                    }
                } else {
                    console.error("❌ Failed to add to cart:", data.message);
                    alert("❌ " + data.message);
                }
            } catch (error) {
                console.error("❌ Error in fetch request:", error);
                alert("❌ Error adding to cart: " + error.message);
            } finally {
                button.dataset.processing = "false"; // ✅ Re-enable button after request
            }
        });
    });

    // ✅ Quantity Controls (For Pages with Quantity Input)
    document.querySelectorAll(".quantity-control").forEach((control) => {
        const decreaseBtn = control.querySelector(".decrease");
        const increaseBtn = control.querySelector(".increase");
        const quantityInput = control.querySelector(".quantity-input");

        if (!quantityInput) return;

        let maxStock = parseInt(quantityInput.getAttribute("data-max-stock"), 10);

        // ✅ Prevent negative values when decreasing
        decreaseBtn.addEventListener("click", () => {
            let value = parseInt(quantityInput.value, 10) || 1;
            if (value > 1) {
                quantityInput.value = value - 1;
            } else {
                alert("⚠️ Quantity cannot be 0 or less.");
            }
        });

        // ✅ Prevent exceeding stock when increasing
        increaseBtn.addEventListener("click", () => {
            let value = parseInt(quantityInput.value, 10) || 1;
            if (value < maxStock) {
                quantityInput.value = value + 1;
            } else {
                alert(`⚠️ Maximum stock limit reached! Only ${maxStock} available.`);
            }
        });

        // ✅ Prevent users from manually entering 0 or negative numbers
        quantityInput.addEventListener("input", () => {
            let value = parseInt(quantityInput.value, 10);

            if (isNaN(value) || value < 1) {
                alert("⚠️ Quantity cannot be 0 or less. Please enter a valid number.");
                quantityInput.value = ""; // Allow correction instead of forcing 1
            } else if (value > maxStock) {
                alert(`⚠️ Maximum stock limit reached! Only ${maxStock} available.`);
                quantityInput.value = maxStock;
            }
        });
    });
});


