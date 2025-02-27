document.addEventListener("DOMContentLoaded", function () {
    const readAllBtn = document.getElementById("readAllBtn");
    const clearAllBtn = document.getElementById("clearAllBtn");

    // ✅ Mark all notifications as read
    readAllBtn.addEventListener("click", async () => {
        try {
            const response = await fetch("/notifications/markAllRead", { method: "POST" });

            if (response.ok) {
                document.querySelectorAll(".notification-item").forEach((item) => {
                    item.classList.remove("unread");
                });
            }
        } catch (error) {
            console.error("❌ Error marking notifications as read:", error);
        }
    });

    // ✅ Clear all notifications
    clearAllBtn.addEventListener("click", async () => {
        if (!confirm("Are you sure you want to clear all notifications?")) return;

        try {
            const response = await fetch("/notifications/clearAll", { method: "DELETE" });

            if (response.ok) {
                document.getElementById("notifications-list").innerHTML = "<p>No new notifications.</p>";
            }
        } catch (error) {
            console.error("❌ Error clearing notifications:", error);
        }
    });
});
