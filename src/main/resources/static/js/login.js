document.getElementById("loginForm")
.addEventListener("submit", async function (e) {

    e.preventDefault();

    const email = document.getElementById("email").value;
    const password = document.getElementById("password").value;

    try {

        const response = await fetch(
            "https://society-kwgy.onrender.com/api/auth/login",
            {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    email,
                    password
                })
            }
        );

        // 🔥 SAFE RESPONSE HANDLING (IMPORTANT FOR RENDER)
        let data;

        const contentType = response.headers.get("content-type");

        if (contentType && contentType.includes("application/json")) {
            data = await response.json();
        } else {
            const text = await response.text();
            throw new Error(text || "Invalid server response");
        }

        // ❌ LOGIN FAILED
        if (!response.ok) {
            alert(data.message || "Login failed");
            return;
        }

        // ❌ NO TOKEN
        if (!data.token) {
            alert("Invalid login response (missing token)");
            return;
        }

        // ✅ STORE SESSION
        localStorage.setItem("token", data.token);
        localStorage.setItem("role", data.role || "");
        localStorage.setItem("email", data.email || "");

        alert("Login Successful");

        // ✅ SAFE ROLE HANDLING
        const role = (data.role || "").toUpperCase();

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

        console.error("LOGIN ERROR:", error);
        alert(error.message || "Server error. Please try again.");
    }
});