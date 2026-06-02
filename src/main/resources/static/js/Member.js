// =========================================
// CONFIG
// =========================================

const BASE_URL = "https://society-kwgy.onrender.com";

const ADMIN_API = `${BASE_URL}/api/admin`;
const MEMBER_API = `${BASE_URL}/api/member`;

const USERS_API = `${ADMIN_API}/users`;
const PAYMENTS_API = `${ADMIN_API}/payments`;
const STATS_API = `${ADMIN_API}/stats`;
const AI_SUMMARY_API = `${ADMIN_API}/ai-summary`;
const PROFILE_API = `${ADMIN_API}/profile`;
const QUERIES_API = `${ADMIN_API}/queries`;

const PAYMENT_HISTORY_API = `${MEMBER_API}/payments/history`;
const PAYMENT_UPLOAD_API = `${MEMBER_API}/payments/upload`;

const token = localStorage.getItem("token");

if (!token) {
    window.location.href = "/pages/login.html";
}

//========AUTH HEADERS====================

function authHeaders(json = true) {

    const headers = {
        Authorization: `Bearer ${token}`
    };

    if (json) {
        headers["Content-Type"] = "application/json";
    }

    return headers;
}

//===============LOGOUT ONCE ============================

function logout() {
    localStorage.clear();
    window.location.href = "/pages/login.html";
}

function logout() {
    redirectToLogin();
}

// =========================================
// SECTION NAVIGATION
// =========================================

function showSection(sectionId) {

    document
        .querySelectorAll(".dashboard-section")
        .forEach(section => {
            section.style.display = "none";
        });

    const target =
        document.getElementById(sectionId);

    if (target) {
        target.style.display = "block";
    }
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

// =========================================
// PROFILE
// =========================================

async function loadProfile() {

    try {

        const response = await fetch(PROFILE_API, {
            method: "GET",
            headers: authHeaders(false)
        });

        if (!response.ok) throw new Error("Profile failed");

        const user = await response.json();

        const profile = document.getElementById("profile");
        if (!profile) return;

        profile.innerHTML = `
            <div class="profile-card">

                <div class="profile-avatar">
                    <img src="https://ui-avatars.com/api/?name=${encodeURIComponent(
                        (user.firstName || "") + " " + (user.lastName || "")
                    )}&background=0D8ABC&color=fff&size=120">
                </div>

                <h2>${user.firstName || ""} ${user.lastName || ""}</h2>

                <p>Email: ${user.email || "N/A"}</p>
                <p>Phone: ${user.phone || "N/A"}</p>
                <p>Role: ${user.role || "ADMIN"}</p>

            </div>
        `;

    } catch (error) {
        console.error("Profile error:", error);
    }
}

// ================= QUERIES =================

async function loadQueries() {

    try {

        const response = await fetch(QUERIES_API, {
            method: "GET",
            headers: authHeaders(false)
        });

        if (!response.ok) throw new Error("Failed queries");

        const data = await response.json();

        const table = document.querySelector("#myTable tbody");
        if (!table) return;

        table.innerHTML = "";

        if (!data?.length) {
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
        console.error("Queries error:", error);
    }
}

// ================= PAYMENT HISTORY =================

async function loadPaymentHistory() {

    try {

        const response = await fetch(PAYMENT_HISTORY_API, {
            method: "GET",
            headers: authHeaders(false)
        });

        if (!response.ok) throw new Error("Failed payments");

        const payments = await response.json();

        const table = document.querySelector("#paymentTable tbody");
        if (!table) return;

        table.innerHTML = "";

        if (!payments?.length) {
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
        console.error("Payments error:", error);
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

        const response = await fetch(PAYMENT_UPLOAD_API, {
            method: "POST",
            headers: {
                Authorization: `Bearer ${token}`
            },
            body: formData
        });

        if (!response.ok) {
            throw new Error(await response.text());
        }

        alert("Payment uploaded");

        loadPaymentHistory();

    } catch (error) {
        console.error("Upload error:", error);
    }
}

window.showSection = showSection;
window.logout = logout;
window.loadProfile = loadProfile;
window.loadQueries = loadQueries;
window.loadPaymentHistory = loadPaymentHistory;
window.uploadPayment = uploadPayment;

// ================= INIT =================

document.addEventListener("DOMContentLoaded", () => {

    loadProfile();
    loadQueries();
    loadPaymentHistory();
});