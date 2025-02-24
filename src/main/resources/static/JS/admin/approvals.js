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

function openImageModal(img) {
    const modal = document.getElementById("imageModal");
    const modalImage = document.getElementById("modalImage");

    modal.style.display = "flex";
    modalImage.src = img.src;
}

function closeImageModal(event) {
    // ✅ If no event (clicked directly from JS), just close the modal
    if (!event || !event.target) {
        document.getElementById("imageModal").style.display = "none";
        return;
    }

    // ✅ Prevent closing when clicking inside the modal
    if (event.target.classList.contains("modal")) {
        document.getElementById("imageModal").style.display = "none";
    }
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


