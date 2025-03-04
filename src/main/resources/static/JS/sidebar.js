document.addEventListener('DOMContentLoaded', function () {
    // Elements
    const header = document.getElementById('headeryoon');
    const menuBtn = document.getElementById('menuBtnyoon');
    const closeBtn = document.getElementById('closeBtnyoon');
    const sidebar = document.getElementById('sidebaryoon');
    const categoriesBtn = document.getElementById('categoriesBtnyoon');
    const categoriesDropdown = document.getElementById('categoriesDropdownyoon');

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

    document.addEventListener('click', (e) => {
        if (sidebar?.classList.contains('activeyoon') &&
            !sidebar.contains(e.target) &&
            e.target !== menuBtn) {
            sidebar.classList.remove('activeyoon');
        }
    });

    // Update cart count
    async function updateCartCount() {
        try {
            const response = await fetch("/cart/count");
            if (!response.ok) throw new Error("Failed to fetch cart count");

            const data = await response.json();
            const cartCountElement = document.getElementById("cartCountyoon");

            if (cartCountElement) {
                cartCountElement.textContent = data.cartCount;
                cartCountElement.style.display = data.cartCount > 0 ? "flex" : "none";
            }
        } catch (error) {
            console.error("❌ Error fetching cart count:", error);
        }
    }

    // Update notification count
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
                    notifCountElement.style.display = "flex"; 
                    notifIconElement.setAttribute("data-lucide", "bell-ring"); 
                } else {
                    notifCountElement.style.display = "none";
                    notifIconElement.setAttribute("data-lucide", "bell"); 
                }
            }

            lucide.createIcons();
        } catch (error) {
            console.error("❌ Error fetching unread notifications:", error);
        }
    }

    // ✅ Fetch undelivered order count
    async function updateUndeliveredOrderCount() {
        try {
            const response = await fetch("/confirmDelivery/count");
            if (!response.ok) throw new Error("Failed to fetch undelivered order count");

            const data = await response.json();
            const undeliveredCountElement = document.getElementById("undeliveredCount");

            if (undeliveredCountElement) {
                if (data.count > 0) {
                    undeliveredCountElement.textContent = data.count;
                    undeliveredCountElement.style.display = "flex";
                } else {
                    undeliveredCountElement.style.display = "none";
                }
            }
        } catch (error) {
            console.error("❌ Error fetching undelivered order count:", error);
        }
    }

    // ✅ Auto-update on page load
    updateCartCount();
    updateNotificationIcon();
    updateUndeliveredOrderCount();

    // Set copyright year
    const currentYear = new Date().getFullYear();
    document.getElementById('copyright-yearyoon').textContent = `© ${currentYear}`;

    // Newsletter Subscription
    const newsletterInput = document.getElementById('newsletter-emailyoon');
    if (newsletterInput) {
        newsletterInput.addEventListener('keypress', function(event) {
            if (event.key === 'Enter') {
                const email = this.value.trim();
                if (/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
                    alert('Thank you for subscribing!');
                    this.value = '';
                } else {
                    alert('Please enter a valid email address');
                }
            }
        });
    }

    // Initialize Lucide Icons
    lucide.createIcons();
});
