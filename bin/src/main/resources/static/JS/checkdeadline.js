document.addEventListener('DOMContentLoaded', function () {
    const deadlineInput = document.getElementById('item-deadline');

    deadlineInput.addEventListener('input', function () {
        const today = new Date();
        const inputDate = new Date(deadlineInput.value);
        const maxDate = new Date();
        maxDate.setDate(today.getDate() + 14); // Add 14 days to today

        if (inputDate <= today) {
            alert('The deadline must be a future date.');
            deadlineInput.value = ''; // Clear the invalid input
        } else if (inputDate > maxDate) {
            alert('The deadline must not exceed 14 days from today.');
            deadlineInput.value = ''; // Clear the invalid input
        }
    });
});
