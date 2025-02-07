document.addEventListener("DOMContentLoaded", function () {
    const categoryButton = document.getElementById("category-button");
    const categoryList = document.getElementById("category-list");
    const selectedCategoryInput = document.getElementById("selected-category");

    // Toggle dropdown when clicking the button
    categoryButton.addEventListener("click", function () {
        categoryList.style.display = categoryList.style.display === "block" ? "none" : "block";
    });

    // Toggle subcategories when clicking the arrow
    function toggleSubcategories(element) {
        let parentId = element.parentElement.getAttribute("data-id"); // Arrow is inside the <li>, so we get the parent
        let subcategories = document.querySelectorAll(`[data-parent="${parentId}"]`);
        let arrow = element; // Arrow itself is clicked

        subcategories.forEach(sub => {
            sub.style.display = sub.style.display === "none" ? "block" : "none";
        });

        // Toggle arrow direction
        arrow.textContent = arrow.textContent === "▶" ? "▼" : "▶";
    }

    // Select category when clicking on the name
	// Select category and update button text (without arrow)
	function selectCategory(element) {
	    let categoryName = element.querySelector("span").innerText.trim(); // Get only the name
	    let categoryId = element.getAttribute("data-id");

	    // Update dropdown button text (without arrow)
	    categoryButton.innerText = categoryName;
	    selectedCategoryInput.value = categoryId;

	    // Hide dropdown after selection
	    categoryList.style.display = "none";
	}

    // Attach event listeners to category names (selecting category)
    document.querySelectorAll(".category-item span:not(.arrow)").forEach(category => {
        category.addEventListener("click", function (event) {
            event.stopPropagation(); // Prevent accidental dropdown close
            selectCategory(category.parentElement); // Select the <li> parent
        });
    });

    // Attach event listeners to arrows (expanding subcategories)
    document.querySelectorAll(".category-item .arrow").forEach(arrow => {
        arrow.addEventListener("click", function (event) {
            event.stopPropagation(); // Prevent selecting category
            toggleSubcategories(arrow);
        });
    });

    // Hide dropdown when clicking outside
    document.addEventListener("click", function (event) {
        if (!categoryButton.contains(event.target) && !categoryList.contains(event.target)) {
            categoryList.style.display = "none";
        }
    });

    // Expose functions globally
    window.toggleSubcategories = toggleSubcategories;
    window.selectCategory = selectCategory;
});
