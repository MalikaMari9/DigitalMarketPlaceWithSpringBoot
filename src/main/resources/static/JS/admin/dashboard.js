document.addEventListener("DOMContentLoaded", async () => {
    async function fetchData() {
        try {
            const response = await fetch("/admin/viewDashboard/data");
            return await response.json();
        } catch (error) {
            console.error("❌ Error fetching dashboard data:", error);
            return [];
        }
    }

    async function renderOverviewChart() {
        const data = await fetchData();

        if (data.length === 0) {
            console.warn("⚠️ No data available.");
            return;
        }

        const labels = data.map(d => d.month);
        const salesData = data.map(d => d.sales);
        const viewsData = data.map(d => d.views);

        const ctx = document.querySelector("#overviewChart canvas").getContext("2d");

        new Chart(ctx, {
            type: "bar",
            data: {
                labels: labels,
                datasets: [
                    {
                        label: "Sales ($)",
                        data: salesData,
                        backgroundColor: "#10B981",
                        borderColor: "#10B981",
                        borderWidth: 1
                    },
                    {
                        label: "Views",
                        data: viewsData,
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
	
	async function fetchBestSeller() {
	        try {
	            const response = await fetch("/admin/bestSeller");
	            return await response.json();
	        } catch (error) {
	            console.error("❌ Error fetching best seller:", error);
	            return { userID: null, sellerName: "No Best Seller", totalSales: 0, salesTarget: "N/A" };
	        }
	    }

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
		async function fetchProgressData() {
		       try {
		           const response = await fetch("/admin/progressData");
		           return await response.json();
		       } catch (error) {
		           console.error("❌ Error fetching progress data:", error);
		           return { salesProgress: 0, productsProgress: 0, incomeProgress: 0 };
		       }
		   }

		   async function updateProgressCircles() {
		       const data = await fetchProgressData();

		       // Update progress values
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

		   updateProgressCircles();

	    renderBestSeller();

    renderOverviewChart();
});
