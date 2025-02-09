// Initialize Feather icons
document.addEventListener('DOMContentLoaded', () => {
    // Initialize Feather icons
    feather.replace();

    // Chart data
    const data = [
        { name: "Jan", sales: 4000, views: 2400, orders: 3200 },
        { name: "Feb", sales: 3000, views: 1398, orders: 2800 },
        { name: "Mar", sales: 2000, views: 9800, orders: 3400 },
        { name: "Apr", sales: 2780, views: 3908, orders: 2900 },
        { name: "May", sales: 1890, views: 4800, orders: 3100 },
        { name: "Jun", sales: 2390, views: 3800, orders: 3500 },
    ];

    // Auction data
    const auctionData = [
        { month: "Jan", bidItems: 45, winningBids: 32 },
        { month: "Feb", bidItems: 55, winningBids: 41 },
        { month: "Mar", bidItems: 65, winningBids: 48 },
        { month: "Apr", bidItems: 75, winningBids: 58 },
        { month: "May", bidItems: 85, winningBids: 62 },
        { month: "Jun", bidItems: 95, winningBids: 78 },
    ];

    // Initialize progress circles
    const initializeProgressCircles = () => {
        const circles = document.querySelectorAll('.progress-circle');
        const circumference = 2 * Math.PI * 44; // 2πr where r=44

        circles.forEach(circle => {
            const progress = circle.dataset.progress;
            const progressBar = circle.querySelector('.progress-bar');
            const offset = circumference - (progress / 100) * circumference;
            
            progressBar.style.strokeDasharray = `${circumference} ${circumference}`;
            progressBar.style.strokeDashoffset = offset;
            
            // Set color based on the parent's icon color
            const parentCard = circle.closest('.card');
            const icon = parentCard.querySelector('.progress-icon');
            if (icon.classList.contains('blue')) {
                progressBar.style.stroke = '#0EA5E9';
            } else if (icon.classList.contains('green')) {
                progressBar.style.stroke = '#10B981';
            } else if (icon.classList.contains('purple')) {
                progressBar.style.stroke = '#8B5CF6';
            }
        });
    };

    // Initialize all charts using Chart.js
    const initializeCharts = () => {
        // Orders Chart
        new Chart(document.querySelector('#ordersChart canvas'), {
            type: 'line',
            data: {
                labels: data.map(d => d.name),
                datasets: [{
                    data: data.map(d => d.orders),
                    backgroundColor: '#0EA5E9',
                    borderColor: '#0EA5E9',
                    borderWidth: 2,
                    tension: 0.4,
                    fill: false
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        display: false
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        display: false
                    },
                    x: {
                        display: false
                    }
                }
            }
        });
        
        // Sales Chart
        new Chart(document.querySelector('#salesChart canvas'), {
            type: 'line',
            data: {
                labels: data.map(d => d.name),
                datasets: [{
                    data: data.map(d => d.sales),
                    backgroundColor: '#10B981',
                    borderColor: '#10B981',
                    borderWidth: 2,
                    tension: 0.4,
                    fill: false
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        display: false
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        display: false
                    },
                    x: {
                        display: false
                    }
                }
            }
        });
        
        // Visits Chart
        new Chart(document.querySelector('#visitsChart canvas'), {
            type: 'line',
            data: {
                labels: data.map(d => d.name),
                datasets: [{
                    data: data.map(d => d.views),
                    backgroundColor: '#8B5CF6',
                    borderColor: '#8B5CF6',
                    borderWidth: 2,
                    tension: 0.4,
                    fill: false
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        display: false
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        display: false
                    },
                    x: {
                        display: false
                    }
                }
            }
        });
        
        // Overview Chart
        new Chart(document.querySelector('#overviewChart canvas'), {
            type: 'bar',
            data: {
                labels: data.map(d => d.name),
                datasets: [
                    {
                        label: 'Sales',
                        data: data.map(d => d.sales),
                        backgroundColor: '#10B981',
                        borderColor: '#10B981',
                        borderWidth: 1
                    },
                    {
                        label: 'Views',
                        data: data.map(d => d.views),
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

        // Auction Overview Chart
        new Chart(document.querySelector('#auctionChart canvas'), {
            type: 'line',
            data: {
                labels: auctionData.map(d => d.month),
                datasets: [
                    {
                        label: 'Bid Items',
                        data: auctionData.map(d => d.bidItems),
                        backgroundColor: '#9b87f5',
                        borderColor: '#9b87f5',
                        borderWidth: 2,
                        tension: 0.4,
                        fill: false
                    },
                    {
                        label: 'Winning Bids',
                        data: auctionData.map(d => d.winningBids),
                        backgroundColor: '#D946EF',
                        borderColor: '#D946EF',
                        borderWidth: 2,
                        tension: 0.4,
                        fill: false
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
    };

    // Initialize everything
    initializeProgressCircles();
    initializeCharts();
});