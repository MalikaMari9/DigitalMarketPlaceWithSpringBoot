document.addEventListener('DOMContentLoaded', function() {
    // Elements
    const header = document.getElementById('headeryoon');
    const menuBtn = document.getElementById('menuBtnyoon');
    const closeBtn = document.getElementById('closeBtnyoon');
    const sidebar = document.getElementById('sidebaryoon');
    const categoriesBtn = document.getElementById('categoriesBtnyoon');
    const categoriesDropdown = document.getElementById('categoriesDropdownyoon');
    
    // Cart count
    let cartCount = 0;
    
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

async function updateCartCount() {
    try {
        const response = await fetch("/cart/count"); // Ensure this matches your backend endpoint
        if (!response.ok) {
            throw new Error("Failed to fetch cart count");
        }

        const data = await response.json();
        const cartCountElement = document.getElementById("cartCountyoon");

        if (cartCountElement) {
            cartCountElement.textContent = data.cartCount;
            cartCountElement.style.display = data.cartCount > 0 ? "inline" : "none";
        }
    } catch (error) {
        console.error("❌ Error fetching cart count:", error);
    }
}
async function updateNotificationIcon() {
    try {
        const response = await fetch("/notifications/unread-count");
        if (!response.ok) throw new Error("Failed to fetch unread notifications");

        const data = await response.json();
        const notifCountElement = document.getElementById("notifCount");
        const notifIconElement = document.getElementById("notifIcon");

        if (notifCountElement && notifIconElement) {
            if (data.unreadCount > 0) {
                notifCountElement.textContent = data.unreadCount;
                notifCountElement.style.display = "flex"; // ✅ Show when unread exists
                notifIconElement.setAttribute("data-lucide", "bell-ring"); // 🔔 Unread notifications
            } else {
                notifCountElement.style.display = "none";
                notifIconElement.setAttribute("data-lucide", "bell"); // 🔕 Default bell
            }
        }

        lucide.createIcons(); // ✅ Refresh Lucide icons dynamically
    } catch (error) {
        console.error("❌ Error fetching unread notifications:", error);
    }
}

// ✅ Auto-update on page load
document.addEventListener("DOMContentLoaded", updateNotificationIcon);


// ✅ Fetch cart count on every page load
document.addEventListener("DOMContentLoaded", updateCartCount);
