// =========================================
// AUTH
// =========================================

const token = localStorage.getItem("token");
const email = localStorage.getItem("email");

if (!token) {
    localStorage.clear();
    window.location.href = "/pages/login.html";
}

// =========================================
// CONFIG
// =========================================

const BASE_URL = "https://society-kwgy.onrender.com";

const ADMIN_API = `${BASE_URL}/api/admin`;

// =========================================
// ADMIN ENDPOINTS
// =========================================

const PROFILE_API =
    email
        ? `${ADMIN_API}/profile/${encodeURIComponent(email)}`
        : `${ADMIN_API}/profile`;

const QUERIES_API = `${ADMIN_API}/queries`;

const PAYMENT_UPLOAD_API =
    `${ADMIN_API}/payments/upload`;

const PAYMENT_HISTORY_API =
    `${ADMIN_API}/payments/history`;

// =========================================
// HEADERS
// =========================================

function authHeaders(includeJson = true) {

    const headers = {
        Authorization: `Bearer ${token}`
    };

    if (includeJson) {
        headers["Content-Type"] = "application/json";
    }

    return headers;
}

// =========================================
// LOGOUT
// =========================================

function logout() {

    localStorage.clear();

    window.location.href =
        "/pages/login.html";
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

// =========================================
// 401 / 403 HANDLER
// =========================================

function handleUnauthorized(response) {

    if (
        response.status === 401 ||
        response.status === 403
    ) {

        localStorage.clear();

        alert("Session expired. Please login again.");

        window.location.href =
            "/pages/login.html";

        return true;
    }

    return false;
}

// =========================================
// PROFILE
// =========================================

async function loadProfile() {

    try {

        const response =
            await fetch(PROFILE_API, {
                method: "GET",
                headers: authHeaders(false)
            });

        if (handleUnauthorized(response)) return;

        if (!response.ok) {

            throw new Error(
                `Profile request failed (${response.status})`
            );
        }

        const user =
            await response.json();

        const profile =
            document.getElementById("profile");

        if (!profile) return;

        const fullName =
            `${user.firstName || ""} ${user.lastName || ""}`.trim();

        const avatarUrl =
            `https://ui-avatars.com/api/?name=${encodeURIComponent(
                fullName || "Admin"
            )}&background=0D8ABC&color=fff&size=120`;

        profile.innerHTML = `

            <div class="profile-card">

                <div class="profile-avatar">

                    <img
                        src="${avatarUrl}"
                        alt="Profile Avatar"
                    >

                </div>

                <h2>${fullName}</h2>

                <p>
                    <strong>Email:</strong>
                    ${user.email || "N/A"}
                </p>

                <p>
                    <strong>Phone:</strong>
                    ${user.phone || "N/A"}
                </p>

                <p>
                    <strong>Role:</strong>
                    ${user.role || "ADMIN"}
                </p>

                <p>
                    <strong>Status:</strong>

                    <span class="${
                        user.enabled
                            ? "active"
                            : "inactive"
                    }">

                        ${
                            user.enabled
                                ? "ACTIVE"
                                : "DISABLED"
                        }

                    </span>

                </p>

            </div>
        `;

    } catch (error) {

        console.error(
            "Profile load error:",
            error
        );

        const profile =
            document.getElementById("profile");

        if (profile) {

            profile.innerHTML = `

                <div class="profile-card">

                    <h3>
                        Failed to load profile
                    </h3>

                    <p>
                        ${error.message}
                    </p>

                </div>
            `;
        }
    }
}

// =========================================
// QUERIES
// =========================================

async function loadQueries() {

    try {

        const response =
            await fetch(QUERIES_API, {
                method: "GET",
                headers: authHeaders(false)
            });

        if (handleUnauthorized(response)) return;

        if (!response.ok) {
            throw new Error(
                `Query request failed (${response.status})`
            );
        }

        const data =
            await response.json();

        const table =
            document.querySelector(
                "#queryTable tbody"
            ) ||
            document.querySelector(
                "#myTable tbody"
            );

        if (!table) return;

        table.innerHTML = "";

        if (!data || !data.length) {

            table.innerHTML = `
                <tr>
                    <td colspan="5">
                        No queries found
                    </td>
                </tr>
            `;

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

        console.error(
            "Queries error:",
            error
        );
    }
}

// =========================================
// PAYMENT HISTORY
// =========================================

async function loadPaymentHistory() {

    try {

        const response =
            await fetch(
                PAYMENT_HISTORY_API,
                {
                    method: "GET",
                    headers: authHeaders(false)
                }
            );

        if (handleUnauthorized(response)) return;

        if (!response.ok) {

            throw new Error(
                `Payment request failed (${response.status})`
            );
        }

        const payments =
            await response.json();

        const table =
            document.querySelector(
                "#paymentTable tbody"
            );

        if (!table) return;

        table.innerHTML = "";

        if (!payments?.length) {

            table.innerHTML = `
                <tr>
                    <td colspan="6">
                        No payments found
                    </td>
                </tr>
            `;

            return;
        }

        payments.forEach(payment => {

            table.innerHTML += `
                <tr>
                    <td>${payment.id}</td>
                    <td>${payment.paymentMonth ?? "-"}</td>
                    <td>R${payment.amount ?? 0}</td>
                    <td>${payment.status ?? "PENDING"}</td>
                    <td>${payment.paymentDate ?? "-"}</td>
                    <td>${payment.originalFileName ?? "-"}</td>
                </tr>
            `;
        });

    } catch (error) {

        console.error(
            "Payment history error:",
            error
        );
    }
}

// =========================================
// PAYMENT UPLOAD
// =========================================

async function uploadPayment() {

    const file =
        document.getElementById("proofFile")?.files[0];

    const amount =
        document.getElementById("amount")?.value;

    if (!file || !amount) {

        alert(
            "Select file and amount"
        );

        return;
    }

    const formData =
        new FormData();

    formData.append("file", file);
    formData.append("amount", amount);

    try {

        const response =
            await fetch(
                PAYMENT_UPLOAD_API,
                {
                    method: "POST",
                    headers: {
                        Authorization:
                            `Bearer ${token}`
                    },
                    body: formData
                }
            );

        if (handleUnauthorized(response)) return;

        if (!response.ok) {

            throw new Error(
                await response.text()
            );
        }

        alert(
            "Payment uploaded successfully"
        );

        await loadPaymentHistory();

    } catch (error) {

        console.error(
            "Upload error:",
            error
        );

        alert(error.message);
    }
}

// =========================================
// GLOBALS
// =========================================

window.showSection = showSection;
window.logout = logout;
window.uploadPayment = uploadPayment;

// =========================================
// INIT
// =========================================

document.addEventListener(
    "DOMContentLoaded",
    async () => {

        await loadProfile();

        await Promise.all([
            loadQueries(),
            loadPaymentHistory()
        ]);
    }
);