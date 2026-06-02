// =========================================
// CONFIG (FIXED FOR RENDER)
// =========================================

const BASE_URL = "https://society-kwgy.onrender.com";

const ADMIN_API = `${BASE_URL}/api/admin`;

const USERS_API = `${ADMIN_API}/users`;
const PAYMENTS_API = `${ADMIN_API}/payments`;
const STATS_API = `${ADMIN_API}/stats`;
const AI_SUMMARY_API = `${ADMIN_API}/ai-summary`;

const MEMBER_API = `${BASE_URL}/api/member`;

const QUERIES_API = `${BASE_URL}/api/member/queries`;
const PROFILE_API = `${BASE_URL}/api/member/profile`;

const PAYMENT_HISTORY_API = `${BASE_URL}/api/member/payments/history`;
const PAYMENT_UPLOAD_API = `${BASE_URL}/api/member/payments/upload`;

const token = localStorage.getItem("token");

let paymentChart = null;

// =========================================
// AUTH CHECK
// =========================================

if (!token) {
    window.location.href = "/pages/login.html";
}

function authHeaders(json = true) {
    const headers = {
        Authorization: `Bearer ${token}`
    };

    if (json) {
        headers["Content-Type"] = "application/json";
    }

    return headers;
}

function handleUnauthorized(response) {
    if (response.status === 401 || response.status === 403) {
        alert("Session expired");
        localStorage.clear();
        window.location.href = "/pages/login.html";
        return true;
    }
    return false;
}

// =========================================
// SAFE RESPONSE HANDLER
// =========================================

async function handleResponse(response) {

    if (handleUnauthorized(response)) return null;

    let text = await response.text();

    if (!response.ok) {
        throw new Error(text || "Request failed");
    }

    try {
        return JSON.parse(text);
    } catch {
        return text;
    }
}

// =========================================
// API WRAPPER
// =========================================

async function apiRequest(url, options = {}) {

    const response = await fetch(url, {
        ...options,
        headers: {
            ...authHeaders(),
            ...(options.headers || {})
        }
    });

    return handleResponse(response);
}

// =========================================
// ESCAPE HTML
// =========================================

function escapeHtml(value) {
    return String(value ?? "")
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}

// =========================================
// PROFILE (FULLY FIXED + AVATAR)
// =========================================

async function loadProfile() {

    try {

        const user = await apiRequest(PROFILE_API);

        const profile = document.getElementById("profile");

        if (!profile) return;

        if (!user) {
            profile.innerHTML = `<p>Profile not found</p>`;
            return;
        }

        const avatar = user.firstName
            ? user.firstName.charAt(0).toUpperCase()
            : "U";

        profile.innerHTML = `
            <div class="profile-card">

                <div class="avatar-circle">
                    ${avatar}
                </div>

                <h2>
                    ${escapeHtml(user.firstName || "")}
                    ${escapeHtml(user.lastName || "")}
                </h2>

                <p><strong>Email:</strong> ${escapeHtml(user.email || "N/A")}</p>
                <p><strong>Phone:</strong> ${escapeHtml(user.phone || "N/A")}</p>
                <p><strong>Role:</strong> ${escapeHtml(user.role || "MEMBER")}</p>

                <p>
                    <strong>Status:</strong>
                    <span class="${user.enabled ? "active" : "inactive"}">
                        ${user.enabled ? "ACTIVE" : "DISABLED"}
                    </span>
                </p>

            </div>
        `;

    } catch (error) {
        console.error("PROFILE ERROR:", error);

        const profile = document.getElementById("profile");

        if (profile) {
            profile.innerHTML = `
                <div class="profile-error">
                    Failed to load profile
                </div>
            `;
        }
    }
}

// =========================================
// LOAD QUERIES (SAFE)
// =========================================

async function loadQueries() {

    try {

        const data = await apiRequest(QUERIES_API);

        const table = document.querySelector("#queryTable tbody");

        if (!table) return;

        table.innerHTML = "";

        if (!Array.isArray(data) || data.length === 0) {
            table.innerHTML = `
                <tr>
                    <td colspan="5">No queries found</td>
                </tr>
            `;
            return;
        }

        data.forEach(q => {

            table.innerHTML += `
                <tr>
                    <td>${q.id ?? "-"}</td>
                    <td>${escapeHtml(q.email || "-")}</td>
                    <td>${escapeHtml(q.subject || "")}</td>
                    <td>${escapeHtml(q.message || "")}</td>
                    <td>${escapeHtml(q.status || "PENDING")}</td>
                </tr>
            `;
        });

    } catch (error) {
        console.error(error);
    }
}

// =========================================
// PAYMENT HISTORY (SAFE)
// =========================================

async function loadPaymentHistory() {

    try {

        const payments = await apiRequest(PAYMENT_HISTORY_API);

        const table = document.querySelector("#paymentTable tbody");

        if (!table) return;

        table.innerHTML = "";

        if (!Array.isArray(payments) || payments.length === 0) {
            table.innerHTML = `
                <tr>
                    <td colspan="6">No payments found</td>
                </tr>
            `;
            return;
        }

        payments.forEach(p => {

            table.innerHTML += `
                <tr>
                    <td>${p.id ?? "-"}</td>
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

// =========================================
// PAYMENT UPLOAD (FIXED)
// =========================================

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

        await handleResponse(response);

        alert("Payment uploaded");

        loadPaymentHistory();

    } catch (error) {
        console.error(error);
        alert("Upload failed");
    }
}

// =========================================
// LOGOUT
// =========================================

function logout() {
    localStorage.clear();
    window.location.href = "/pages/login.html";
}

// =========================================
// INIT
// =========================================

document.addEventListener("DOMContentLoaded", () => {
    loadProfile();
    loadQueries();
    loadPaymentHistory();
});