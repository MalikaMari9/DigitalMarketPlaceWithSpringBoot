document.addEventListener("DOMContentLoaded", () => {
    document.querySelectorAll(".wishlist-btn").forEach((button) => {
        button.addEventListener("click", async () => {
            const itemID = button.getAttribute("data-item-id");
            const isWishlisted = button.getAttribute("data-wishlisted") === "true";

            // Check if user is logged in
            if (!itemID) {
                alert("⚠️ You need to log in to use the wishlist.");
                window.location.href = "/loginPage";
                return;
            }

            try {
                const response = await fetch(`/wishlist/toggle/${itemID}`, {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                });

                const data = await response.json();

                if (data.success) {
                    // ✅ Toggle UI based on new wishlist status
                    button.setAttribute("data-wishlisted", data.wishlisted);

                    // ✅ Update heart icon
                    const img = button.querySelector(".wishlist-icon");
                    img.src = data.wishlisted 
                        ? "/Image/icon/heart-filled.png" 
                        : "/Image/icon/heart.png";

                    // ✅ Update wishlist count
                    button.querySelector(".wishlist-count").textContent = data.wishlistCount;
                } else {
                    alert("❌ Failed to update wishlist.");
                }
            } catch (error) {
                console.error("❌ Error updating wishlist:", error);
                alert("❌ Something went wrong. Please try again.");
            }
        });
    });
});
