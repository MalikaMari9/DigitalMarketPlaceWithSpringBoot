document.addEventListener("DOMContentLoaded", () => {
    const productGrids = document.querySelectorAll(".pg-products-grid");

    productGrids.forEach((productGrid) => {
        const container = productGrid.closest(".pg-products-container");
        const prevButton = container.querySelector(".pg-nav-arrow.pg-prev");
        const nextButton = container.querySelector(".pg-nav-arrow.pg-next");

        const scrollAmount = 300; // Adjust to fit your card width

        // ✅ Enable smooth horizontal scrolling with mouse wheel
        productGrid.addEventListener("wheel", (event) => {
            event.preventDefault();
            productGrid.scrollBy({
                left: event.deltaY > 0 ? scrollAmount : -scrollAmount,
                behavior: "smooth",
            });
        });

        // ✅ Scroll Left (Previous Button)
        prevButton.addEventListener("click", () => {
            productGrid.scrollBy({
                left: -scrollAmount,
                behavior: "smooth",
            });
        });

        // ✅ Scroll Right (Next Button)
        nextButton.addEventListener("click", () => {
            productGrid.scrollBy({
                left: scrollAmount,
                behavior: "smooth",
            });
        });

        // ✅ Update arrow visibility when scrolling
        productGrid.addEventListener("scroll", () => updateArrowVisibility(productGrid, prevButton, nextButton));

        function updateArrowVisibility(grid, prevBtn, nextBtn) {
            prevBtn.style.opacity = grid.scrollLeft <= 0 ? "0.5" : "1";
            prevBtn.style.pointerEvents = grid.scrollLeft <= 0 ? "none" : "auto";

            nextBtn.style.opacity = grid.scrollLeft + grid.clientWidth >= grid.scrollWidth ? "0.5" : "1";
            nextBtn.style.pointerEvents = grid.scrollLeft + grid.clientWidth >= grid.scrollWidth ? "none" : "auto";
        }

        // Initialize arrow visibility
        updateArrowVisibility(productGrid, prevButton, nextButton);
    });
});
