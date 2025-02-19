
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
    };

    // Initialize charts
    initializeCharts();
});