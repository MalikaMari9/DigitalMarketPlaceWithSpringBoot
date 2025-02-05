document.addEventListener("DOMContentLoaded", () => {
    const categorySelect = document.getElementById("item-category");
    const additionalDetailsContainer = document.getElementById("additional-details-container");
    const modal = document.getElementById("clothing-modal");
    const closeModalButton = document.querySelector(".close-button");
    const openModalButton = document.getElementById("open-modal");
    const saveDetailsButton = document.getElementById("save-details");
    const colorFields = document.getElementById("color-fields");
    const addColorButton = document.getElementById("add-color");
    const sizeFields = document.getElementById("size-fields");
    const addSizeButton = document.getElementById("add-size");

    let colorCount = 1; // Track the number of color fields
    const maxColors = 5;

    // Show or hide additional clothing details based on category selection
    categorySelect.addEventListener("change", () => {
        const selectedOption = categorySelect.options[categorySelect.selectedIndex];
        const selectedCategory = selectedOption.dataset.category; // Get parent category (e.g., "Clothing")

        // Show details only if the parent category is "Clothing"
        if (selectedCategory === "Clothing" || selectedCategory === "Shoes") {
            additionalDetailsContainer.style.display = "block";
        } else {
            additionalDetailsContainer.style.display = "none";
        }
    });

    // Open the modal
    openModalButton.addEventListener("click", () => {
        modal.style.display = "block";
    });

    // Close the modal
    closeModalButton.addEventListener("click", () => {
        modal.style.display = "none";
    });

    // Close the modal when clicking outside of it
    window.addEventListener("click", (event) => {
        if (event.target === modal) {
            modal.style.display = "none";
        }
    });

    // Add more color fields
    addColorButton.addEventListener("click", () => {
        if (colorCount < maxColors) {
            const colorInput = document.createElement("div");
            colorInput.innerHTML = `
                <input type="text" class="form-input" name="colors[]" placeholder="Enter color" required>
                <input type="file" class="form-input" name="colorImages[]" accept="image/*" required>`;
            colorFields.appendChild(colorInput);
            colorCount++;
        } else {
            alert(`You can only add up to ${maxColors} colors.`);
        }
    });

    // Add more size fields
    addSizeButton.addEventListener("click", () => {
        const sizeInput = document.createElement("input");
        sizeInput.type = "text";
        sizeInput.className = "form-input";
        sizeInput.name = "sizes[]";
        sizeInput.placeholder = "Enter size (e.g., S, M, L)";
        sizeInput.required = true;
        sizeFields.appendChild(sizeInput);
    });

    // Save details and close modal
    saveDetailsButton.addEventListener("click", () => {
        alert("Clothing details saved!");
        modal.style.display = "none";
    });
});
