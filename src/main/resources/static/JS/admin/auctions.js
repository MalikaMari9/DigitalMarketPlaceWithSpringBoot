document.addEventListener("DOMContentLoaded", () => {
    const trackButtons = document.querySelectorAll(".track-btn");
    const modal = document.getElementById("trackingModal");
    const closeModal = document.querySelector(".close-modal");
    const trackingData = document.getElementById("trackingData");

    trackButtons.forEach((button) => {
        button.addEventListener("click", async () => {
            const auctionID = button.getAttribute("data-auction-id");

            try {
                const response = await fetch(`/admin/auction/getTrackings?auctionID=${auctionID}`);
                const data = await response.json();

                if (response.ok && data.length > 0) {
                    trackingData.innerHTML = "";
                    data.forEach((track) => {
                        trackingData.innerHTML += `
                            <tr>
                                <td>${track.bidderName}</td>
                                <td>$${track.bidAmount}</td>
                                <td>${track.time}</td>
                            </tr>
                        `;
                    });

                    modal.style.display = "block";
                } else {
                    trackingData.innerHTML = `<tr><td colspan="3">No tracking data found.</td></tr>`;
                    modal.style.display = "block";
                }
            } catch (error) {
                console.error("Error fetching tracking data:", error);
            }
        });
    });

    closeModal.addEventListener("click", () => {
        modal.style.display = "none";
    });

    window.addEventListener("click", (event) => {
        if (event.target === modal) {
            modal.style.display = "none";
        }
    });
});
