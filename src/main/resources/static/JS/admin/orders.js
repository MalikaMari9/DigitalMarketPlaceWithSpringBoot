document.addEventListener('DOMContentLoaded', () => {
    // Initialize Feather icons
    feather.replace();

    // Initialize orders data from localStorage or use default data
    let ordersData = JSON.parse(localStorage.getItem('ordersData')) || [
        {
            orderId: "ORD001",
            orderDate: "2024-02-15",
            customerName: "John Smith",
            itemId: "ITEM001",
            itemName: "Vintage Watch",
            quantity: 1,
            price: "$1,200",
            sellType: "auction",
            image: "https://images.unsplash.com/photo-1523275335684-37898b6baf30"
        },
        {
            orderId: "ORD002",
            orderDate: "2024-02-16",
            customerName: "Emma Johnson",
            itemId: "ITEM002",
            itemName: "Diamond Ring",
            quantity: 2,
            price: "$2,500",
            sellType: "direct",
            image: "https://images.unsplash.com/photo-1505740420928-5e560c06d30e"
        },
        {
            orderId: "ORD003",
            orderDate: "2024-02-17",
            customerName: "Michael Brown",
            itemId: "ITEM003",
            itemName: "Antique Vase",
            quantity: 1,
            price: "$800",
            sellType: "auction",
            image: "https://images.unsplash.com/photo-1542291026-7eec264c27ff"
        }
    ];

    // Function to save orders data to localStorage
    const saveOrdersData = () => {
        localStorage.setItem('ordersData', JSON.stringify(ordersData));
    };

    // Function to add new order
    const addOrder = (newOrder) => {
        ordersData.push(newOrder);
        saveOrdersData();
        populateOrdersTable(); // Refresh the table
    };

    // Function to create sell type badge
    const createSellTypeBadge = (type) => {
        const badge = document.createElement('span');
        badge.className = `sell-type-badge ${type}`;
        const icon = feather.icons[
            type === 'auction' ? 'trending-up' : 'shopping-cart'
        ].toSvg({ width: 16, height: 16 });
        const displayText = type === 'direct' ? 'Fixed Price' : type.charAt(0).toUpperCase() + type.slice(1);
        badge.innerHTML = `${icon} ${displayText}`;
        return badge;
    };

    // Function to create view order button
    const createViewOrderButton = (image) => {
        const button = document.createElement('button');
        button.className = 'view-order-btn';
        button.innerHTML = feather.icons['eye'].toSvg({ width: 16, height: 16 });
        button.onclick = () => showOrderImage(image);
        return button;
    };

    // Function to show order image modal
    const showOrderImage = (imageUrl) => {
        const modal = document.getElementById('orderImageModal');
        const modalImage = document.getElementById('orderImage');
        modalImage.src = imageUrl;
        modal.style.display = 'block';
    };

    // Close modal when clicking the close button
    document.querySelector('.close-modal').onclick = () => {
        document.getElementById('orderImageModal').style.display = 'none';
    };

    // Close modal when clicking outside of it
    window.onclick = (event) => {
        const modal = document.getElementById('orderImageModal');
        if (event.target === modal) {
            modal.style.display = 'none';
        }
    };

    // Function to populate orders table
    const populateOrdersTable = () => {
        const tableBody = document.getElementById('ordersTableBody');
        if (!tableBody) return;

        // Clear existing table content
        tableBody.innerHTML = '';

        ordersData.forEach(order => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${order.orderId}</td>
                <td>${order.orderDate}</td>
                <td>${order.customerName}</td>
                <td>${order.itemId}</td>
                <td>${order.itemName}</td>
                <td>${order.quantity}</td>
                <td>${order.price}</td>
                <td></td>
                <td></td>
            `;
            
            // Add sell type badge
            const sellTypeCell = row.children[7];
            sellTypeCell.appendChild(createSellTypeBadge(order.sellType));

            // Add view order button
            const actionsCell = row.children[8];
            actionsCell.appendChild(createViewOrderButton(order.image));
            
            tableBody.appendChild(row);
        });
    };

    // Example of how to add a new order
    window.addNewOrder = () => {
        const newOrder = {
            orderId: `ORD${String(ordersData.length + 1).padStart(3, '0')}`,
            orderDate: new Date().toISOString().split('T')[0],
            customerName: "New Customer",
            itemId: `ITEM${String(ordersData.length + 1).padStart(3, '0')}`,
            itemName: "New Item",
            quantity: 1,
            price: "$0",
            sellType: "direct",
            image: "https://images.unsplash.com/photo-1618160702438-9b02ab6515c9"
        };
        addOrder(newOrder);
    };

    // Initialize the table
    populateOrdersTable();
});