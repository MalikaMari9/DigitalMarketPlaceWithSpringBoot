document.addEventListener("DOMContentLoaded", () => {
    document.querySelectorAll(".pg-wishlist-btn").forEach(button => {
        button.addEventListener("click", async function() {
            const itemId = this.getAttribute("data-item-id");

            if (!itemId) {
                alert("⚠️ Error: Item ID is missing.");
                return;
            }

            try {
                const response = await fetch(`/wishlist/toggle/${itemId}`, {
                    method: "POST",
                });

                const data = await response.json();
                if (data.success) {
                    // ✅ Toggle button appearance
                    this.classList.toggle("wishlisted");
                    this.textContent = data.wishlisted ? "♥" : "♡"; // Toggle text
                } else {
                    alert("❌ " + data.message);
                }
            } catch (error) {
                console.error("❌ Wishlist toggle failed:", error);
                alert("❌ Error toggling wishlist.");
            }
        });
    });
});
