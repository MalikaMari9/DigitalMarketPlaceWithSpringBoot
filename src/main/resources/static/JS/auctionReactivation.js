
    function reactivateAuction(button) {
        const auctionID = button.getAttribute("data-auction-id");

        if (!auctionID) {
            alert("Error: Auction ID is missing.");
            return;
        }

        fetch(`/reactivate/${auctionID}`, {
            method: "POST"
        })
        .then(response => {
            if (response.ok) {
                alert("✅ Auction has been reactivated for another 7 days!");
                location.reload(); // ✅ Refresh to show updated auction status
            } else {
                alert("❌ Failed to reactivate auction.");
            }
        })
        .catch(error => {
            alert("❌ Error: " + error.message);
        });
    }

