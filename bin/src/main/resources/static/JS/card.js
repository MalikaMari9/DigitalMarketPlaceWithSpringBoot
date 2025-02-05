document.addEventListener("DOMContentLoaded", function () {
    const form = document.getElementById("payment-form");
  
    form.addEventListener("submit", function (event) {
      event.preventDefault(); // Prevent the form from submitting for testing purposes
  
      // Get input values
      const cardNumber = document.getElementById("card-number").value.trim();
      const expirationDate = document.getElementById("expiration-date").value.trim();
      const cvCode = document.getElementById("cv-code").value.trim();
      const cardOwner = document.getElementById("card-owner").value.trim();
      const postalCode = document.getElementById("postal-code").value.trim();
  
      // Error message elements
      const cardNumberError = document.getElementById("card-number-error");
      const expirationDateError = document.getElementById("expiration-date-error");
      const cvCodeError = document.getElementById("cv-code-error");
      const cardOwnerError = document.getElementById("card-owner-error");
      const postalCodeError = document.getElementById("postal-code-error");
  
      // Clear previous error messages
      cardNumberError.textContent = "";
      expirationDateError.textContent = "";
      cvCodeError.textContent = "";
      cardOwnerError.textContent = "";
      postalCodeError.textContent = "";
  
      // Validation flags
      let isValid = true;
  
      // Validate card number (16 digits)
      if (!/^\d{16}$/.test(cardNumber)) {
        cardNumberError.textContent = "Card number must be exactly 16 digits.";
        isValid = false;
      }
  
      // Validate expiration date (MM/YY format)
      if (!/^(0[1-9]|1[0-2])\/\d{2}$/.test(expirationDate)) {
        expirationDateError.textContent = "Expiration date must be in MM/YY format.";
        isValid = false;
      }
  
      // Validate CV code (3 digits)
      if (!/^\d{3}$/.test(cvCode)) {
        cvCodeError.textContent = "CV code must be exactly 3 digits.";
        isValid = false;
      }
  
      // Validate card owner (not empty)
      if (cardOwner === "") {
        cardOwnerError.textContent = "Card owner name cannot be empty.";
        isValid = false;
      }
  
      // Validate postal code (5-10 alphanumeric characters)
      if (!/^\w{5,10}$/.test(postalCode)) {
        postalCodeError.textContent = "Postal code must be 5-10 alphanumeric characters.";
        isValid = false;
      }
  
      // If all validations pass
      if (isValid) {
        alert("Payment details are valid! Form submitted.");
        form.reset(); // Clear the form fields
      }
    });
  });
  