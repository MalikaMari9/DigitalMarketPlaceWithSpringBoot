document.addEventListener("DOMContentLoaded", function () {
    const createdDateInput = document.getElementById("item-created-date");
    const deadlineInput = document.getElementById("item-deadline");

    createdDateInput.addEventListener("change", function () {
        const today = new Date();
        today.setHours(0, 0, 0, 0); // Normalize time

        const selectedCreatedDate = new Date(createdDateInput.value);
        selectedCreatedDate.setHours(0, 0, 0, 0);

        const minAllowedDate = new Date();
        minAllowedDate.setDate(today.getDate() + 1); // Min: Today + 1 day

        const maxAllowedDate = new Date();
        maxAllowedDate.setDate(today.getDate() + 5); // Max: Today + 5 days

        // ❌ Created Date must be at least 1 day from today
        if (selectedCreatedDate < minAllowedDate) {
            alert("🚨 The Start Date must be at least 1 day from today!");
            createdDateInput.value = ""; 
            return;
        }

        // ❌ Created Date must be at most 5 days from today
        if (selectedCreatedDate > maxAllowedDate) {
            alert("⚠️ The Start Date cannot be more than 5 days from today!");
            createdDateInput.value = ""; 
            return;
        }

        // ✅ If deadline is already selected, ensure it's valid
        if (deadlineInput.value) {
            validateDeadline();
        }
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

        // ❌ Deadline must be after Created Date
        if (createdDateInput.value && selectedDeadline <= createdDate) {
            alert("⚠️ The Deadline must be at least 1 day after the Start Date!");
            deadlineInput.value = ""; 
        }
    }
});
