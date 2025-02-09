document.addEventListener('DOMContentLoaded', () => {
    // Initialize Feather icons
    feather.replace();

    // Initialize auctions data from localStorage or use default data
    let auctionsData = JSON.parse(localStorage.getItem('auctionsData')) || [
        {
            auctionId: "A001",
            incrementAmount: "$50",
            createdAt: "2024-02-15",
            stat: "active",
            itemId: "I001",
            startPrice: "$1,000",
            endTime: "2024-03-15 15:00",
            auctionTblcol: "Regular",
            image: "https://images.unsplash.com/photo-1486312338219-ce68d2c6f44d"
        },
        {
            auctionId: "A002",
            incrementAmount: "$100",
            createdAt: "2024-02-16",
            stat: "upcoming",
            itemId: "I002",
            startPrice: "$2,500",
            endTime: "2024-03-20 18:00",
            auctionTblcol: "Featured",
            image: "https://images.unsplash.com/photo-1485827404703-89b55fcc595e"
        },
        {
            auctionId: "A003",
            incrementAmount: "$25",
            createdAt: "2024-02-10",
            stat: "ended",
            itemId: "I003",
            startPrice: "$500",
            endTime: "2024-02-20 12:00",
            auctionTblcol: "Regular",
            image: "https://images.unsplash.com/photo-1581091226825-a6a2a5aee158"
        }
    ];

    // Function to save auctions data to localStorage
    const saveAuctionsData = () => {
        localStorage.setItem('auctionsData', JSON.stringify(auctionsData));
    };

    // Function to add new auction
    const addAuction = (newAuction) => {
        auctionsData.push(newAuction);
        saveAuctionsData();
        populateAuctionsTable(); // Refresh the table
    };

    // Function to create status badge
    const createStatusBadge = (status) => {
        const badge = document.createElement('span');
        badge.className = `status-badge ${status}`;
        const icon = feather.icons[
            status === 'active' ? 'check-circle' : 
            status === 'upcoming' ? 'clock' : 'x-circle'
        ].toSvg({ width: 16, height: 16 });
        badge.innerHTML = `${icon} ${status.charAt(0).toUpperCase() + status.slice(1)}`;
        return badge;
    };

    // Function to create view auction button
    const createViewAuctionButton = (image) => {
        const button = document.createElement('button');
        button.className = 'view-auction-btn';
        button.innerHTML = feather.icons['eye'].toSvg({ width: 16, height: 16 });
        button.onclick = () => showAuctionImage(image);
        return button;
    };

    // Function to show auction image modal
    const showAuctionImage = (imageUrl) => {
        const modal = document.getElementById('auctionImageModal');
        const modalImage = document.getElementById('auctionImage');
        modalImage.src = imageUrl;
        modal.style.display = 'block';
    };

    // Close modal when clicking the close button
    document.querySelector('.close-modal').onclick = () => {
        document.getElementById('auctionImageModal').style.display = 'none';
    };

    // Close modal when clicking outside of it
    window.onclick = (event) => {
        const modal = document.getElementById('auctionImageModal');
        if (event.target === modal) {
            modal.style.display = 'none';
        }
    };

    // Function to populate auctions table
    const populateAuctionsTable = () => {
        const tableBody = document.getElementById('auctionsTableBody');
        if (!tableBody) return;

        // Clear existing table content
        tableBody.innerHTML = '';

        auctionsData.forEach(auction => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${auction.auctionId}</td>
                <td>${auction.incrementAmount}</td>
                <td>${auction.createdAt}</td>
                <td></td>
                <td>${auction.itemId}</td>
                <td>${auction.startPrice}</td>
                <td>${auction.endTime}</td>
                <td>${auction.auctionTblcol}</td>
                <td></td>
            `;
            
            // Add status badge
            const statusCell = row.children[3];
            statusCell.appendChild(createStatusBadge(auction.stat));

            // Add view auction button
            const actionsCell = row.children[8];
            actionsCell.appendChild(createViewAuctionButton(auction.image));
            
            tableBody.appendChild(row);
        });
    };

    // Example of how to add a new auction
    window.addNewAuction = () => {
        const newAuction = {
            auctionId: `A${String(auctionsData.length + 1).padStart(3, '0')}`,
            incrementAmount: "$50",
            createdAt: new Date().toISOString().split('T')[0],
            stat: "upcoming",
            itemId: `I${String(auctionsData.length + 1).padStart(3, '0')}`,
            startPrice: "$1,000",
            endTime: "2024-03-30 15:00",
            auctionTblcol: "Regular",
            image: "https://images.unsplash.com/photo-1486312338219-ce68d2c6f44d"
        };
        addAuction(newAuction);
    };

    // Initialize the table
    populateAuctionsTable();
});