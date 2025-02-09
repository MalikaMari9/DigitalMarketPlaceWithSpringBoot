document.addEventListener('DOMContentLoaded', () => {
    // Initialize Feather icons
    feather.replace();

    // Initialize sellers data from localStorage or use default data
    let sellersData = JSON.parse(localStorage.getItem('sellersData')) || [
        {
            sellerId: "S001",
            businessName: "Tech Solutions Ltd",
            businessType: "Electronics",
            approval: "approved",
            approvalDate: "2024-02-15",
            userId: "U123",
            name: "John Smith",
            nrcNo: "12/ABC(N)123456",
           nrcFront: "https://images.unsplash.com/photo-1486312338219-ce68d2c6f44d",
            nrcBack: "https://images.unsplash.com/photo-1485827404703-89b55fcc595e"
        },
        {
            sellerId: "S002",
            businessName: "Fashion Hub",
            businessType: "Clothing",
            approval: "pending",
            approvalDate: "-",
            userId: "U124",
            name: "Emma Wilson",
            nrcNo: "12/XYZ(N)789012",
           nrcFront: "https://images.unsplash.com/photo-1486312338219-ce68d2c6f44d",
            nrcBack: "https://images.unsplash.com/photo-1485827404703-89b55fcc595e"
        },
        {
            sellerId: "S003",
            businessName: "Food Express",
            businessType: "Restaurant",
            approval: "rejected",
            approvalDate: "2024-02-10",
            userId: "U125",
            name: "Michael Brown",
            nrcNo: "12/PQR(N)345678",
            nrcFront: "https://images.unsplash.com/photo-1486312338219-ce68d2c6f44d",
            nrcBack: "https://images.unsplash.com/photo-1485827404703-89b55fcc595e"
        }
    ];

    // Function to save sellers data to localStorage
    const saveSellersData = () => {
        localStorage.setItem('sellersData', JSON.stringify(sellersData));
    };

    // Function to add new seller
    const addSeller = (newSeller) => {
        sellersData.push(newSeller);
        saveSellersData();
        populateSellersTable(); // Refresh the table
    };

    // Function to create approval badge
    const createApprovalBadge = (status) => {
        const badge = document.createElement('span');
        badge.className = `approval-badge ${status}`;
        const icon = feather.icons[
            status === 'approved' ? 'check-circle' : 
            status === 'pending' ? 'clock' : 'x-circle'
        ].toSvg({ width: 16, height: 16 });
        badge.innerHTML = `${icon} ${status.charAt(0).toUpperCase() + status.slice(1)}`;
        return badge;
    };

    // Function to view NRC image
    const viewNRCImage = (imagePath, type, name) => {
        const modal = document.getElementById('nrcImageModal');
        const modalImage = document.getElementById('nrcImage');
        if (modal && modalImage) {
            modalImage.src = imagePath;
            modalImage.alt = `${name}'s ${type} NRC`;
            modal.style.display = 'block';
        }
    };

    // Make viewNRCImage function globally accessible
    window.viewNRCImage = viewNRCImage;

    // Function to populate sellers table
    const populateSellersTable = () => {
        const tableBody = document.getElementById('sellersTableBody');
        if (!tableBody) return;

        // Clear existing table content
        tableBody.innerHTML = '';

        sellersData.forEach(seller => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${seller.sellerId}</td>
                <td>${seller.businessName}</td>
                <td>${seller.businessType}</td>
                <td></td>
                <td>${seller.approvalDate}</td>
                <td>${seller.userId}</td>
                <td>${seller.name}</td>
                <td>${seller.nrcNo}</td>
                <td>
                    <button class="view-btn" onclick="viewNRCImage('${seller.nrcFront}', 'Front', '${seller.name}')">
                        ${feather.icons['eye'].toSvg({ width: 16, height: 16 })}
                    </button>
                </td>
                <td>
                    <button class="view-btn" onclick="viewNRCImage('${seller.nrcBack}', 'Back', '${seller.name}')">
                        ${feather.icons['eye'].toSvg({ width: 16, height: 16 })}
                    </button>
                </td>
            `;
            
            // Add approval badge
            const approvalCell = row.children[3];
            approvalCell.appendChild(createApprovalBadge(seller.approval));
            
            tableBody.appendChild(row);
        });
    };

    // Example of how to add a new seller
    window.addNewSeller = () => {
        const newSeller = {
            sellerId: `S${String(sellersData.length + 1).padStart(3, '0')}`,
            businessName: `Business ${sellersData.length + 1}`,
            businessType: "New Business",
            approval: "pending",
            approvalDate: "-",
            userId: `U${String(sellersData.length + 1).padStart(3, '0')}`,
            name: "New Seller",
            nrcNo: "12/XXX(N)000000",
            nrcFront: "nrc_front_default.jpg",
            nrcBack: "nrc_back_default.jpg"
        };
        addSeller(newSeller);
    };

    // Close modal when clicking the close button or outside the modal
    window.onclick = function(event) {
        const modal = document.getElementById('nrcImageModal');
        if (event.target === modal) {
            modal.style.display = 'none';
        }
    };

    document.querySelector('.close-modal')?.addEventListener('click', () => {
        const modal = document.getElementById('nrcImageModal');
        if (modal) modal.style.display = 'none';
    });

    // Initialize the table
    populateSellersTable();
});