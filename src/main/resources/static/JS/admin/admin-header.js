document.addEventListener("DOMContentLoaded", function () {
    // Elements
    const header = document.getElementById("headeradmin");
    const menuBtn = document.getElementById("menuBtnadmin");
    const closeBtn = document.getElementById("closeBtnadmin");
    const sidebar = document.getElementById("sidebaradmin");
    const adminNotifCount = document.getElementById("adminNotifCount"); // ✅ Notification count element

    // ✅ Scroll handler for header
    window.addEventListener("scroll", () => {
        if (window.scrollY > 20) {
            header?.classList.add("scrolled");
        } else {
            header?.classList.remove("scrolled");
        }
    });

    // ✅ Sidebar toggle handlers
    menuBtn?.addEventListener("click", () => {
        console.log("Menu button clicked"); // Debug log
        sidebar?.classList.add("active");
    });

    closeBtn?.addEventListener("click", () => {
        console.log("Close button clicked"); // Debug log
        sidebar?.classList.remove("active");
    });

    // ✅ Close sidebar when clicking outside
    document.addEventListener("click", (e) => {
        if (
            sidebar?.classList.contains("active") &&
            !sidebar.contains(e.target) &&
            e.target !== menuBtn
        ) {
            sidebar.classList.remove("active");
        }
    });

    // ✅ Fetch unread admin notifications count
    fetch("/admin/notifications/unread-count")
        .then(response => response.json())
        .then(data => {
            if (data.unreadCount > 0) {
                adminNotifCount.textContent = data.unreadCount;
                adminNotifCount.style.display = "flex"; // ✅ Show notification count
            } else {
                adminNotifCount.style.display = "none"; // ✅ Hide if no notifications
            }
        })
        .catch(error => console.error("❌ Error fetching admin notification count:", error));

    // ✅ Create icons
    lucide.createIcons();
});
