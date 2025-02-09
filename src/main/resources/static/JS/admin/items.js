document.addEventListener('DOMContentLoaded', () => {
    // Initialize Feather icons
    feather.replace();

    // Initialize items data from localStorage or use default data
    let itemsData = JSON.parse(localStorage.getItem('itemsData')) || [
        {
            itemId: "I001",
            itemName: "Vintage Watch",
            descrip: "A classic timepiece from the 1950s",
            price: "$1,500",
            quality: "Excellent",
            sellType: "Auction",
            created_at: "2024-02-15",
            stat: "available",
            cond: "Used - Like New",
            sellerId: "S001",
            catId: "C001",
            image: "https://images.unsplash.com/photo-1486312338219-ce68d2c6f44d"
        },
        {
            itemId: "I002",
            itemName: "Diamond Ring",
            descrip: "18K white gold ring with 1 carat diamond",
            price: "$3,000",
            quality: "Premium",
            sellType: "Fixed Price",
            created_at: "2024-02-16",
            stat: "pending",
            cond: "New",
            sellerId: "S002",
            catId: "C002",
            image: "https://images.unsplash.com/photo-1485827404703-89b55fcc595e"
        },
        {
            itemId: "I003",
            itemName: "Antique Vase",
            descrip: "Chinese porcelain vase from Ming Dynasty",
            price: "$5,000",
            quality: "Good",
            sellType: "Auction",
            created_at: "2024-02-17",
            stat: "sold",
            cond: "Used - Good",
            sellerId: "S003",
            catId: "C003",
            image: "https://images.unsplash.com/photo-1581091226825-a6a2a5aee158"
        }
    ];

    // Function to save items data to localStorage
    const saveItemsData = () => {
        localStorage.setItem('itemsData', JSON.stringify(itemsData));
    };

    // Function to add new item
    const addItem = (newItem) => {
        itemsData.push(newItem);
        saveItemsData();
        populateItemsTable(); // Refresh the table
    };

    // Function to create status badge
    const createStatusBadge = (status) => {
        const badge = document.createElement('span');
        badge.className = `status-badge ${status}`;
        const icon = feather.icons[
            status === 'available' ? 'check-circle' : 
            status === 'pending' ? 'clock' : 'x-circle'
        ].toSvg({ width: 16, height: 16 });
        badge.innerHTML = `${icon} ${status.charAt(0).toUpperCase() + status.slice(1)}`;
        return badge;
    };

    // Function to create view item button
    const createViewItemButton = (image) => {
        const button = document.createElement('button');
        button.className = 'view-item-btn';
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

    // Function to populate items table
    const populateItemsTable = () => {
        const tableBody = document.getElementById('itemsTableBody');
        if (!tableBody) return;

        // Clear existing table content
        tableBody.innerHTML = '';

        itemsData.forEach(item => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${item.itemId}</td>
                <td>${item.itemName}</td>
                <td>${item.descrip}</td>
                <td>${item.price}</td>
                <td>${item.quality}</td>
                <td>${item.sellType}</td>
                <td>${item.created_at}</td>
                <td></td>
                <td>${item.cond}</td>
                <td>${item.sellerId}</td>
                <td>${item.catId}</td>
                <td></td>
            `;
            
            // Add status badge
            const statusCell = row.children[7];
            statusCell.appendChild(createStatusBadge(item.stat));

            // Add view item button
            const actionsCell = row.children[11];
            actionsCell.appendChild(createViewItemButton(item.image));
            
            tableBody.appendChild(row);
        });
    };

    // Example of how to add a new item
    window.addNewItem = () => {
        const newItem = {
            itemId: `I${String(itemsData.length + 1).padStart(3, '0')}`,
            itemName: "New Item",
            descrip: "Description for new item",
            price: "$0",
            quality: "New",
            sellType: "Fixed Price",
            created_at: new Date().toISOString().split('T')[0],
            stat: "pending",
            cond: "New",
            sellerId: `S${String(itemsData.length + 1).padStart(3, '0')}`,
            catId: `C${String(itemsData.length + 1).padStart(3, '0')}`,
            image: "https://images.unsplash.com/photo-1486312338219-ce68d2c6f44d"
        };
        addItem(newItem);
    };

    // Initialize the table
    populateItemsTable();
});