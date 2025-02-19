document.addEventListener("DOMContentLoaded", () => {
    document.querySelectorAll("tr").forEach((row) => {
        const decrementBtn = row.querySelector(".decrement");
        const incrementBtn = row.querySelector(".increment");
        const stockInput = row.querySelector(".stock-input");
        const saveButton = row.querySelector(".save-button");

        if (!stockInput || !saveButton || !decrementBtn || !incrementBtn) {
            console.warn("Missing elements in row:", row);
            return;
        }

        let originalStock = parseInt(stockInput.value, 10) || 0; // ✅ Store initial stock

        function toggleSaveButton() {
            let currentStock = parseInt(stockInput.value, 10) || 0;
            if (currentStock !== originalStock) {
                saveButton.classList.add("active");
                saveButton.removeAttribute("disabled");
            } else {
                saveButton.classList.remove("active");
                saveButton.setAttribute("disabled", "true");
            }
        }

        decrementBtn.addEventListener("click", (event) => {
            event.preventDefault();
            let currentStock = parseInt(stockInput.value, 10) || 0;
            if (currentStock > 0) {
                stockInput.value = currentStock - 1;
                toggleSaveButton();
            }
        });

        incrementBtn.addEventListener("click", (event) => {
            event.preventDefault();
            let currentStock = parseInt(stockInput.value, 10) || 0;
            stockInput.value = currentStock + 1;
            toggleSaveButton();
        });

        stockInput.addEventListener("input", () => {
            toggleSaveButton();
        });

        // ✅ Send stock update when Save is clicked
        saveButton.addEventListener("click", async () => {
            let itemID = row.querySelector(".delete-btn").getAttribute("data-item-id"); // Get item ID
            let newStock = parseInt(stockInput.value, 10);

            try {
                const response = await fetch(`/update-stock/${itemID}`, {
                    method: "POST",
                    headers: { "Content-Type": "application/x-www-form-urlencoded" },
                    body: new URLSearchParams({ stock: newStock }),
                });

                if (response.ok) {
                    alert("✅ Stock updated successfully!");
                    saveButton.classList.remove("active");
                    saveButton.setAttribute("disabled", "true");
                } else {
                    alert("❌ Failed to update stock.");
                }
            } catch (error) {
                console.error("❌ Error updating stock:", error);
            }
        });
    });
});
