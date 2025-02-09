document.addEventListener('DOMContentLoaded', () => {
    const categoriesGrid = document.querySelector('.categories-grid');
    const prevArrow = document.querySelector('.prev-arrow');
    const nextArrow = document.querySelector('.next-arrow');

    // Scroll amount for each click (width of one card plus gap)
    const scrollAmount = 520; // 500px card width + 20px gap

    // Update arrow visibility on load
    updateArrowVisibility();

    prevArrow.addEventListener('click', () => {
        categoriesGrid.scrollBy({
            left: -scrollAmount,
            behavior: 'smooth'
        });
        console.log('Scrolling to previous categories');
    });

    nextArrow.addEventListener('click', () => {
        categoriesGrid.scrollBy({
            left: scrollAmount,
            behavior: 'smooth'
        });
        console.log('Scrolling to next categories');
    });

    // Track scroll position to update arrow visibility
    categoriesGrid.addEventListener('scroll', updateArrowVisibility);

    function updateArrowVisibility() {
        const isAtStart = categoriesGrid.scrollLeft === 0;
        const isAtEnd = categoriesGrid.scrollLeft + categoriesGrid.clientWidth >= categoriesGrid.scrollWidth - 1;

        prevArrow.style.opacity = isAtStart ? '0.5' : '1';
        prevArrow.style.pointerEvents = isAtStart ? 'none' : 'auto';
        
        nextArrow.style.opacity = isAtEnd ? '0.5' : '1';
        nextArrow.style.pointerEvents = isAtEnd ? 'none' : 'auto';
    }

    // Log category exploration


    // Handle window resize
    window.addEventListener('resize', updateArrowVisibility);
});