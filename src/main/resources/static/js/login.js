document.getElementById("loginForm").addEventListener("submit", async function (e) {
    e.preventDefault();

    const email = document.getElementById("email").value;
    const password = document.getElementById("password").value;

    try {

        const response = await fetch("https://society-kwgy.onrender.com/api/auth/login", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                email,
                password
            })
        });

        const data = await response.json();

        // ❌ LOGIN FAILED
        if (!response.ok) {
            alert(data.message || "Login failed");
            return;
        }

        // ❌ SAFETY CHECK
        if (!data.token) {
            alert("Invalid login response (no token)");
            return;
        }

        // ✅ STORE TOKEN
        localStorage.setItem("token", data.token);
        localStorage.setItem("role", data.role);
        localStorage.setItem("email", data.email);

        alert("Login Successful");

        // ✅ REDIRECT BY ROLE
        const role = data.role?.toUpperCase();

        if (role === "ADMIN") {
            window.location.href = "/pages/admin-dashboard.html";
        }
        else if (role === "STAFF") {
            window.location.href = "/pages/staff-dashboard.html";
        }
        else {
            window.location.href = "/pages/member-dashboard.html";
        }

    } catch (error) {
        console.error(error);
        alert("Server error. Please try again.");
    }
});