document.addEventListener("DOMContentLoaded", function () {
    function updateCountdown() {
        let countdownElem = document.getElementById("countdown");
        let endTimeString = countdownElem.getAttribute("data-endtime");
        
        if (!endTimeString) {
            countdownElem.textContent = "No end time available";
            return;
        }

        let endTime = new Date(endTimeString).getTime();
        let now = new Date().getTime();
        let remaining = endTime - now;

        if (remaining > 0) {
            let days = Math.floor(remaining / (1000 * 60 * 60 * 24));
            let hours = Math.floor((remaining % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
            let minutes = Math.floor((remaining % (1000 * 60 * 60)) / (1000 * 60));
            let seconds = Math.floor((remaining % (1000 * 60)) / 1000);

            // Ensure two-digit formatting
            hours = String(hours).padStart(2, '0');
            minutes = String(minutes).padStart(2, '0');
            seconds = String(seconds).padStart(2, '0');

            countdownElem.textContent = `${days} days ${hours}:${minutes}:${seconds}`;
        } else {
            countdownElem.textContent = "Expired";
        }
    }

    updateCountdown(); // Initial update
    setInterval(updateCountdown, 1000); // Update every second
});
