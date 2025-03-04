document.addEventListener("DOMContentLoaded", () => {
    document.querySelectorAll(".buy-again").forEach((btn) => {
        btn.addEventListener("click", async (event) => {
            event.preventDefault();
            console.log("✅ Buy Again button clicked!");

            const itemID = btn.getAttribute("data-item-id");

            if (!itemID) {
                alert("❌ Invalid item details.");
                return;
            }

            try {
                const response = await fetch(`/cart/buy-again/${itemID}`, {
                    method: "POST",
                    headers: { "Content-Type": "application/json" }
                });

                const data = await response.json();
                console.log("🔍 Response:", data);

                if (response.ok && data.success) {
                    alert("✅ Item added to cart!");
					location.reload();
                } else {
                    alert("❌ Failed to add item to cart: " + (data.message || "Unknown error"));
                }
            } catch (error) {
                console.error("❌ Error buying again:", error);
                alert("❌ An error occurred.");
            }
        });
    });
});
