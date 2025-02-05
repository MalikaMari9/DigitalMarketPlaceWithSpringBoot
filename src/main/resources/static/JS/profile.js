function showSection(sectionId) {
    // Hide all tab contents
    const contents = document.querySelectorAll('.tab-content');
    contents.forEach(content => content.classList.remove('active'));

    // Remove active class from all tabs
    const tabs = document.querySelectorAll('.tab');
    tabs.forEach(tab => tab.classList.remove('active'));

    // Show selected tab content
    document.getElementById(sectionId).classList.add('active');

    // Highlight the active tab
    const activeTab = [...tabs].find(tab =>
        tab.textContent.toLowerCase() === sectionId.replace('-', ' ')
    );
    if (activeTab) activeTab.classList.add('active');
}

// Toggle between truncated and full bio
function toggleBio() {
    const bioTextElement = document.getElementById('bioText');
    const toggleButton = document.getElementById('toggleBioButton');
    const fullText = bioTextElement.dataset.fullText;
    const truncatedText = truncateText(fullText, 20); // Adjust the word limit as needed

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
    if (words.length > wordLimit) {
        return words.slice(0, wordLimit).join(' ') + '...';
    }
    return text;
}

// Initialize the truncated bio on page load
document.addEventListener('DOMContentLoaded', () => {
    const bioTextElement = document.getElementById('bioText');
    const fullText = bioTextElement.dataset.fullText;
    bioTextElement.textContent = truncateText(fullText, 20); // Adjust word limit
});

document.querySelectorAll('.see-more').forEach(button => {
    button.addEventListener('click', () => {
        const expanded = button.classList.toggle('expanded');
        button.innerHTML = expanded ? 'See less &#9650;' : 'See more &#9660;';
        // Implement logic for expanding/collapsing additional details
    });
});
