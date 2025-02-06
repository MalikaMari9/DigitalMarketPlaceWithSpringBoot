document.addEventListener('DOMContentLoaded', () => {
    const carousel = document.querySelector('.carousel');
    const slides = document.querySelectorAll('.slide');
    const prevButton = document.querySelector('.prev');
    const nextButton = document.querySelector('.next');
    const dotsContainer = document.querySelector('.carousel-dots');
    
    let currentSlide = 0;
    let isAutoPlaying = true;
    const autoPlayInterval = 5000;
    let autoPlayTimer;

    // Create dots
    slides.forEach((_, index) => {
        const dot = document.createElement('div');
        dot.classList.add('dot');
        if (index === 0) dot.classList.add('active');
        dot.addEventListener('click', () => goToSlide(index));
        dotsContainer.appendChild(dot);
    });

    const dots = document.querySelectorAll('.dot');

    function updateSlides() {
        slides.forEach((slide, index) => {
            slide.classList.toggle('active', index === currentSlide);
        });
        dots.forEach((dot, index) => {
            dot.classList.toggle('active', index === currentSlide);
        });
    }

    function goToSlide(index) {
        currentSlide = index;
        updateSlides();
        resetAutoPlay();
    }

    function nextSlide() {
        currentSlide = (currentSlide + 1) % slides.length;
        updateSlides();
        resetAutoPlay();
    }

    function previousSlide() {
        currentSlide = (currentSlide - 1 + slides.length) % slides.length;
        updateSlides();
        resetAutoPlay();
    }

    function startAutoPlay() {
        if (autoPlayTimer) clearInterval(autoPlayTimer);
        autoPlayTimer = setInterval(nextSlide, autoPlayInterval);
    }

    function resetAutoPlay() {
        if (isAutoPlaying) {
            startAutoPlay();
        }
    }

    // Event listeners
    prevButton.addEventListener('click', previousSlide);
    nextButton.addEventListener('click', nextSlide);

    carousel.addEventListener('mouseenter', () => {
        isAutoPlaying = false;
        if (autoPlayTimer) clearInterval(autoPlayTimer);
    });

    carousel.addEventListener('mouseleave', () => {
        isAutoPlaying = true;
        startAutoPlay();
    });

    // Start autoplay
    startAutoPlay();

    // Log initial setup
    console.log('Carousel initialized with:', {
        numberOfSlides: slides.length,
        autoPlayInterval,
        currentSlide
    });
});