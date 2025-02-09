document.addEventListener('DOMContentLoaded', () => {
    // Initialize Feather icons
    feather.replace();

    // Initialize users data from localStorage or use default data
    let usersData = JSON.parse(localStorage.getItem('usersData')) || [
        {
            userId: "U001",
            username: "johndoe",
            password: "********",
            email: "john.doe@example.com",
            phone: "+1 234 567 8901",
            profilePath: "https://images.unsplash.com/photo-1485827404703-89b55fcc595e",
            dob: "1990-05-15",
            gender: "Male",
            bio: "Software developer with 5 years of experience",
            role: "admin",
            created_at: "2024-01-15"
        },
        {
            userId: "U002",
            username: "janedoe",
            password: "********",
            email: "jane.doe@example.com",
            phone: "+1 234 567 8902",
            profilePath: "https://images.unsplash.com/photo-1485827404703-89b55fcc595e",
            dob: "1992-08-22",
            gender: "Female",
            bio: "Digital marketing specialist",
            role: "user",
            created_at: "2024-01-20"
        },
        {
            userId: "U003",
            username: "mikesmith",
            password: "********",
            email: "mike.smith@example.com",
            phone: "+1 234 567 8903",
            profilePath: "https://images.unsplash.com/photo-1485827404703-89b55fcc595e",
            dob: "1988-12-10",
            gender: "Male",
            bio: "Product manager",
            role: "user",
            created_at: "2024-02-01"
        }
    ];

    // Function to save users data to localStorage
    const saveUsersData = () => {
        localStorage.setItem('usersData', JSON.stringify(usersData));
    };

    // Function to add new user
    const addUser = (newUser) => {
        usersData.push(newUser);
        saveUsersData();
        populateUsersTable(); // Refresh the table
    };

    // Function to create role badge
    const createRoleBadge = (role) => {
        const badge = document.createElement('span');
        badge.className = `role-badge ${role}`;
        const icon = feather.icons[
            role === 'admin' ? 'shield' : 'user'
        ].toSvg({ width: 16, height: 16 });
        badge.innerHTML = `${icon} ${role.charAt(0).toUpperCase() + role.slice(1)}`;
        return badge;
    };

    // Function to view profile image - make it globally accessible
    window.viewProfileImage = (imagePath, username) => {
        const modal = document.getElementById('userImageModal');
        const modalImage = document.getElementById('userImage');
        if (modal && modalImage) {
            modalImage.src = imagePath;
            modalImage.alt = `${username}'s profile`;
            modal.style.display = 'block';
        }
    };

    // Function to populate users table
    const populateUsersTable = () => {
        const tableBody = document.getElementById('usersTableBody');
        if (!tableBody) return;

        // Clear existing table content
        tableBody.innerHTML = '';

        usersData.forEach(user => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${user.userId}</td>
                <td>${user.username}</td>
                <td>${user.password}</td>
                <td>${user.email}</td>
                <td>${user.phone}</td>
                <td>
                    <button class="view-btn" onclick="viewProfileImage('${user.profilePath}', '${user.username}')">
                        ${feather.icons['eye'].toSvg({ width: 16, height: 16 })}
                    </button>
                </td>
                <td>${user.dob}</td>
                <td>${user.gender}</td>
                <td>${user.bio}</td>
                <td></td>
                <td>${user.created_at}</td>
            `;
            
            // Add role badge
            const roleCell = row.children[9];
            roleCell.appendChild(createRoleBadge(user.role));
            
            tableBody.appendChild(row);
        });
    };

    // Example of how to add a new user
    window.addNewUser = () => {
        const newUser = {
            userId: `U${String(usersData.length + 1).padStart(3, '0')}`,
            username: `user${usersData.length + 1}`,
            password: "********",
            email: `user${usersData.length + 1}@example.com`,
            phone: "+1 234 567 8900",
            profilePath: "profile_default.jpg",
            dob: "2000-01-01",
            gender: "Not specified",
            bio: "New user",
            role: "user",
            created_at: new Date().toISOString().split('T')[0]
        };
        addUser(newUser);
    };

    // Close modal when clicking the close button or outside the modal
    window.onclick = function(event) {
        const modal = document.getElementById('userImageModal');
        if (event.target === modal) {
            modal.style.display = 'none';
        }
    };

    document.querySelector('.close-modal')?.addEventListener('click', () => {
        const modal = document.getElementById('userImageModal');
        if (modal) modal.style.display = 'none';
    });

    // Initialize the table
    populateUsersTable();
});