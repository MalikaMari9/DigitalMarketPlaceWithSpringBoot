

// Function to switch between profile sections
function showSection(sectionId) {
    // Hide all tab contents
    document.querySelectorAll('.tab-content').forEach(content => content.classList.remove('active'));

    // Remove active class from all tabs
    document.querySelectorAll('.tab').forEach(tab => tab.classList.remove('active'));

    // Show selected tab content
    document.getElementById(sectionId).classList.add('active');

    // Highlight the active tab
    document.querySelectorAll('.tab').forEach(tab => {
        if (tab.textContent.toLowerCase() === sectionId.replace('-', ' ')) {
            tab.classList.add('active');
        }
    });
}

document.addEventListener('DOMContentLoaded', function () {
    if (window.location.hash === "#reviews") {
        showSection("reviews");
    }
});


// Toggle between truncated and full bio
function toggleBio() {
    const bioTextElement = document.getElementById('bioText');
    const toggleButton = document.getElementById('toggleBioButton');

    if (!bioTextElement.dataset.fullText) {
        bioTextElement.dataset.fullText = bioTextElement.textContent.trim(); // Store full text
    }

    const fullText = bioTextElement.dataset.fullText;
    const truncatedText = truncateText(fullText, 20); // Adjust word limit as needed

    if (toggleButton.textContent === 'See More') {
        bioTextElement.textContent = fullText; // Show full text
        toggleButton.textContent = 'See Less';
    } else {
        bioTextElement.textContent = truncatedText; // Show truncated text
        toggleButton.textContent = 'See More';
    }
}

// Helper function to truncate text
function truncateText(text, wordLimit) {
    const words = text.split(' ');
    return words.length > wordLimit ? words.slice(0, wordLimit).join(' ') + '...' : text;
}

// Initialize the truncated bio on page load
document.addEventListener('DOMContentLoaded', () => {
    const bioTextElement = document.getElementById('bioText');
    const toggleButton = document.getElementById('toggleBioButton');

    if (bioTextElement && toggleButton) {
        const fullText = bioTextElement.textContent.trim();
        const truncatedText = truncateText(fullText, 20); // Adjust word limit

        if (fullText !== truncatedText) {
            bioTextElement.textContent = truncatedText;
            toggleButton.style.display = 'inline-block'; // Show the button if truncation is needed
        } else {
            toggleButton.style.display = 'none'; // Hide button if bio is short
        }
    }
});

// "See More" button for other expandable elements
document.querySelectorAll('.see-more').forEach(button => {
    button.addEventListener('click', () => {
        const expanded = button.classList.toggle('expanded');
        button.innerHTML = expanded ? 'See less &#9650;' : 'See more &#9660;';
        // Implement logic for expanding/collapsing additional details
    });
});

document.addEventListener("DOMContentLoaded", function () {
    const reviewForm = document.getElementById("reviewForm");
    const showReviewButton = document.getElementById("showReviewForm");

    if (showReviewButton) {
        showReviewButton.addEventListener("click", function () {
            reviewForm.style.display = (reviewForm.style.display === "none" || reviewForm.style.display === "") 
                ? "block" 
                : "none";
        });
    }
});
function showEditForm(reviewID) {
    let editForm = document.getElementById("editForm" + reviewID);
    editForm.style.display = (editForm.style.display === "none" || editForm.style.display === "") ? "block" : "none";
}
function cancelReview() {
    document.getElementById("reviewForm").style.display = "none"; // Hide the Add Review form
}

function cancelEdit(reviewID) {
    document.getElementById("editForm" + reviewID).style.display = "none"; // Hide the Edit Review form
}
