/**
 * userValidation.js
 */

function validateForm(event) {
    const usernameValid = validateUsername();
    const passwordValid = validatePassword();
    const phoneValid = validatePhone();
    const dobValid = validateDate();
    const passwordsMatch = checkPasswords();
  
    // Prevent form submission if validation fails
    return usernameValid && passwordValid && phoneValid && dobValid && passwordsMatch;
  }
  
  function validateUsername() {
    const username = document.getElementById("usn").value;
    const usernameError = document.getElementById("usernameError");
  
    if (username.length < 5 || username.length > 15) {
      usernameError.textContent = "Username must be between 5 and 15 characters.";
      return false;
    } else if (!/^[a-zA-Z0-9_]+$/.test(username)) {
      usernameError.textContent = "Username can only contain letters, numbers, and underscores.";
      return false;
    }
  
    usernameError.textContent = "";
    return true;
  }
  
  function validatePassword() {
    const password = document.getElementById("psw").value;
    const passwordMessage = document.getElementById("passwordMessage");
  
    if (password.length < 8) {
      passwordMessage.textContent = "Password must be at least 8 characters long.";
      return false;
    } else if (!/[A-Z]/.test(password)) {
      passwordMessage.textContent = "Password must contain at least one uppercase letter.";
      return false;
    } else if (!/[a-z]/.test(password)) {
      passwordMessage.textContent = "Password must contain at least one lowercase letter.";
      return false;
    } else if (!/[0-9]/.test(password)) {
      passwordMessage.textContent = "Password must contain at least one number.";
      return false;
    }
  
    passwordMessage.textContent = "";
    return true;
  }
  
  function checkPasswords() {
    const password = document.getElementById("psw").value;
    const confirmPassword = document.getElementById("confirmpsw").value;
    const message = document.getElementById("message");
  
    if (password !== confirmPassword) {
      message.textContent = "Passwords do not match.";
      return false;
    }
  
    message.textContent = "";
    return true;
  }
  
  function validatePhone() {
    const phone = document.getElementById("phone").value;
    const phoneMessage = document.getElementById("phoneMessage");
  
    if (!/^\d{10}$/.test(phone)) {
      phoneMessage.textContent = "Phone number must be 10 digits.";
      return false;
    }
  
    phoneMessage.textContent = "";
    return true;
  }
  
  function validateDate() {
    const dob = new Date(document.getElementById("dob").value);
    const dobMessage = document.getElementById("dobMessage");
    const today = new Date();
  
    if (isNaN(dob.getTime())) {
      dobMessage.textContent = "Please enter a valid date.";
      return false;
    } else if (dob >= today) {
      dobMessage.textContent = "Date of birth must be in the past.";
      return false;
    }
  
    dobMessage.textContent = "";
    return true;
  }
  
        
        
        
        function validateForm(event) {
            const isUsernameValid = validateUsername();
            const isPasswordValid = validatePassword();
            const arePasswordsMatching = checkPasswords();
            const isDateValid = validateDate();
            const isPhoneValid = validatePhone();
            // Prevent form submission if any validation fails
            if (!isUsernameValid || !isPasswordValid || !arePasswordsMatching || !isDateValid || !isPhoneValid) {
        event.preventDefault(); // Prevent form submission
        alert("Please fix the errors before submitting the form.");
    }
        }
		
		    function validateFormSeller(event) {
		        const isUsernameValid = validateBusinessname();
		        const isPasswordValid = validatePassword();
		        const arePasswordsMatching = checkPasswords();
		        const isDateValid = validateDate();
		        const isPhoneValid = validatePhone();
		        // Prevent form submission if any validation fails
		        if (!isUsernameValid || !isPasswordValid || !arePasswordsMatching || !isDateValid || !isPhoneValid) {
		    event.preventDefault(); // Prevent form submission
		    alert("Please fix the errors before submitting the form.");
		}
		    }
          
            function validateBusinessname() {
                //   const bsn = document.getElementById("").value;
                  const errorMsg = document.getElementById("BusinessnameError");
                  if (bsn.trim() === "") {
                    errorMsg.textContent = "Business name cannot be empty.";
                  } else {
                    errorMsg.textContent = "";
                  }
                }
            
                window.onload = function() {
                    // Example dynamic data handling
                    const bsn = "";  // Example dynamic data from a server
                    const BusinessnameError = "Business name is required.";  // Example error message
            
                    // Set initial business name value
                    document.getElementById('bsn').value = bsn;
                    document.getElementById('BusinessnameError').textContent = BusinessnameError;
                };
              
                window.onload = function() {
                    // Example of setting dynamic email value
                    const email = "";  // This value could come from a database or session
                    const emailError = "";  // Initially no error
            
                    // Set the email value in the input field
                    document.getElementById('email').value = email;
            
                    // Set any error messages (if any)
                    document.getElementById('emailError').textContent = emailError;
                };
            
                function validateEmail() {
                    const email = document.getElementById("email").value;
                    const emailError = document.getElementById("emailError");
            
                    // Regular expression to check for valid email format
                    const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
                    
                    if (!email.match(emailPattern)) {
                        emailError.textContent = "Please enter a valid email address.";
                    } else {
                        emailError.textContent = ""; // Clear error if valid
                    }
                }
                          

                function toggleInfo() {
                    const infoBox = document.getElementById("info-box");
                    infoBox.classList.toggle("visible");
                  }
                  
                  function showAlert() {
                    alert("Please enter your data according to your ID card for approval.");
                  }
                  
                  