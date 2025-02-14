// Remove from Cart Function
		document.addEventListener("DOMContentLoaded", () => {
		    document.querySelectorAll(".trash-button").forEach((button) => {
		        button.addEventListener("click", async () => {
		            const cartID = button.getAttribute("data-cart-id");
		            if (!cartID) return;

		            const confirmDelete = confirm("Are you sure you want to remove this item from your cart?");
		            if (!confirmDelete) return;

		            try {
		                const response = await fetch(`/cart/remove/${cartID}`, {
		                    method: "DELETE",
		                    headers: { "Content-Type": "application/json" }
		                });

		                const data = await response.json();
		                if (data.success) {
		                    alert("✅ Item removed from your cart.");
		                    location.reload(); // Refresh the cart page
		                } else {
		                    alert("❌ Failed to remove item from cart: " + data.message);
		                }
		            } catch (error) {
		                console.error("❌ Error removing item from cart:", error);
		                alert("❌ Error removing item. Please try again.");
		            }
		        });
		    });
		});

