function validateLogin() {
    // Get the values of the username and password
    const username = document.getElementById("username").value;
    const password = document.getElementById("password").value;

    // Get the error message elements
    const usernameError = document.getElementById("usernameError");
    const passwordError = document.getElementById("passwordError");

    // Reset the error messages
    usernameError.textContent = "";
    passwordError.textContent = "";

    let valid = true;

    // Username validation: Must not be empty
    if (username.trim() === "") {
        usernameError.textContent = "Username cannot be empty.";
        valid = false;
    }

    // Password validation: Must not be empty
    if (password.trim() === "") {
        passwordError.textContent = "Password cannot be empty.";
        valid = false;
    }

    // Check if both fields are filled before submitting the form
    if (valid) {
        return true; // Allow form submission
    } else {
        return false; // Prevent form submission
    }
}
