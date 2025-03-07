document.addEventListener('DOMContentLoaded', async () => {
    feather.replace();

    // Fetch data from the backend
    async function fetchData() {
        try {
            const response = await fetch("/sellerDashboard/data");
            return await response.json();
        } catch (error) {
            console.error("❌ Error fetching seller dashboard data:", error);
            return [];
        }
    }

    // Render Bar Chart
    async function renderOverviewChart() {
        const data = await fetchData();

        if (data.length === 0) {
            console.warn("⚠️ No data available for the seller.");
            return;
        }

        const labels = data.map(d => d.month);
        const salesData = data.map(d => d.sales);
        const viewsData = data.map(d => d.views);

        const ctx = document.querySelector("#overviewChart canvas").getContext("2d");

        new Chart(ctx, {
            type: 'bar',
            data: {
                labels: labels,
                datasets: [
                    {
                        label: 'Sales ($)',
                        data: salesData,
                        backgroundColor: '#10B981',
                        borderColor: '#10B981',
                        borderWidth: 1
                    },
                    {
                        label: 'Views',
                        data: viewsData,
                        backgroundColor: '#8B5CF6',
                        borderColor: '#8B5CF6',
                        borderWidth: 1
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        display: true,
                        position: 'top',
                        labels: {
                            color: '#718096'
                        }
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        grid: {
                            color: '#2D3748'
                        },
                        ticks: {
                            color: '#718096'
                        }
                    },
                    x: {
                        grid: {
                            color: '#2D3748'
                        },
                        ticks: {
                            color: '#718096'
                        }
                    }
                }
            }
        });
    }

    // Initialize Overview Chart
    renderOverviewChart();
});
document.addEventListener('DOMContentLoaded', async () => {
	async function fetchTopProducts() {
	    try {
	        const response = await fetch("/sellerDashboard/topProducts");
	        const data = await response.json();
	        console.log("📢 Received API Data:", data);
	        return data;
	    } catch (error) {
	        console.error("❌ Error fetching top products:", error);
	        return { type: "unknown", products: [] };
	    }
	}
	async function renderTopProducts() {
	    const data = await fetchTopProducts();
	    const container = document.querySelector(".top-selling-list");
	    const titleElement = document.querySelector(".card-title"); // Select the title element
	    container.innerHTML = ""; // Clear previous content

	    if (data.error) {
	        container.innerHTML = `<p>${data.error}</p>`;
	        return;
	    }

		console.log(titleElement);
	    // ✅ Update the header dynamically
	    titleElement.textContent = data.type === "B2C" ? "Top Selling Products" : "Recently Sold Products";

	    if (data.products.length === 0) {
	        container.innerHTML = `<p>No data available.</p>`;
	        return;
	    }

	    data.products.forEach(product => {
	        const productItem = document.createElement("div");
	        productItem.classList.add("product-item");

	        productItem.innerHTML = `
	            <img src="${product.thumbnail}" alt="${product.itemName}">
	            <div class="product-info">
	                <h4>${product.itemName}</h4>
	                <div class="product-stats">
	                    ${
	                        data.type === "B2C"
	                            ? `<span class="sales">${product.totalSales} Sales</span>
	                               <span class="stock">${product.stock} in Stock</span>`
	                            : `<span class="sales">Sold on: ${new Date(product.soldDate).toLocaleDateString()}</span>
	                               <span class="sales">Quantity Sold: ${product.quantity}</span>`
	                    }
	                </div>
	            </div>
	        `;

	        container.appendChild(productItem);
	    });
	}

    renderTopProducts();
});
