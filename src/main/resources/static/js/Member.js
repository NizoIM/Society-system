
// ================= AUTH =================

const token = localStorage.getItem("token");
const email = localStorage.getItem("email");

if (!token || !email) {
    window.location.href = "/pages/login.html";
}

// ================= BASE URL =================

const BASE_URL = "https://society-kwgy.onrender.com/api/member";

// ================= FIXED ENDPOINTS =================

const PROFILE_API = `${BASE_URL}/profile/${email}`;
const PAYMENT_API = `${BASE_URL}/payments/upload`;
const PAYMENT_HISTORY_API = `${BASE_URL}/payments/${email}`;
const QUERY_API = `${BASE_URL}/queries/${email}`;

// ================= HEADERS =================

function authHeaders(includeJson = true) {

    const headers = {
        Authorization: `Bearer ${token}`
    };

    if (includeJson) {
        headers["Content-Type"] = "application/json";
    }

    return headers;
}

// ================= 401 HANDLER =================

function handleUnauthorized(response) {

    if (response.status === 401 || response.status === 403) {

        localStorage.clear();
        alert("Session expired");
        window.location.href = "/pages/login.html";

        return true;
    }

    return false;
}

// ================= PROFILE (WITH AVATAR FIXED) =================

async function loadProfile() {

    try {

        const response = await fetch(PROFILE_API, {
            method: "GET",
            headers: authHeaders(false)
        });

        console.log("PROFILE STATUS:", response.status);

        if (handleUnauthorized(response)) return;

        if (!response.ok) {
            const text = await response.text();
            console.error("PROFILE ERROR:", text);
            throw new Error("Failed profile");
        }

        const user = await response.json();

        const profileEl = document.getElementById("profile");
        if (!profileEl) return;

        profileEl.innerHTML = `
            <div class="profile-card">

                <div class="profile-avatar">
                    <img
                        src="https://ui-avatars.com/api/?name=${encodeURIComponent(
                            (user.firstName || "") + " " + (user.lastName || "")
                        )}&background=0D8ABC&color=fff&size=120"
                        alt="avatar"
                    />
                </div>

                <h2>
                    ${user.firstName ?? ""}
                    ${user.lastName ?? ""}
                </h2>

                <p><strong>Email:</strong> ${user.email ?? "N/A"}</p>
                <p><strong>Phone:</strong> ${user.phone ?? "N/A"}</p>
                <p><strong>Role:</strong> ${user.role ?? "MEMBER"}</p>

                <p>
                    <strong>Status:</strong>
                    <span class="${user.enabled ? "active" : "inactive"}">
                        ${user.enabled ? "ACTIVE" : "DISABLED"}
                    </span>
                </p>

            </div>
        `;

    } catch (error) {

        console.error("PROFILE LOAD FAILED:", error);
    }
}

// ================= QUERIES =================

async function loadQueries() {

    try {

        const response = await fetch(QUERY_API, {
            method: "GET",
            headers: authHeaders(false)
        });

        if (handleUnauthorized(response)) return;

        if (!response.ok) {
            throw new Error("Failed queries");
        }

        const data = await response.json();

        const table = document.querySelector("#myTable tbody");
        if (!table) return;

        table.innerHTML = "";

        if (!data.length) {
            table.innerHTML = `<tr><td colspan="5">No queries found</td></tr>`;
            return;
        }

        data.forEach(q => {

            table.innerHTML += `
                <tr>
                    <td>${q.id}</td>
                    <td>${q.subject ?? ""}</td>
                    <td>${q.message ?? ""}</td>
                    <td>${q.status ?? "PENDING"}</td>
                    <td>${q.response ?? "No response yet"}</td>
                </tr>
            `;
        });

    } catch (error) {
        console.error(error);
    }
}

// ================= PAYMENT HISTORY =================

async function loadPaymentHistory() {

    try {

        const response = await fetch(PAYMENT_HISTORY_API, {
            method: "GET",
            headers: authHeaders(false)
        });

        if (handleUnauthorized(response)) return;

        if (!response.ok) throw new Error("Failed payments");

        const payments = await response.json();

        const table = document.querySelector("#paymentTable tbody");
        if (!table) return;

        table.innerHTML = "";

        if (!payments.length) {
            table.innerHTML = `<tr><td colspan="6">No payments</td></tr>`;
            return;
        }

        payments.forEach(p => {

            table.innerHTML += `
                <tr>
                    <td>${p.id}</td>
                    <td>${p.paymentMonth ?? "-"}</td>
                    <td>R${p.amount ?? 0}</td>
                    <td>${p.status ?? "PENDING"}</td>
                    <td>${p.paymentDate ?? "-"}</td>
                    <td>${p.originalFileName ?? "-"}</td>
                </tr>
            `;
        });

    } catch (error) {
        console.error(error);
    }
}

// ================= UPLOAD PAYMENT =================

async function uploadPayment() {

    const file = document.getElementById("proofFile")?.files[0];
    const amount = document.getElementById("amount")?.value;

    if (!file || !amount) {
        alert("Select file and amount");
        return;
    }

    const formData = new FormData();
    formData.append("file", file);
    formData.append("amount", amount);

    try {

        const response = await fetch(PAYMENT_API, {
            method: "POST",
            headers: { Authorization: `Bearer ${token}` },
            body: formData
        });

        if (!response.ok) {
            throw new Error(await response.text());
        }

        alert("Payment uploaded");

        loadPaymentHistory();

    } catch (error) {
        console.error(error);
    }
}

// ================= LOGOUT =================

function logout() {
    localStorage.clear();
    window.location.href = "/pages/login.html";
}

// ================= INIT =================

document.addEventListener("DOMContentLoaded", () => {

    loadProfile();
    loadQueries();
    loadPaymentHistory();
});