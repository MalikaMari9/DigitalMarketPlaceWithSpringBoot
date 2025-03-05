document.addEventListener("DOMContentLoaded", function () {
    const countdownElements = document.querySelectorAll(".countdown");

    countdownElements.forEach(function (element) {
        const deadline = new Date(element.getAttribute("data-deadline")).getTime();

        function updateCountdown() {
            const now = new Date().getTime();
            const timeLeft = deadline - now;

            if (timeLeft > 0) {
                const hours = Math.floor(timeLeft / (1000 * 60 * 60));
                const minutes = Math.floor((timeLeft % (1000 * 60 * 60)) / (1000 * 60));
                element.textContent = `${hours}h ${minutes}m`;
            } else {
                element.textContent = "Payment Expired";
                element.style.color = "red";
            }
        }

        updateCountdown();
        setInterval(updateCountdown, 1000);
    });
});
