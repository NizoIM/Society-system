// ================= AUTH =================

const token = localStorage.getItem("token");

if (!token) {
    window.location.href = "/pages/login.html";
}

// ================= API =================

//const BASE_URL = "http://localhost:8080/api/staff";
const API_URL =
    "https://society-kwgy.onrender.com/api/auth/login";

const QUERY_API = `${BASE_URL}/queries`;
const PROFILE_API = `${BASE_URL}/profile`;
const PAYMENT_API = `${BASE_URL}/payments/upload`;
const PAYMENT_HISTORY_API = `${BASE_URL}/payments/history`;

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

// ================= HANDLE RESPONSE =================

async function handleResponse(response) {

    if (response.status === 401 || response.status === 403) {
        alert("Session expired");
        logout();
        throw new Error("Unauthorized");
    }

    if (!response.ok) {

        let message = "Request failed";

        try {
            message = await response.text();
        } catch (e) {
            console.error(e);
        }

        throw new Error(message);
    }

    const contentType = response.headers.get("content-type");

    if (contentType && contentType.includes("application/json")) {
        return await response.json();
    }

    return await response.text();
}

// ================= LOAD PROFILE =================

async function loadProfile() {

    try {

        const response = await fetch(PROFILE_API, {
            method: "GET",
            headers: authHeaders(false)
        });

        const user = await handleResponse(response);

        document.getElementById("profile").innerHTML = `
            <div class="profile-card">

                <h2>
                    ${escapeHtml(user.firstName || "")}
                    ${escapeHtml(user.lastName || "")}
                </h2>

                <p><strong>Email:</strong> ${escapeHtml(user.email || "N/A")}</p>
                <p><strong>Phone:</strong> ${escapeHtml(user.phone || "N/A")}</p>
                <p><strong>Role:</strong> ${escapeHtml(user.role || "STAFF")}</p>

                <p>
                    <strong>Status:</strong>
                    <span class="${user.enabled ? "active" : "inactive"}">
                        ${user.enabled ? "ACTIVE" : "DISABLED"}
                    </span>
                </p>

            </div>
        `;

    } catch (error) {
        console.error(error);
        alert(error.message);
    }
}

// ================= UPLOAD PAYMENT =================

async function uploadPayment() {

    const file = document.getElementById("proofFile")?.files[0];
    const amount = document.getElementById("amount")?.value;

    if (!file || !amount) {
        alert("Enter amount and choose file");
        return;
    }

    const formData = new FormData();
    formData.append("file", file);
    formData.append("amount", amount);

    try {

        const response = await fetch(PAYMENT_API, {
            method: "POST",
            headers: {
                Authorization: `Bearer ${token}`
            },
            body: formData
        });

        await handleResponse(response);

        alert("Payment uploaded successfully");

        document.getElementById("proofFile").value = "";
        document.getElementById("amount").value = "";

        loadPaymentHistory();

    } catch (error) {
        console.error(error);
        alert(error.message || "Failed to upload payment");
    }
}

// ================= LOAD PAYMENT HISTORY =================

async function loadPaymentHistory() {

    try {

        const response = await fetch(PAYMENT_HISTORY_API, {
            method: "GET",
            headers: authHeaders(false)
        });

        const payments = await handleResponse(response);

        const table = document.querySelector("#paymentTable tbody");

        if (!table) return;

        table.innerHTML = "";

        if (!payments || payments.length === 0) {
            table.innerHTML = `
                <tr>
                    <td colspan="6">No payments uploaded</td>
                </tr>
            `;
            return;
        }

        payments.forEach(payment => {

            table.innerHTML += `
                <tr>
                    <td>${payment.id}</td>
                    <td>${payment.paymentMonth ?? "-"}</td>
                    <td>R${payment.amount ?? "0.00"}</td>
                    <td>${payment.status ?? "PENDING"}</td>
                    <td>${payment.paymentDate ?? "-"}</td>
                    <td>${payment.originalFileName ?? "-"}</td>
                </tr>
            `;
        });

    } catch (error) {
        console.error(error);
        alert("Failed to load payments");
    }
}


// ================= LOAD QUERIES =================

async function loadQueries() {

    try {

        const response = await fetch(QUERY_API, {
            method: "GET",
            headers: authHeaders(false)
        });

        const data = await handleResponse(response);

        const table = document.querySelector("#queryTable tbody");

        if (!table) return;

        table.innerHTML = "";

        if (!data || data.length === 0) {
            table.innerHTML = `
                <tr>
                    <td colspan="6">No queries found</td>
                </tr>
            `;
            return;
        }

        data.forEach(q => {

            table.innerHTML += `
                <tr>
                    <td>${q.id}</td>
                    <td>${escapeHtml(q.email || "-")}</td>
                    <td>${escapeHtml(q.subject || "")}</td>
                    <td>${escapeHtml(q.message || "")}</td>
                    <td>${escapeHtml(q.status || "PENDING")}</td>

                    <td>
                        <button class="edit-btn"
                            onclick="respondToQuery(${q.id})">
                            Respond
                        </button>
                    </td>
                </tr>
            `;
        });

    } catch (error) {
        console.error(error);
        alert(error.message);
    }
}
// ================= RESPOND =================

async function respondToQuery(id) {

    const responseMessage = prompt("Enter response");

    if (!responseMessage) return;

    try {

        const response = await fetch(`${QUERY_API}/${id}`, {
            method: "PUT",
            headers: authHeaders(),
            body: JSON.stringify({
                status: "RESOLVED",
                response: responseMessage
            })
        });

        await handleResponse(response);

        alert("Query responded successfully");

        loadQueries();

    } catch (error) {
        console.error(error);
        alert(error.message);
    }
}

// ================= SEARCH =================

function searchQueries() {

    const text = document.getElementById("search")?.value.toLowerCase();

    document.querySelectorAll("#queryTable tbody tr").forEach(row => {

        const match = row.innerText.toLowerCase().includes(text);

        row.style.display = match ? "" : "none";
    });
}

// ================= HELPERS =================

function escapeHtml(value) {

    return String(value ?? "")
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}

// ================= INIT =================

document.addEventListener("DOMContentLoaded", () => {

    loadProfile();
    loadQueries();
    loadPaymentHistory();
});