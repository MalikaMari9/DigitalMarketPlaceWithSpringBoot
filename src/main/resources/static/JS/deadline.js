document.addEventListener("DOMContentLoaded", () => {
    function checkAuctionExpiration() {
        const countdownElement = document.getElementById("countdown");
        const endTime = countdownElement.getAttribute("data-endtime");

        if (!endTime) return;

        const auctionEndTime = new Date(endTime).getTime();
        const currentTime = new Date().getTime();

        if (currentTime >= auctionEndTime) {
            console.log("⏳ Auction expired. Disabling bid buttons and input.");

            // ✅ Disable bid section and buttons
            document.getElementById("userBid").disabled = true;
            document.getElementById("realTimeBidBtn").disabled = true;
            document.getElementById("sniperBidBtn").disabled = true;
        }
    }

    // ✅ Check every second (1000 milliseconds)
    setInterval(checkAuctionExpiration, 1000);
});
