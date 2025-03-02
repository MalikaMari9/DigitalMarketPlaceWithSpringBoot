document.addEventListener("DOMContentLoaded", function () {
    const createdDateInput = document.getElementById("item-created-date");
    const deadlineInput = document.getElementById("item-deadline");

    createdDateInput.addEventListener("change", function () {
        validateDeadline(); // Revalidate deadline when start date changes
    });

    deadlineInput.addEventListener("change", function () {
        validateDeadline();
    });

    function validateDeadline() {
        const today = new Date();
        today.setHours(0, 0, 0, 0);

        const createdDate = new Date(createdDateInput.value);
        createdDate.setHours(0, 0, 0, 0);

        const selectedDeadline = new Date(deadlineInput.value);
        selectedDeadline.setHours(0, 0, 0, 0);

        // Ensure a valid start date is selected
        if (!createdDateInput.value) {
            alert("⚠️ Please select a Start Date first!");
            deadlineInput.value = "";
            return;
        }

        // ❌ Deadline must be in the future
        if (selectedDeadline <= today) {
            alert("🚨 The deadline must be a future date!");
            deadlineInput.value = ""; 
            return;
        }

        // ❌ Deadline must be after Created Date
        if (selectedDeadline <= createdDate) {
            alert("⚠️ The Deadline must be at least 1 day after the Start Date!");
            deadlineInput.value = ""; 
            return;
        }

        // ❌ Deadline must not be more than 14 days after the Created Date
        const maxDeadline = new Date(createdDate);
        maxDeadline.setDate(createdDate.getDate() + 14); // Max: Created Date + 14 days

        if (selectedDeadline > maxDeadline) {
            alert("⚠️ The Deadline cannot be more than 14 days from the Start Date!");
            deadlineInput.value = ""; 
            return;
        }
    }
});
