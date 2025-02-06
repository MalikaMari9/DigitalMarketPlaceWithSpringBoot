class SlideannSlider {
    constructor() {
        this.currentSlide = 0;
        this.slides = document.querySelectorAll('.slideann-slide');
        this.autoPlayInterval = null;
        this.isAutoPlaying = true;

        // Create dots
        this.createDots();
        
        // Set up initial state
        this.showSlide(0);
        
        // Add event listeners
        this.setupEventListeners();
        
        // Start autoplay
        this.startAutoPlay();
    }

    createDots() {
        const dotsContainer = document.querySelector('.slideann-dots');
        this.slides.forEach((_, index) => {
            const dot = document.createElement('button');
            dot.classList.add('slideann-dot');
            dot.setAttribute('aria-label', `Go to announcement ${index + 1}`);
            dot.addEventListener('click', () => this.showSlide(index));
            dotsContainer.appendChild(dot);
        });
        this.dots = dotsContainer.querySelectorAll('.slideann-dot');
    }

    setupEventListeners() {
        // Arrow navigation
        document.querySelector('.slideann-prev').addEventListener('click', () => this.previousSlide());
        document.querySelector('.slideann-next').addEventListener('click', () => this.nextSlide());

        // Pause on hover
        const slider = document.querySelector('.slideann-slider');
        slider.addEventListener('mouseenter', () => this.pauseAutoPlay());
        slider.addEventListener('mouseleave', () => this.startAutoPlay());

        // Keyboard navigation
        document.addEventListener('keydown', (e) => {
            if (e.key === 'ArrowLeft') this.previousSlide();
            if (e.key === 'ArrowRight') this.nextSlide();
        });
    }

    showSlide(index) {
        // Remove active class from current slide and dot
        this.slides[this.currentSlide].classList.remove('active');
        this.dots[this.currentSlide].classList.remove('active');

        // Update current slide index
        this.currentSlide = index;

        // Add active class to new slide and dot
        this.slides[this.currentSlide].classList.add('active');
        this.dots[this.currentSlide].classList.add('active');
    }

    nextSlide() {
        const newIndex = (this.currentSlide + 1) % this.slides.length;
        this.showSlide(newIndex);
    }

    previousSlide() {
        const newIndex = (this.currentSlide - 1 + this.slides.length) % this.slides.length;
        this.showSlide(newIndex);
    }

    startAutoPlay() {
        if (!this.isAutoPlaying) {
            this.isAutoPlaying = true;
            this.autoPlayInterval = setInterval(() => this.nextSlide(), 3000);
        }
    }

    pauseAutoPlay() {
        if (this.isAutoPlaying) {
            this.isAutoPlaying = false;
            clearInterval(this.autoPlayInterval);
        }
    }
}

// Initialize the slider when the DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    new SlideannSlider();
});