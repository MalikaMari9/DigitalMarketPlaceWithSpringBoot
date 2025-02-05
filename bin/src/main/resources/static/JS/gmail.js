document.getElementById('forgotPasswordForm').addEventListener('submit', function (e) {
    e.preventDefault();
    const email = document.getElementById('email').value;

    if (email) {
        alert(`Password reset link has been sent to ${email}`);
        document.getElementById('forgotPasswordForm').reset();
    } else {
        alert('Please enter a valid email address');
    }
});

// Automatically focus the cursor inside the input box when the page loads
// Automatically focus the cursor inside the input box when the page loads
document.addEventListener("DOMContentLoaded", () => {
    const emailInput = document.getElementById("email");
    emailInput.focus();
});
