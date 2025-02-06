class ProductGrid {
    constructor() {
        this.products = [
            {
                id: 1,
                name: "Vintage Leather Backpack",
                price: 89.99,
                seller: "Vintage Goods Co.",
                image: "Leather Bag.jpg ",
                rating: 4.5
            },
            {
                id: 2,
                name: "Wireless Headphones",
                price: 199.99,
                seller: "Tech Haven",
                image: "Wireless headphones.jpg",
                rating: 4.8
            },
            {
                id: 3,
                name: "Smart Watch",
                price: 299.99,
                seller: "Tech Haven",
                image: "Smart Watch.jpg",
                rating: 4.7
            },
            {
                id: 4,
                name: "Coffee Maker",
                price: 129.99,
                seller: "Kitchen Essentials",
                image: "Coffee Maker.jpg",
                rating: 4.6
            },
            {
                id: 5,
                name: "Laptop Stand",
                price: 49.99,
                seller: "Tech Haven",
                image: "Laptop Stand.jpg",
                rating: 4.4
            },
            {
                id: 6,
                name: "Wireless Mouse",
                price: 29.99,
                seller: "Tech Haven",
                image: "Wireless Mouse.jpg",
                rating: 4.3
            },
            {
                id: 7,
                name: "Desk Lamp",
                price: 39.99,
                seller: "Home Decor",
                image: "Desk Lamp.jpg",
                rating: 4.5
            },
            {
                id: 8,
                name: "Bluetooth Speaker",
                price: 79.99,
                seller: "Tech Haven",
                image: "Bluetooth Speaker.jpg",
                rating: 4.6
            }
        ];

        this.recommendedProducts = [
            {
                id: 9,
                name: "Premium Earbuds",
                price: 149.99,
                seller: "Audio Pro",
                image: "Premium Earbuds.jpg",
                rating: 4.8
            },
            {
                id: 10,
                name: "Smart Home Hub",
                price: 199.99,
                seller: "Smart Living",
                image: "Smart Home Hub.jpg",
                rating: 4.7
            },
            {
                id: 11,
                name: "Fitness Tracker",
                price: 79.99,
                seller: "Health Tech",
                image: "Fitness Tracker.jpg",
                rating: 4.6
            },
            {
                id: 12,
                name: "Portable Charger",
                price: 49.99,
                seller: "Power Solutions",
                image: "Portable Charger.jpg",
                rating: 4.5
            },
            {
                id: 13,
                name: "Smart Thermostat",
                price: 129.99,
                seller: "Smart Living",
                image: "Smart Thermostat.jpg",
                rating: 4.7
            },
            {
                id: 14,
                name: "Security Camera",
                price: 159.99,
                seller: "Smart Security",
                image: "Security Camera.jpg",
                rating: 4.8
            },
            {
                id: 15,
                name: "Robot Vacuum",
                price: 299.99,
                seller: "Smart Living",
                image: "Robot Vacuum.jpg",
                rating: 4.6
            },
            {
                id: 16,
                name: "Smart Light Bulbs",
                price: 39.99,
                seller: "Smart Living",
                image: "Smart Light Bulb.jpg",
                rating: 4.5
            }
        ];

        this.grids = document.querySelectorAll('.pg-products-grid');
        this.currentPage = [0, 0]; // One for each grid
        this.itemsPerPage = 4;
        this.scrollAmount = this.calculateScrollAmount(); // Dynamic scroll amount
        
        this.initializeGrids();
        this.setupEventListeners();
        this.updateArrowVisibility();
    }

    calculateScrollAmount() {
        const grid = this.grids[0];
        if (!grid) return 0;
        const cardWidth = grid.offsetWidth / 4; // Width of one card (25% of container)
        return cardWidth * 4; // Scroll by 4 cards at a time
    }
    
    createProductCard(product) {
        return `
            <div class="pg-product-card">
                <div class="pg-product-image">
                    <img src="${product.image}" alt="${product.name}">
                    <button class="pg-wishlist-btn">♡</button>
                </div>
                <div class="pg-product-info">
                    <h3 class="pg-product-title">${product.name}</h3>
                    <p class="pg-price">$${product.price.toFixed(2)}</p>
                    <p class="pg-seller">${product.seller}</p>
                    <div class="pg-rating">
                        <div class="pg-stars">${'★'.repeat(Math.floor(product.rating))}${'☆'.repeat(5 - Math.floor(product.rating))}</div>
                        <span>${product.rating}</span>
                    </div>
                    <div class="pg-buttons">
                        <button class="pg-cart-btn">Add to Cart</button>
                        <button class="pg-view-btn">👁</button>
                    </div>
                </div>
            </div>
        `;
    }
    
    initializeGrids() {
        // Show all 8 items in the grid
        this.updateGrid(0, this.products);
        this.updateGrid(1, this.recommendedProducts);
    }
    
    updateGrid(gridIndex, products) {
        const grid = this.grids[gridIndex];
        grid.innerHTML = products.map(product => this.createProductCard(product)).join('');
    }
    
    setupEventListeners() {
        const prevButtons = document.querySelectorAll('.pg-nav-arrow.pg-prev');
        const nextButtons = document.querySelectorAll('.pg-nav-arrow.pg-next');
        
        prevButtons.forEach((btn, index) => {
            btn.addEventListener('click', () => {
                const grid = this.grids[index];
                grid.scrollBy({
                    left: -this.scrollAmount,
                    behavior: 'smooth'
                });
                console.log('Scrolling to previous products');
                this.updateArrowVisibility(index);
            });
        });
        
        nextButtons.forEach((btn, index) => {
            btn.addEventListener('click', () => {
                const grid = this.grids[index];
                grid.scrollBy({
                    left: this.scrollAmount,
                    behavior: 'smooth'
                });
                console.log('Scrolling to next products');
                this.updateArrowVisibility(index);
            });
        });
        
        // Add scroll event listeners to each grid
        this.grids.forEach((grid, index) => {
            grid.addEventListener('scroll', () => this.updateArrowVisibility(index));
        });
        
        document.addEventListener('click', (e) => {
            if (e.target.classList.contains('pg-wishlist-btn')) {
                e.target.textContent = e.target.textContent === '♡' ? '♥' : '♡';
            }
        });

        // Handle window resize
        window.addEventListener('resize', () => {
            this.scrollAmount = this.calculateScrollAmount();
            this.grids.forEach((_, index) => this.updateArrowVisibility(index));
        });
    }
    
    updateArrowVisibility(gridIndex) {
        const grid = this.grids[gridIndex];
        const container = grid.closest('.pg-products-container');
        const prevButton = container.querySelector('.pg-nav-arrow.pg-prev');
        const nextButton = container.querySelector('.pg-nav-arrow.pg-next');
        
        const isAtStart = grid.scrollLeft <= 0;
        const isAtEnd = grid.scrollLeft + grid.clientWidth >= grid.scrollWidth - 2;
        
        prevButton.style.opacity = isAtStart ? '0.5' : '1';
        prevButton.style.pointerEvents = isAtStart ? 'none' : 'auto';
        
        nextButton.style.opacity = isAtEnd ? '0.5' : '1';
        nextButton.style.pointerEvents = isAtEnd ? 'none' : 'auto';
    }
}

// Initialize when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    new ProductGrid();
});