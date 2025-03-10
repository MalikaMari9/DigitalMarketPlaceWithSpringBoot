document.addEventListener('DOMContentLoaded', function() {
    // Elements
    const header = document.getElementById('headeryoon');
    const menuBtn = document.getElementById('menuBtnyoon');
    const closeBtn = document.getElementById('closeBtnyoon');
    const sidebar = document.getElementById('sidebaryoon');
    const categoriesBtn = document.getElementById('categoriesBtnyoon');
    const categoriesDropdown = document.getElementById('categoriesDropdownyoon');
    
    // Cart count
    let cartCount = 3;
    
    // Scroll handler
    window.addEventListener('scroll', () => {
        if (window.scrollY > 20) {
            header.classList.add('scrolledyoon');
        } else {
            header.classList.remove('scrolledyoon');
        }
    });
    
    // Categories dropdown
    categoriesBtn?.addEventListener('click', (e) => {
        e.stopPropagation();
        if (categoriesDropdown) {
            categoriesDropdown.style.display = 
                categoriesDropdown.style.display === 'block' ? 'none' : 'block';
        }
    });
    
    // Close dropdown when clicking outside
    document.addEventListener('click', () => {
        if (categoriesDropdown) {
            categoriesDropdown.style.display = 'none';
        }
    });
    
    // Sidebar handlers
    menuBtn?.addEventListener('click', () => {
        sidebar?.classList.add('activeyoon');
    });
    
    closeBtn?.addEventListener('click', () => {
        sidebar?.classList.remove('activeyoon');
    });
    
    // Close sidebar when clicking outside
    document.addEventListener('click', (e) => {
        if (sidebar?.classList.contains('activeyoon') &&
            !sidebar.contains(e.target) &&
            e.target !== menuBtn) {
            sidebar.classList.remove('activeyoon');
        }
    });
    
    // Update cart count
    function updateCartCount(count) {
        const cartCountElement = document.getElementById('cartCountyoon');
        if (cartCountElement) {
            cartCountElement.textContent = count.toString();
            cartCountElement.style.display = count > 0 ? 'flex' : 'none';
        }
    }
    
    // Set copyright year
    const currentYear = new Date().getFullYear();
    document.getElementById('copyright-yearyoon').textContent = `© ${currentYear}`;
    
    // Newsletter subscription
    const newsletterInput = document.getElementById('newsletter-emailyoon');
    if (newsletterInput) {
        newsletterInput.addEventListener('keypress', function(event) {
            if (event.key === 'Enter') {
                const email = this.value.trim();
                if (isValidEmail(email)) {
                    console.log('Newsletter subscription for:', email);
                    alert('Thank you for subscribing!');
                    this.value = '';
                } else {
                    alert('Please enter a valid email address');
                }
            }
        });
    }

    // Email validation helper
    function isValidEmail(email) {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return emailRegex.test(email);
    }
    
    // Initialize cart count
    updateCartCount(cartCount);
    
    // Create icons
    lucide.createIcons();
});