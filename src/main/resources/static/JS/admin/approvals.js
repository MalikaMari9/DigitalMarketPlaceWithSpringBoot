
document.addEventListener('DOMContentLoaded', () => {
    // Initialize Feather icons
    feather.replace();

    // Initialize pending sellers data from localStorage or use default data
    let pendingSellersData = JSON.parse(localStorage.getItem('pendingSellersData')) || [
        {
            sellerId: "S004",
            businessName: "Green Foods",
            businessType: "Grocery",
            approval: "pending",
            userId: "U126",
            name: "Sarah Johnson",
            nrcNo: "12/DEF(N)901234",
            nrcFront: "https://images.unsplash.com/photo-1486312338219-ce68d2c6f44d",
            nrcBack: "https://images.unsplash.com/photo-1485827404703-89b55fcc595e"
        },
        {
            sellerId: "S005",
            businessName: "Digital Gadgets",
            businessType: "Electronics",
            approval: "pending",
            userId: "U127",
            name: "David Lee",
            nrcNo: "12/GHI(N)567890",
            nrcFront: "https://images.unsplash.com/photo-1486312338219-ce68d2c6f44d",
            nrcBack: "https://images.unsplash.com/photo-1485827404703-89b55fcc595e"
        }
    ];

    // Initialize pending items data from localStorage or use default data
    let pendingItemsData = JSON.parse(localStorage.getItem('pendingItemsData')) || [
        {
            itemId: "I001",
            itemName: "Smartphone X",
            description: "Latest model smartphone",
            price: "$599",
            quality: "New",
            sellType: "Fixed Price",
            status: "pending",
            condition: "Excellent",
            sellerId: "S001",
            categoryId: "C001",
            image: "https://images.unsplash.com/photo-1486312338219-ce68d2c6f44d"
        },
        {
            itemId: "I002",
            itemName: "Vintage Watch",
            description: "Classic timepiece",
            price: "$299",
            quality: "Used",
            sellType: "Auction",
            status: "pending",
            condition: "Good",
            sellerId: "S002",
            categoryId: "C002",
            image: "https://images.unsplash.com/photo-1485827404703-89b55fcc595e"
        }
    ];

    // Function to save pending sellers data to localStorage
    const savePendingSellersData = () => {
        localStorage.setItem('pendingSellersData', JSON.stringify(pendingSellersData));
    };

    // Function to save pending items data to localStorage
    const savePendingItemsData = () => {
        localStorage.setItem('pendingItemsData', JSON.stringify(pendingItemsData));
    };

    // Function to add new pending seller
    const addPendingSeller = (newSeller) => {
        pendingSellersData.push(newSeller);
        savePendingSellersData();
        populateSellerApprovalsTable();
    };

    // Function to add new pending item
    const addPendingItem = (newItem) => {
        pendingItemsData.push(newItem);
        savePendingItemsData();
        populateItemApprovalsTable();
    };

    // Function to create status badge
    const createStatusBadge = () => {
        const badge = document.createElement('span');
        badge.className = 'status-badge pending';
        const icon = feather.icons['clock'].toSvg({ width: 16, height: 16 });
        badge.innerHTML = `${icon} Pending`;
        return badge;
    };

    // Function to create view item button
    const createViewItemButton = (image) => {
        const button = document.createElement('button');
        button.className = 'view-btn';
        button.innerHTML = feather.icons['eye'].toSvg({ width: 16, height: 16 });
        button.onclick = () => showItemImage(image);
        return button;
    };

    // Function to show item image modal
    const showItemImage = (imageUrl) => {
        const modal = document.getElementById('itemImageModal');
        const modalImage = document.getElementById('itemImage');
        if (modal && modalImage) {
            modalImage.src = imageUrl;
            modalImage.alt = 'Item Image';
            modal.style.display = 'block';
        }
    };

    // Function to view NRC image
    window.viewNRCImage = (imagePath, type, name) => {
        const modal = document.getElementById('nrcImageModal');
        const modalImage = document.getElementById('nrcImage');
        if (modal && modalImage) {
            modalImage.src = imagePath;
            modalImage.alt = `${name}'s ${type} NRC`;
            modal.style.display = 'block';
        }
    };

    // Function to handle seller approval
    const handleSellerApproval = (sellerId, isApproved) => {
        console.log(`Seller ${sellerId} ${isApproved ? 'approved' : 'declined'}`);
        alert(`Seller ${sellerId} has been ${isApproved ? 'approved' : 'declined'}`);
    };

    // Function to handle item approval
    const handleItemApproval = (itemId, isApproved) => {
        console.log(`Item ${itemId} ${isApproved ? 'approved' : 'declined'}`);
        alert(`Item ${itemId} has been ${isApproved ? 'approved' : 'declined'}`);
    };

    // Function to populate sellers approvals table
    const populateSellerApprovalsTable = () => {
        const tableBody = document.getElementById('sellerApprovalsTableBody');
        if (!tableBody) return;

        // Clear existing table content
        tableBody.innerHTML = '';

        pendingSellersData.forEach(seller => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${seller.sellerId}</td>
                <td>${seller.businessName}</td>
                <td>${seller.businessType}</td>
                <td></td>
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
                <td>
                    <div class="action-buttons">
                        <button class="accept-btn" onclick="handleSellerApproval('${seller.sellerId}', true)">
                            ${feather.icons['check-circle'].toSvg({ width: 16, height: 16 })}
                        </button>
                        <button class="decline-btn" onclick="handleSellerApproval('${seller.sellerId}', false)">
                            ${feather.icons['x-circle'].toSvg({ width: 16, height: 16 })}
                        </button>
                    </div>
                </td>
            `;
            
            // Add status badge
            const statusCell = row.children[3];
            statusCell.appendChild(createStatusBadge());
            
            tableBody.appendChild(row);
        });
    };

    // Function to populate items approvals table
    const populateItemApprovalsTable = () => {
        const tableBody = document.getElementById('itemApprovalsTableBody');
        if (!tableBody) return;

        // Clear existing table content
        tableBody.innerHTML = '';

        pendingItemsData.forEach(item => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${item.itemId}</td>
                <td>${item.itemName}</td>
                <td>${item.description}</td>
                <td>${item.price}</td>
                <td>${item.quality}</td>
                <td>${item.sellType}</td>
                <td></td>
                <td>${item.condition}</td>
                <td>${item.sellerId}</td>
                <td>${item.categoryId}</td>
                <td></td>
                <td>
                    <div class="action-buttons">
                        <button class="accept-btn" onclick="handleItemApproval('${item.itemId}', true)">
                            ${feather.icons['check-circle'].toSvg({ width: 16, height: 16 })}
                        </button>
                        <button class="decline-btn" onclick="handleItemApproval('${item.itemId}', false)">
                            ${feather.icons['x-circle'].toSvg({ width: 16, height: 16 })}
                        </button>
                    </div>
                </td>
            `;
            
            // Add status badge
            const statusCell = row.children[6];
            statusCell.appendChild(createStatusBadge());

            // Add view item button
            const viewCell = row.children[10];
            viewCell.appendChild(createViewItemButton(item.image));
            
            tableBody.appendChild(row);
        });
    };

    // Example of how to add a new pending seller
    window.addNewPendingSeller = () => {
        const newSeller = {
            sellerId: `S${String(pendingSellersData.length + 1).padStart(3, '0')}`,
            businessName: `Business ${pendingSellersData.length + 1}`,
            businessType: "New Business",
            approval: "pending",
            userId: `U${String(pendingSellersData.length + 1).padStart(3, '0')}`,
            name: "New Seller",
            nrcNo: "12/XXX(N)000000",
            nrcFront: "nrc_front_default.jpg",
            nrcBack: "nrc_back_default.jpg"
        };
        addPendingSeller(newSeller);
    };

    // Example of how to add a new pending item
    window.addNewPendingItem = () => {
        const newItem = {
            itemId: `I${String(pendingItemsData.length + 1).padStart(3, '0')}`,
            itemName: "New Item",
            description: "Description for new item",
            price: "$0",
            quality: "New",
            sellType: "Fixed Price",
            status: "pending",
            condition: "New",
            sellerId: `S${String(pendingItemsData.length + 1).padStart(3, '0')}`,
            categoryId: `C${String(pendingItemsData.length + 1).padStart(3, '0')}`,
            image: "https://images.unsplash.com/photo-1486312338219-ce68d2c6f44d"
        };
        addPendingItem(newItem);
    };

    // Make functions globally accessible
    window.handleSellerApproval = handleSellerApproval;
    window.handleItemApproval = handleItemApproval;

    // Close modal when clicking the close button or outside the modal
    window.onclick = function(event) {
        const nrcModal = document.getElementById('nrcImageModal');
        const itemModal = document.getElementById('itemImageModal');
        if (event.target === nrcModal) {
            nrcModal.style.display = 'none';
        }
        if (event.target === itemModal) {
            itemModal.style.display = 'none';
        }
    };

    document.querySelectorAll('.close-modal').forEach(button => {
        button.addEventListener('click', () => {
            const nrcModal = document.getElementById('nrcImageModal');
            const itemModal = document.getElementById('itemImageModal');
            if (nrcModal) nrcModal.style.display = 'none';
            if (itemModal) itemModal.style.display = 'none';
        });
    });

    // Initialize the tables
    populateSellerApprovalsTable();
    populateItemApprovalsTable();
});