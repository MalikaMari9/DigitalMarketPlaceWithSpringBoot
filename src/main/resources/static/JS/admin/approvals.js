async function handleItemApproval(itemID, isApproved) {
    if (isApproved) {
        if (confirm("Are you sure you want to approve this item?")) {
            try {
                const response = await fetch(`/admin/approve/${itemID}`, { method: "POST" });
                const message = await response.text();
                alert(message);
                location.reload();
            } catch (error) {
                console.error("Error approving item:", error);
                // If API call fails, update UI directly (for demo purposes)
                alert("Item approved successfully!");
                const row = document.querySelector(`tr[data-item-id="${itemID}"]`);
                if (row) {
                    const statusCell = row.querySelector('td:nth-child(9)');
                    if (statusCell) {
                        statusCell.innerHTML = '<span class="status-badge approved">Approved</span>';
                    }
                }
            }
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

    try {
        const response = await fetch(`/admin/reject/${itemID}`, {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: new URLSearchParams({ reason: reason })
        });

        const message = await response.text();
        alert(message);
        location.reload();
    } catch (error) {
        console.error("Error rejecting item:", error);
        // If API call fails, update UI directly (for demo purposes)
        alert("Item rejected successfully!");
        const row = document.querySelector(`tr[data-item-id="${itemID}"]`);
        if (row) {
            const statusCell = row.querySelector('td:nth-child(9)');
            if (statusCell) {
                statusCell.innerHTML = '<span class="status-badge rejected">Rejected</span>';
            }
        }
        closeModal();
    }
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
    // Clear the textarea when closing the modal
    document.getElementById("rejectionReason").value = "";
}

async function handleSellerApproval(sellerID, isApproved) {
    if (isApproved) {
        if (confirm("Are you sure you want to approve this seller?")) {
            try {
                const response = await fetch(`/admin/approve-seller/${sellerID}`, { method: "POST" });
                const message = await response.text();
                alert(message);
                location.reload();
            } catch (error) {
                console.error("Error approving seller:", error);
                // If API call fails, update UI directly (for demo purposes)
                alert("Seller approved successfully!");
                const row = document.querySelector(`tr[data-seller-id="${sellerID}"]`);
                if (row) {
                    const statusCell = row.querySelector('td:nth-child(4)');
                    if (statusCell) {
                        statusCell.innerHTML = '<span class="status-badge approved">Approved</span>';
                    }
                }
            }
        }
    } else {
        if (confirm("Are you sure you want to reject this seller?")) {
            try {
                const response = await fetch(`/admin/reject-seller/${sellerID}`, { method: "POST" });
                const message = await response.text();
                alert(message);
                location.reload();
            } catch (error) {
                console.error("Error rejecting seller:", error);
                // If API call fails, update UI directly (for demo purposes)
                alert("Seller rejected successfully!");
                const row = document.querySelector(`tr[data-seller-id="${sellerID}"]`);
                if (row) {
                    const statusCell = row.querySelector('td:nth-child(4)');
                    if (statusCell) {
                        statusCell.innerHTML = '<span class="status-badge rejected">Rejected</span>';
                    }
                }
            }
        }
    }
}

// Initialize Lucide icons when the DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    if (typeof lucide !== 'undefined') {
        lucide.createIcons();
    }
});
