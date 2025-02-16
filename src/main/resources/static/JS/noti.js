async function updateNotificationCount() {
    try {
        const response = await fetch("/user/notifications/unread");
        if (!response.ok) throw new Error("Failed to fetch notifications");

        const data = await response.json();
        const notifCountElement = document.getElementById("notifCount");

        if (notifCountElement) {
            notifCountElement.textContent = data.length;
            notifCountElement.style.display = data.length > 0 ? "inline" : "none";
        }
    } catch (error) {
        console.error("❌ Error fetching notifications:", error);
    }
}

// ✅ Fetch unread notifications on page load
document.addEventListener("DOMContentLoaded", updateNotificationCount);
