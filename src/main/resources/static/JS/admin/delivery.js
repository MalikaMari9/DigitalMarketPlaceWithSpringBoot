
document.addEventListener('DOMContentLoaded', () => {
    // Initialize Feather icons
    feather.replace();

    // Initialize delivery data from localStorage or use default data
    let deliveryData = JSON.parse(localStorage.getItem('deliveryData')) || [
        {
            deliveryId: "DEL001",
            estimatedDeliveryDate: "2024-02-20",
            status: "pending",
            itemId: "ITEM001",
            itemImage: "https://images.unsplash.com/photo-1523275335684-37898b6baf30",
            itemStatus: "Shipped",
            sellerId: "SELLER001",
            recipientId: "REC001",
            recipientName: "John Smith",
            deliveryAddress: "123 Main St, New York, NY 10001"
        },
        {
            deliveryId: "DEL002",
            estimatedDeliveryDate: "2024-02-21",
            status: "in-transit",
            itemId: "ITEM002",
            itemImage: "https://images.unsplash.com/photo-1505740420928-5e560c06d30e",
            itemStatus: "In Transit",
            sellerId: "SELLER002",
            recipientId: "REC002",
            recipientName: "Emma Johnson",
            deliveryAddress: "456 Oak Ave, Los Angeles, CA 90001"
        },
        {
            deliveryId: "DEL003",
            estimatedDeliveryDate: "2024-02-19",
            status: "delivered",
            itemId: "ITEM003",
            itemImage: "https://images.unsplash.com/photo-1542291026-7eec264c27ff",
            itemStatus: "Delivered",
            sellerId: "SELLER003",
            recipientId: "REC003",
            recipientName: "Michael Brown",
            deliveryAddress: "789 Pine St, Chicago, IL 60601"
        }

        
    ];

    // Function to save delivery data to localStorage
    const saveDeliveryData = () => {
        localStorage.setItem('deliveryData', JSON.stringify(deliveryData));
    };

    // Function to add new delivery
    const addDelivery = (newDelivery) => {
        deliveryData.push(newDelivery);
        saveDeliveryData();
        populateDeliveryTable(); // Refresh the table
    };

    // Function to create status badge
    const createStatusBadge = (status) => {
        const badge = document.createElement('span');
        badge.className = `status-badge ${status}`;
        const displayText = status.charAt(0).toUpperCase() + status.slice(1).replace('-', ' ');
        badge.textContent = displayText;
        return badge;
    };

    // Function to create view button
    const createViewButton = (image) => {
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
        modalImage.src = imageUrl;
        modal.style.display = 'block';
    };

    // Close modal when clicking the close button
    document.querySelector('.close-modal').onclick = () => {
        document.getElementById('itemImageModal').style.display = 'none';
    };

    // Close modal when clicking outside of it
    window.onclick = (event) => {
        const modal = document.getElementById('itemImageModal');
        if (event.target === modal) {
            modal.style.display = 'none';
        }
    };

    // Function to populate delivery table
    const populateDeliveryTable = () => {
        const tableBody = document.getElementById('deliveryTableBody');
        if (!tableBody) return;

        // Clear existing table content
        tableBody.innerHTML = '';

        deliveryData.forEach(delivery => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${delivery.deliveryId}</td>
                <td>${delivery.estimatedDeliveryDate}</td>
                <td></td>
                <td>${delivery.itemId}</td>
                <td></td>
                <td>${delivery.itemStatus}</td>
                <td>${delivery.sellerId}</td>
                <td>${delivery.recipientId}</td>
                <td>${delivery.recipientName}</td>
                <td>${delivery.deliveryAddress}</td>
            `;
            
            // Add status badge
            const statusCell = row.children[2];
            statusCell.appendChild(createStatusBadge(delivery.status));

            // Add view button
            const imageCell = row.children[4];
            imageCell.appendChild(createViewButton(delivery.itemImage));
            
            tableBody.appendChild(row);
        });
    };

    // Example of how to add a new delivery (you can create a form or other UI elements to call this)
    window.addNewDelivery = () => {
        const newDelivery = {
            deliveryId: `DEL${String(deliveryData.length + 1).padStart(3, '0')}`,
            estimatedDeliveryDate: "2024-02-22",
            status: "pending",
            itemId: `ITEM${String(deliveryData.length + 1).padStart(3, '0')}`,
            itemImage: "https://images.unsplash.com/photo-1523275335684-37898b6baf30",
            itemStatus: "Processing",
            sellerId: `SELLER${String(deliveryData.length + 1).padStart(3, '0')}`,
            recipientId: `REC${String(deliveryData.length + 1).padStart(3, '0')}`,
            recipientName: "Test User",
            deliveryAddress: "Test Address"
        };
        addDelivery(newDelivery);
    };

    // Initialize the table
    populateDeliveryTable();
});