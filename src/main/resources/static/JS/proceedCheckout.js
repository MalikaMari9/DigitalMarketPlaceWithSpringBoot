document.addEventListener("DOMContentLoaded", () => {
    lucide.createIcons();

    // ✅ Delivery Fee & Total Price Update
    const deliveryOption = document.getElementById("deliveryOption");
    const deliveryFeeText = document.getElementById("deliveryFeeText");
    const totalPriceText = document.getElementById("totalPriceText");
    const subtotalText = document.getElementById("subtotalText");

    function updateDeliveryFee() {
        let subtotal = parseFloat(subtotalText.innerText.replace('$', '')) || 0;
        let selectedDeliveryFee = deliveryOption?.value === "express" ? 10.00 : 5.00;

        // ✅ Update delivery fee dynamically
        deliveryFeeText.innerText = `$${selectedDeliveryFee.toFixed(2)}`;

        // ✅ Update the total price dynamically
        let totalPrice = subtotal + selectedDeliveryFee;
        totalPriceText.innerText = `$${totalPrice.toFixed(2)}`;
    }

    // ✅ Trigger event when delivery option changes
    if (deliveryOption) {
        deliveryOption.addEventListener("change", updateDeliveryFee);
        updateDeliveryFee(); // Initialize on page load
    }

    // ✅ Order Placement Logic
    document.querySelectorAll(".btn-place-order").forEach((btn) => {
        btn.addEventListener("click", async (event) => {
            event.preventDefault();
            console.log("✅ Place Order button clicked!");

            const paymentMethod = document.querySelector('input[name="paymentMethod"]:checked')?.value;
            const deliveryOption = document.getElementById("deliveryOption")?.value;
            const addressID = document.getElementById("deliveryAddress")?.value; // ✅ FIXED ID

            const sellerID = btn.getAttribute("data-seller-id");

            if (!sellerID) {
                alert("❌ Missing seller ID!");
                return;
            }

            if (!paymentMethod || !deliveryOption || !addressID) {
                alert("❌ Please select an address, delivery option, and payment method!");
                return;
            }

            // ✅ Ensure we fetch the subtotal dynamically
            const subtotalText = document.querySelector(".total-price").innerText.replace('$', '').trim();
            let subtotal = parseFloat(subtotalText) || 0;

            let deliveryFee = deliveryOption === "express" ? 10.00 : 5.00;
            let totalAmount = (subtotal + deliveryFee).toFixed(2);

            console.log(`🛒 Subtotal: $${subtotal}, 🚚 Delivery Fee: $${deliveryFee}, 💰 Total: $${totalAmount}`);

            // ✅ Redirect for online payment
            if (paymentMethod === "online") {
                window.location.href = `/credit-card-payment?amount=${totalAmount}&sellerID=${sellerID}`;
                return;
            }

            // ✅ Otherwise, process the order (COD)
            try {
                const response = await fetch("/place-order", {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({
                        paymentMethod: paymentMethod,
                        deliveryOption: deliveryOption,
                        addressID: addressID,
                        deliveryFee: deliveryFee,
                        sellerID: parseInt(sellerID),
                        totalAmount: totalAmount
                    })
                });

                const data = await response.json();
                console.log("🔍 Response:", data);

                if (response.ok && data.success) {
                    alert("✅ Order placed successfully!");
                    window.location.href = "/orderHistory"; // Redirect to confirmation page
                } else {
                    alert("❌ Failed to place order: " + (data.message || "Unknown error"));
                }
            } catch (error) {
                console.error("❌ Fetch error:", error);
                alert("❌ An error occurred while placing the order.");
            }
        });
    });
});
