document.addEventListener("DOMContentLoaded", () => {
    document.querySelectorAll(".bid-btn").forEach((button) => {
        button.addEventListener("click", async (event) => {
            event.preventDefault();

            const auctionID = button.getAttribute("data-auction-id");
            const userID = button.getAttribute("data-user-id");
            const bidType = button.getAttribute("data-bid-type"); // "real-time" or "sniper"
            const bidAmountInput = document.getElementById("userBid");

            if (!bidAmountInput) {
                alert("⚠️ Error: Bid amount field not found.");
                return;
            }

            const bidAmount = parseFloat(bidAmountInput.value.trim());

            if (!userID) {
                alert("⚠️ Please log in to place a bid.");
                return;
            }

            if (isNaN(bidAmount) || bidAmount <= 0) {
                alert("⚠️ Please enter a valid bid amount.");
                return;
            }

            try {
                // ✅ Fetch the current highest bid
                const highestBidResponse = await fetch(`/auction/getHighestBid?auctionID=${auctionID}`);
                const highestBidData = await highestBidResponse.json();

                if (highestBidResponse.ok && highestBidData.success) {
                    const highestBid = parseFloat(highestBidData.highestBid);
                    
                    if (!isNaN(highestBid) && bidAmount <= highestBid) {
                        alert(`⚠️ Your bid must be higher than the current highest bid of $${highestBid}`);
                        return;
                    }
                }

                // ✅ Proceed with bid placement
                const response = await fetch("/auction/placeBid", {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({ 
                        auctionID: auctionID,
                        userID: userID,
                        bidAmount: bidAmount,
                        bidType: bidType
                    })
                });

                const data = await response.json();

                if (response.ok && data.success) {
                    alert("✅ Bid placed successfully!");
                    location.reload(); // Refresh to update bid history
                } else {
                    alert("❌ Failed to place bid: " + data.message);
                }
            } catch (error) {
                console.error("❌ Error placing bid:", error);
                alert("❌ Network error! Please try again.");
            }
        });
    });
});
