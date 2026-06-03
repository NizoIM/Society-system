// ================= AUTH =================

const token = localStorage.getItem("token");
const email = localStorage.getItem("email");

if (!token || !email) {
    window.location.href = "/pages/login.html";
}

// ================= BASE =================

const BASE_URL = "https://society-kwgy.onrender.com/api/member";

// ================= ENDPOINTS (MATCH CONTROLLER EXACTLY) =================

const PROFILE_API = `${BASE_URL}/profile/${email}`;
const PAYMENT_API = `${BASE_URL}/payment/${email}`;
const PAYMENT_HISTORY_API = `${BASE_URL}/payments/${email}`;
const QUERY_API = `${BASE_URL}/query`;

// ================= HEADERS =================

function authHeaders(json = true) {
    const headers = {
        Authorization: `Bearer ${token}`
    };

    if (json) {
        headers["Content-Type"] = "application/json";
    }

    return headers;
}

// ================= LOGOUT =================

function logout() {
    localStorage.clear();
    window.location.href = "/pages/login.html";
}

// ================= PROFILE =================

async function loadProfile() {

    try {
        const res = await fetch(PROFILE_API, {
            method: "GET",
            headers: authHeaders(false)
        });

        if (!res.ok) throw new Error("Profile failed");

        const user = await res.json();

        document.getElementById("profile").innerHTML = `
            <h2>${user.firstName} ${user.lastName}</h2>
            <p>Email: ${user.email}</p>
            <p>Phone: ${user.phone}</p>
            <p>Role: ${user.role}</p>
        `;

    } catch (err) {
        console.error("Profile error", err);
    }
}

// ================= SEND QUERY =================

async function sendQuery() {

    const subject = document.getElementById("subject").value;
    const message = document.getElementById("message").value;

    if (!subject || !message) {
        alert("Fill all fields");
        return;
    }

    try {

        const res = await fetch(QUERY_API, {
            method: "POST",
            headers: authHeaders(),
            body: JSON.stringify({
                email,
                subject,
                message
            })
        });

        if (!res.ok) throw new Error("Query failed");

        alert("Query sent successfully");

        loadQueries();

    } catch (err) {
        console.error(err);
    }
}

// ================= LOAD QUERIES =================

async function loadQueries() {

    try {

        const res = await fetch(`${BASE_URL}/query/${email}`, {
            method: "GET",
            headers: authHeaders(false)
        });

        if (!res.ok) throw new Error("Queries failed");

        const data = await res.json();

        const table = document.querySelector("#myTable tbody");

        table.innerHTML = "";

        if (!data.length) {
            table.innerHTML = `<tr><td colspan="5">No queries found</td></tr>`;
            return;
        }

        data.forEach(q => {
            table.innerHTML += `
                <tr>
                    <td>${q.id}</td>
                    <td>${q.subject}</td>
                    <td>${q.message}</td>
                    <td>${q.status}</td>
                    <td>${q.response || "-"}</td>
                </tr>
            `;
        });

    } catch (err) {
        console.error(err);
    }
}

// ================= UPLOAD PAYMENT =================

async function uploadPayment() {

    const file = document.getElementById("proofFile").files[0];
    const amount = document.getElementById("amount").value;

    if (!file || !amount) {
        alert("Select file + amount");
        return;
    }

    const formData = new FormData();
    formData.append("file", file);
    formData.append("amount", amount);

    try {

        const res = await fetch(PAYMENT_API, {
            method: "POST",
            headers: {
                Authorization: `Bearer ${token}`
            },
            body: formData
        });

        if (!res.ok) throw new Error("Upload failed");

        alert("Payment uploaded");

        loadPaymentHistory();

    } catch (err) {
        console.error(err);
    }
}

// ================= PAYMENT HISTORY =================

async function loadPaymentHistory() {

    try {

        const res = await fetch(PAYMENT_HISTORY_API, {
            method: "GET",
            headers: authHeaders(false)
        });

        if (!res.ok) throw new Error("Payment history failed");

        const data = await res.json();

        const table = document.querySelector("#paymentTable tbody");

        table.innerHTML = "";

        if (!data.length) {
            table.innerHTML = `<tr><td colspan="5">No payments found</td></tr>`;
            return;
        }

        data.forEach(p => {
            table.innerHTML += `
                <tr>
                    <td>${p.id}</td>
                    <td>${p.paymentMonth || "-"}</td>
                    <td>R${p.amount}</td>
                    <td>${p.status}</td>
                    <td>${p.paymentDate || "-"}</td>
                </tr>
            `;
        });

    } catch (err) {
        console.error(err);
    }
}

// ================= INIT =================

document.addEventListener("DOMContentLoaded", () => {
    loadProfile();
    loadQueries();
    loadPaymentHistory();
});

// ================= GLOBALS =================

window.sendQuery = sendQuery;
window.uploadPayment = uploadPayment;
window.logout = logout;