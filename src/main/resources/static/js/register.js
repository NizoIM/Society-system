document.getElementById("registerForm")
.addEventListener("submit", function(e) {

    e.preventDefault();

    let password = document.getElementById("password").value;
    let confirmPassword = document.getElementById("confirmPassword").value;

    if (password !== confirmPassword) {
        alert("Passwords do not match!");
        return;
    }

    let data = {
        firstName: document.getElementById("firstName").value,
        lastName: document.getElementById("lastName").value,
        email: document.getElementById("email").value,
        phone: document.getElementById("phone").value,
        password: password,
        role: document.getElementById("role").value,
        enabled: true
    };

    fetch("${API_BASE}/api/auth/register", {

        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(data)

    })
    .then(response => response.text())
    .then(result => {

        alert(result);

        window.location.href = "login.html";
    })
    .catch(error => console.log(error));
});