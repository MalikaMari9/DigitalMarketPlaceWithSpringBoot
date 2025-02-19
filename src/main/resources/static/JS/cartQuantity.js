document.addEventListener("DOMContentLoaded", () => {
    document.querySelectorAll(".quantity-btn").forEach((button) => {
        button.addEventListener("click", async (event) => {
            const cartID = button.getAttribute("data-cart-id");
            const quantityInput = document.querySelector(`.quantity-input[data-cart-id='${cartID}']`);
            let newQuantity = parseInt(quantityInput.value, 10);

            if (button.classList.contains("increase")) {
                newQuantity++;
            } else if (button.classList.contains("decrease") && newQuantity > 1) {
                newQuantity--;
            } else {
                return;
            }

            quantityInput.value = newQuantity;

            try {
                const response = await fetch(`/cart/update/${cartID}`, {
                    method: "PUT",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({ quantity: newQuantity })
                });

                const data = await response.json();
                if (data.success) {
                    console.log("✅ Cart updated:", data);
                    location.reload(); // Refresh to update total price
                } else {
                    alert("❌ Failed to update cart: " + data.message);
                }
            } catch (error) {
                console.error("❌ Error updating cart:", error);
                alert("❌ Error updating cart.");
            }
        });
    });
});
