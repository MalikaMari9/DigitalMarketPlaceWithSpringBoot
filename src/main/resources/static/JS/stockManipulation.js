document.addEventListener("DOMContentLoaded", () => {
    const rows = document.querySelectorAll("#sales-table tr");

    rows.forEach((row) => {
        const decrementButton = row.querySelector(".decrement");
        const incrementButton = row.querySelector(".increment");
        const stockInput = row.querySelector(".stock-input");
        const saveButton = row.querySelector(".save-button");

        if (decrementButton && incrementButton && stockInput && saveButton) {
            const initialValue = parseInt(stockInput.value);

            const updateSaveButtonState = () => {
                const currentValue = parseInt(stockInput.value);
                if (currentValue !== initialValue) {
                    saveButton.classList.remove("disabled");
                    saveButton.disabled = false;
                } else {
                    saveButton.classList.add("disabled");
                    saveButton.disabled = true;
                }
            };

            decrementButton.addEventListener("click", () => {
                const currentValue = parseInt(stockInput.value);
                if (currentValue > 0) {
                    stockInput.value = currentValue - 1;
                    updateSaveButtonState();
                }
            });

            incrementButton.addEventListener("click", () => {
                const currentValue = parseInt(stockInput.value);
                stockInput.value = currentValue + 1;
                updateSaveButtonState();
            });

            saveButton.addEventListener("click", () => {
                alert(`Stock updated to ${stockInput.value}`);
                saveButton.classList.add("disabled");
                saveButton.disabled = true;
            });
        }
    });
});
