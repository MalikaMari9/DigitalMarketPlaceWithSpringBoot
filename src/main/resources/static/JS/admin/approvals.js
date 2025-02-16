async function handleItemApproval(itemID, isApproved) {
    if (isApproved) {
        if (confirm("Are you sure you want to approve this item?")) {
            const response = await fetch(`/admin/approve/${itemID}`, { method: "POST" });
            const message = await response.text();
            alert(message);
            location.reload();
        }
    } else {
        document.getElementById("rejectionModal").style.display = "block";
        document.getElementById("rejectionModal").dataset.itemID = itemID;
    }
}

async function confirmRejection() {
    const itemID = document.getElementById("rejectionModal").dataset.itemID;
    const reason = document.getElementById("rejectionReason").value;

    if (!reason.trim()) {
        alert("Please provide a reason for rejection.");
        return;
    }

    const response = await fetch(`/admin/reject/${itemID}`, {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: new URLSearchParams({ reason: reason })
    });

    const message = await response.text();
    alert(message);
    location.reload();
}

function closeModal() {
    document.getElementById("rejectionModal").style.display = "none";
}

async function handleSellerApproval(sellerID, isApproved) {
    if (isApproved) {
        if (confirm("Are you sure you want to approve this seller?")) {
            const response = await fetch(`/admin/approve-seller/${sellerID}`, { method: "POST" });
            const message = await response.text();
            alert(message);
            location.reload();
        }
    } else {
        if (confirm("Are you sure you want to reject this seller?")) {
            const response = await fetch(`/admin/reject-seller/${sellerID}`, { method: "POST" });
            const message = await response.text();
            alert(message);
            location.reload();
        }
    }
}


