document.addEventListener("DOMContentLoaded", function () {
    const form = document.querySelector("form");

    document.getElementById("addressDetail")?.addEventListener("blur", validateAddressDetail);
    document.getElementById("building")?.addEventListener("blur", validateBuilding);
    document.getElementById("city")?.addEventListener("change", validateCity);
    document.getElementById("custName")?.addEventListener("blur", validateCustName);
    document.getElementById("phoneNumber")?.addEventListener("blur", validatePhoneNumber);
    document.getElementById("postalCode")?.addEventListener("blur", validatePostalCode);
    document.getElementById("township")?.addEventListener("change", validateTownship);

    form.addEventListener("submit", function (event) {
        if (!validateForm()) {
            event.preventDefault();
        }
    });
});

// ✅ Function to display error messages BELOW input fields
function setError(input, message) {
    let errorContainer = input.nextElementSibling;
    if (!errorContainer || !errorContainer.classList.contains("error-message")) {
        return;
    }
    errorContainer.textContent = message;
    input.classList.add("error-border");
}

function removeError(input) {
    let errorContainer = input.nextElementSibling;
    if (errorContainer && errorContainer.classList.contains("error-message")) {
        errorContainer.textContent = "";
    }
    input.classList.remove("error-border");
}

// ✅ Individual Field Validations
function validateBuilding() {
    let input = document.getElementById("building");
    if (input.value.trim().length > 40) {
        setError(input, "Building cannot exceed 40 characters.");
        return false;
    }
    removeError(input);
    return true;
}

function validateCity() {
    let input = document.getElementById("city");
    if (!input.value) {
        setError(input, "Please select a City.");
        return false;
    }
    removeError(input);
    return true;
}

function validateCustName() {
    let input = document.getElementById("custName");
    if (input.value.trim().length > 40) {
        setError(input, "Customer Name cannot exceed 40 characters.");
        return false;
    }
    removeError(input);
    return true;
}

function validatePhoneNumber() {
    let input = document.getElementById("phoneNumber");
    if (!/^\d{10,12}$/.test(input.value.trim())) {
        setError(input, "Phone Number must be between 10 and 12 digits.");
        return false;
    }
    removeError(input);
    return true;
}

function validatePostalCode() {
    let input = document.getElementById("postalCode");
    if (!/^\d{5,6}$/.test(input.value.trim())) {
        setError(input, "Postal Code must be 5 or 6 digits.");
        return false;
    }
    removeError(input);
    return true;
}

function validateTownship() {
    let input = document.getElementById("township");
    if (!input.value) {
        setError(input, "Please select a Township.");
        return false;
    }
    removeError(input);
    return true;
}

function validateAddressDetail() {
    let input = document.getElementById("addressDetail");
    if (!input.value.trim()) {
        setError(input, "Address Detail is required.");
        return false;
    }
    removeError(input);
    return true;
}

// ✅ Final Form Validation Function
function validateForm() {
    let isValid = true;

    if (!validateBuilding()) isValid = false;
    if (!validateCity()) isValid = false;
    if (!validateCustName()) isValid = false;
    if (!validatePhoneNumber()) isValid = false;
    if (!validatePostalCode()) isValid = false;
    if (!validateTownship()) isValid = false;
    if (!validateAddressDetail()) isValid = false;

    return isValid;
}
