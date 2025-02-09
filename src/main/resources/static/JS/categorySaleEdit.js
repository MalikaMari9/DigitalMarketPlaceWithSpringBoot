document.addEventListener("DOMContentLoaded", function () {
    const categoryButton = document.getElementById("category-button");
    const categoryList = document.getElementById("category-list");
    const selectedCategoryInput = document.getElementById("selected-category");
    const selectedCategoryName = document.getElementById("selected-category-name");

    // ✅ Toggle dropdown visibility
    categoryButton.addEventListener("click", function () {
        categoryList.style.display = categoryList.style.display === "block" ? "none" : "block";
    });

    // ✅ Toggle subcategories when clicking the arrow
    function toggleSubcategories(element) {
        let parentElement = element.closest("li"); // Get the parent category element
        let parentId = parentElement.getAttribute("data-id");
        let subcategories = document.querySelectorAll(`[data-parent="${parentId}"]`);

        subcategories.forEach(sub => {
            sub.style.display = sub.style.display === "none" ? "block" : "none";
        });

        // ✅ Toggle arrow direction
        element.classList.toggle("expanded");
    }

    // ✅ Select category when clicking the category name
    function selectCategory(element) {
        let categoryItem = element.closest("li");
        let categoryName = element.innerText.trim();
        let categoryId = categoryItem.getAttribute("data-id");

        // ✅ Update button text
        selectedCategoryName.innerText = categoryName;
        selectedCategoryInput.value = categoryId;

        // ✅ Close dropdown after selection
        categoryList.style.display = "none";
    }

    // ✅ Ensure arrows are correctly displayed for expandable categories
    document.querySelectorAll(".category-item").forEach(category => {
        let subcategories = category.nextElementSibling;
        if (subcategories && subcategories.classList.contains("subcategories") && subcategories.children.length > 0) {
            let arrow = category.querySelector(".arrow");
            if (!arrow) {
                arrow = document.createElement("span");
                arrow.classList.add("arrow");
                arrow.textContent = "▶";
                arrow.onclick = function () { toggleSubcategories(this); };
                category.appendChild(arrow);
            }
        }
    });

    // ✅ Hide dropdown when clicking outside
    document.addEventListener("click", function (event) {
        if (!categoryButton.contains(event.target) && !categoryList.contains(event.target)) {
            categoryList.style.display = "none";
        }
    });

    // ✅ Expose functions globally
    window.toggleSubcategories = toggleSubcategories;
    window.selectCategory = selectCategory;
});
