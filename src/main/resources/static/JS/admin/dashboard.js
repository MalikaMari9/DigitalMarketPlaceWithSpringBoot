document.addEventListener("DOMContentLoaded", async () => {

    /** ✅ Fetch Category Data (For Pie Chart) **/
    async function fetchCategoryData() {
        try {
            const response = await fetch("/admin/viewDashboard/Catdata");
            return await response.json();
        } catch (error) {
            console.error("❌ Error fetching category data:", error);
            return [];
        }
    }

    /** ✅ Render Category Distribution Pie Chart **/
    async function renderCategoryChart() {
        const categoryData = await fetchCategoryData();

        if (categoryData.length === 0) {
            console.warn("⚠️ No category data available.");
            return;
        }

        const ctx = document.querySelector("#categoryChart canvas").getContext("2d");

        new Chart(ctx, {
            type: "doughnut",
            data: {
                labels: categoryData.map(cat => cat.name),
                datasets: [{
                    data: categoryData.map(cat => cat.value),
                    backgroundColor: ["#8B5CF6", "#10B981", "#F59E0B", "#0EA5E9", "#EC4899", "#6366F1", "#EF4444"],
                    borderColor: "white",
                    borderWidth: 2
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                cutout: "60%",
                plugins: {
                    legend: { display: false },
                    tooltip: {
                        callbacks: {
                            label: function(context) {
                                const label = context.label || "";
                                const value = context.formattedValue || "";
                                const count = categoryData[context.dataIndex].count;
                                return `${label}: ${value}% (${count} items)`;
                            }
                        }
                    }
                }
            }
        });

        // ✅ Update Category List
        const categoryList = document.getElementById("categoryList");
        categoryList.innerHTML = "";

        categoryData.forEach(category => {
            const item = document.createElement("div");
            item.className = "category-item";
            item.innerHTML = `
                <div class="category-color" style="background-color: #10B981"></div>
                <div class="category-info">
                    <span class="category-name">${category.name}</span>
                    <span class="category-count">${category.count} items</span>
                </div>
            `;
            categoryList.appendChild(item);
        });
    }

    /** ✅ Fetch Best Seller Data **/
    async function fetchBestSeller() {
        try {
            const response = await fetch("/admin/bestSeller");
            return await response.json();
        } catch (error) {
            console.error("❌ Error fetching best seller:", error);
            return { userID: null, sellerName: "No Best Seller", totalSales: 0, salesTarget: "N/A" };
        }
    }

    /** ✅ Render Best Seller Card **/
    async function renderBestSeller() {
        const data = await fetchBestSeller();
        document.querySelector(".congrats-header h3 span").textContent = data.sellerName;
        document.querySelector(".congrats-stats .amount").textContent = `$${data.totalSales}`;
        document.querySelector(".congrats-stats .target").textContent = data.salesTarget;

        const detailsButton = document.querySelector(".view-details");
        if (data.userID) {
            detailsButton.style.display = "block"; // Show button if seller exists
            detailsButton.setAttribute("onclick", `window.location.href='/admin/sellerDetails?userID=${data.userID}'`);
        } else {
            detailsButton.style.display = "none"; // Hide button if no seller
        }
    }

    /** ✅ Fetch Overview Chart Data **/
    async function fetchOverviewData() {
        try {
            const response = await fetch("/admin/viewDashboard/data");
            return await response.json();
        } catch (error) {
            console.error("❌ Error fetching dashboard data:", error);
            return [];
        }
    }

    /** ✅ Render Sales & Views Overview Chart **/
	async function renderOverviewChart() {
	    const data = await fetchOverviewData();

	    if (data.length === 0) {
	        console.warn("⚠️ No data available.");
	        return;
	    }

	    // ✅ Check if the canvas exists before trying to access it
	    const chartCanvas = document.querySelector("#overviewChart canvas");
	    if (!chartCanvas) {
	        console.error("❌ Overview chart canvas not found! Check your HTML structure.");
	        return;
	    }

	    const ctx = chartCanvas.getContext("2d");

	    new Chart(ctx, {
	        type: "bar",
	        data: {
	            labels: data.map(d => d.month),
	            datasets: [
	                {
	                    label: "Sales ($)",
	                    data: data.map(d => d.sales),
	                    backgroundColor: "#10B981",
	                    borderColor: "#10B981",
	                    borderWidth: 1
	                },
	                {
	                    label: "Views",
	                    data: data.map(d => d.views),
	                    backgroundColor: "#8B5CF6",
	                    borderColor: "#8B5CF6",
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
	                    position: "top",
	                    labels: {
	                        color: "#718096"
	                    }
	                }
	            },
	            scales: {
	                y: {
	                    beginAtZero: true,
	                    grid: { color: "#2D3748" },
	                    ticks: { color: "#718096" }
	                },
	                x: {
	                    grid: { color: "#2D3748" },
	                    ticks: { color: "#718096" }
	                }
	            }
	        }
	    });
	}

    /** ✅ Fetch Progress Data **/
    async function fetchProgressData() {
        try {
            const response = await fetch("/admin/progressData");
            return await response.json();
        } catch (error) {
            console.error("❌ Error fetching progress data:", error);
            return { salesProgress: 0, productsProgress: 0, incomeProgress: 0 };
        }
    }

    /** ✅ Update Progress Circles **/
    async function updateProgressCircles() {
        const data = await fetchProgressData();

        document.querySelectorAll(".progress-circle").forEach(circle => {
            let progressType = circle.closest('.card').querySelector("h4").textContent;
            let progressValue = 0;

            if (progressType.includes("Sales")) {
                progressValue = data.salesProgress;
            } else if (progressType.includes("Products")) {
                progressValue = data.productsProgress;
            } else if (progressType.includes("Income")) {
                progressValue = data.incomeProgress;
            }

            // Update circle animation
            const progressBar = circle.querySelector('.progress-bar');
            const progressText = circle.querySelector('.progress-value');
            const circumference = 2 * Math.PI * 44; // 2πr where r=44
            const offset = circumference - (progressValue / 100) * circumference;

            progressBar.style.strokeDasharray = `${circumference} ${circumference}`;
            progressBar.style.strokeDashoffset = offset;
            progressText.textContent = `${Math.round(progressValue)}%`;
        });
    }

    /** ✅ Initialize Icons (Feather & Lucide) **/
    lucide.createIcons();

    /** ✅ Render Everything on Page Load **/
    renderCategoryChart();
    renderBestSeller();
    renderOverviewChart();
    updateProgressCircles();
});
