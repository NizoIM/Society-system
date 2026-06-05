// =========================================
// CONFIG (FIXED FOR RENDER)
// =========================================

// ================= BASE =================
const BASE_URL = "https://society-kwgy.onrender.com";

// ================= ADMIN =================
const ADMIN_API = `${BASE_URL}/api/admin`;

const USERS_API = `${ADMIN_API}/users`;
const PAYMENTS_API = `${ADMIN_API}/payments`;
const QUERIES_API = `${ADMIN_API}/queries`;
const STATS_API = `${ADMIN_API}/stats`;
const AI_SUMMARY_API = `${ADMIN_API}/ai-summary`;

const PROFILE_API = `${ADMIN_API}/profile`;
const PAYMENT_EXPORT_API = `${ADMIN_API}/payments/export`;

// ================= MEMBER =================
const MEMBER_API = `${BASE_URL}/api/member`;

const QUERY_API = `${MEMBER_API}/query`; // POST
const QUERY_GET_API = `${MEMBER_API}/query/${localStorage.getItem("email")}`;

const PAYMENT_HISTORY_API = `${MEMBER_API}/payments/${localStorage.getItem("email")}`;

const PAYMENT_UPLOAD_API = `${ADMIN_API}/payments/upload`;

const token = localStorage.getItem("token");

let paymentChart = null;

// =========================================
// AUTH
// =========================================

if (!token) {
    redirectToLogin();
}

function authHeaders(json = true) {

    const headers = {
        Authorization: `Bearer ${token}`
    };

    if (json) {
        headers["Content-Type"] =
            "application/json";
    }

    return headers;
}

function redirectToLogin() {

    localStorage.clear();

    window.location.href =
        "/pages/login.html";
}

function logout() {
    redirectToLogin();
}

function handleUnauthorized(response) {

    if (
        response.status === 401 ||
        response.status === 403
    ) {

        alert("Session expired");

        redirectToLogin();

        return true;
    }

    return false;
}

// =========================================
// RESPONSE HANDLER
// =========================================

async function handleResponse(response) {

    if (handleUnauthorized(response)) {
        return null;
    }

    if (!response.ok) {

        let message = "Request failed";

        try {

            const contentType =
                response.headers.get("content-type");

            if (
                contentType &&
                contentType.includes("application/json")
            ) {

                const err =
                    await response.json();

                message =
                    err.message || message;

            } else {

                message =
                    await response.text();
            }

        } catch (e) {

            console.error(e);
        }

        throw new Error(message);
    }

    const contentType =
        response.headers.get("content-type");

    if (
        contentType &&
        contentType.includes("application/json")
    ) {

        return await response.json();
    }

    return await response.text();
}

// =========================================
// API REQUEST
// =========================================

async function apiRequest(
    url,
    options = {}
) {

    const response =
        await fetch(url, {

            ...options,

            headers: {
                ...authHeaders(),
                ...(options.headers || {})
            }
        });

    return handleResponse(response);
}

// =========================================
// HELPERS
// =========================================

function escapeHtml(value) {

    return String(value ?? "")
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}

function renderStatus(enabled) {

    return enabled

        ? `
            <span class="badge badge-success">
                Enabled
            </span>
          `

        : `
            <span class="badge badge-danger">
                Disabled
            </span>
          `;
}
function renderQueries(data) {

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
                <td>${q.email}</td>
                <td>${q.subject}</td>
                <td>${q.message}</td>
                <td>${q.status}</td>
                <td>${q.response || "-"}</td>

                <td>
                    <button onclick="respondToQuery(${q.id})">
                        Respond
                    </button>
                </td>
            </tr>
        `;
    });
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
// DASHBOARD STATS
// =========================================

async function loadStats() {

    try {

        const stats =
            await apiRequest(STATS_API);

        const alerts =
            document.getElementById("alerts");

        if (!alerts) return;

        alerts.innerHTML = `

            <div class="alert-card">

                <h3>Dashboard Alerts</h3>

                <p>
                    Total Members:
                    <strong>${stats.totalMembers || 0}</strong>
                </p>

                <p>
                    Approved Payments:
                    <strong>${stats.approvedPayments || 0}</strong>
                </p>

                <p class="danger">
                    Overdue Payments:
                    <strong>${stats.overduePayments || 0}</strong>
                </p>

                <p class="danger">
                    Severe Overdue:
                    <strong>${stats.severeOverdue || 0}</strong>
                </p>

            </div>
        `;

    } catch (error) {

        console.error(error);
    }
}

// =========================================
// CHART
// =========================================

async function loadPaymentChart() {

    try {

        const data =
            await apiRequest(
                `${STATS_API}/monthly`
            );

        const ctx =
            document.getElementById("paymentChart");

        if (!ctx) return;

        const labels =
            data.map(d => d.month);

        const values =
            data.map(d => d.payments);

        if (paymentChart) {
            paymentChart.destroy();
        }

        paymentChart =
            new Chart(ctx, {

                type: "line",

                data: {

                    labels,

                    datasets: [{
                        label: "Payments",
                        data: values,
                        borderWidth: 3,
                        fill: false
                    }]
                }
            });

    } catch (error) {

        console.error(error);
    }
}

// =========================================
// AI SUMMARY
// =========================================

async function loadAiSummary() {

    try {

        const data =
            await apiRequest(AI_SUMMARY_API);

        const container =
            document.getElementById("aiSummary");

        if (!container) return;

        container.innerHTML = `

            <div class="alert-card">

                <p>
                    <strong>Total Income:</strong>
                    R${data.totalIncome || 0}
                </p>

                <p>
                    <strong>Approved Payments:</strong>
                    ${data.approvedPayments || 0}
                </p>

                <p>
                    <strong>Overdue Payments:</strong>
                    ${data.overduePayments || 0}
                </p>

                <p>
                    <strong>Rejected Payments:</strong>
                    ${data.rejectedPayments || 0}
                </p>

                <p>
                    <strong>Pending Queries:</strong>
                    ${data.pendingQueries || 0}
                </p>

                <hr>

                <p class="ai-text">
                    🤖 ${data.summary || ""}
                </p>

            </div>
        `;

    } catch (error) {

        console.error(error);
    }
}

// =========================================
// PROFILE
// =========================================

async function loadProfile() {

    try {

        const user =
            await apiRequest(PROFILE_API);

        const profile =
            document.getElementById("profile");

        if (!profile || !user) return;

      profile.innerHTML = `
      <div class="profile-card">

          <div class="profile-avatar">
              <img
                  src="https://ui-avatars.com/api/?name=${encodeURIComponent(
                      `${user.firstName || ""} ${user.lastName || ""}`
                  )}&background=0D8ABC&color=fff&size=180"
                  alt="Admin Avatar">
          </div>

          <h2>
              ${escapeHtml(user.firstName || "")}
              ${escapeHtml(user.lastName || "")}
          </h2>

          <p>
              <strong>Email:</strong>
              ${escapeHtml(user.email || "N/A")}
          </p>

          <p>
              <strong>Phone:</strong>
              ${escapeHtml(user.phone || "N/A")}
          </p>

          <p>
              <strong>Role:</strong>
              ${escapeHtml(user.role || "ADMIN")}
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

        console.error(error);
    }
}

// =========================================
// ADD USER
// =========================================

async function addUser() {

    const firstName =
        document.getElementById("firstName").value;

    const lastName =
        document.getElementById("lastName").value;

    const email =
        document.getElementById("email").value;

    const phone =
        document.getElementById("phone").value;

    const password =
        document.getElementById("password").value;

    const role =
        document.getElementById("role").value;

    if (
        !firstName ||
        !lastName ||
        !email ||
        !phone ||
        !password
    ) {

        alert("All fields required");

        return;
    }

    try {

        await apiRequest(
            USERS_API,
            {
                method: "POST",

                body: JSON.stringify({
                    firstName,
                    lastName,
                    email,
                    phone,
                    password,
                    role
                })
            }
        );

        alert("User added successfully");

        document.getElementById("firstName").value = "";
        document.getElementById("lastName").value = "";
        document.getElementById("email").value = "";
        document.getElementById("phone").value = "";
        document.getElementById("password").value = "";

        loadUsers();

    } catch (error) {

        console.error(error);

        alert(error.message);
    }
}

// =========================================
// EDIT USER
// =========================================

async function editUser(id) {

    try {

        const users =
            await apiRequest(USERS_API);

        const user =
            users.find(u => u.id === id);

        if (!user) {
            alert("User not found");
            return;
        }

        const firstName =
            prompt("First Name", user.firstName);

        if (firstName === null) return;

        const lastName =
            prompt("Last Name", user.lastName);

        if (lastName === null) return;

        const email =
            prompt("Email", user.email);

        if (email === null) return;

        const phone =
            prompt("Phone", user.phone);

        if (phone === null) return;

        const role =
            prompt("Role (ADMIN, STAFF, MEMBER)", user.role);

        if (role === null) return;

        const enabled =
            confirm("Enable this user?");

        await apiRequest(
            `${USERS_API}/${id}`,
            {
                method: "PUT",

                body: JSON.stringify({
                    firstName,
                    lastName,
                    email,
                    phone,
                    role,
                    enabled
                })
            }
        );

        alert("User updated successfully");

        loadUsers();

    } catch (error) {

        console.error(error);

        alert(error.message);
    }
}

// =========================================
// DELETE USER
// =========================================

async function deleteUser(id) {

    const confirmed =
        confirm(
            "Are you sure you want to delete this user?"
        );

    if (!confirmed) return;

    try {

        await apiRequest(
            `${USERS_API}/${id}`,
            {
                method: "DELETE"
            }
        );

        alert("User deleted successfully");

        loadUsers();

    } catch (error) {

        console.error(error);

        alert(error.message);
    }
}

// =========================================
// SEARCH USERS
// =========================================

async function searchUsers() {

    try {

        const value =
            document.getElementById("search")
                .value
                .toLowerCase();

        const users =
            await apiRequest(USERS_API);

        const filtered =
            users.filter(user =>

                user.email
                    .toLowerCase()
                    .includes(value)

                ||

                `${user.firstName} ${user.lastName}`
                    .toLowerCase()
                    .includes(value)
            );

        renderUsers(filtered);

    } catch (error) {

        console.error(error);
    }
}
// =========================================
// PAYMENTS
// =========================================

async function loadPayments() {

    try {

        const payments =
            await apiRequest(PAYMENTS_API);

        renderPayments(payments);

    } catch (error) {

        console.error(error);
    }
}

function renderPayments(payments) {

    const table =
        document.querySelector(
           "#adminPaymentTable tbody"
        );

    if (!table) return;

    table.innerHTML = "";

    payments.forEach(payment => {

        table.innerHTML += `

            <tr>

                <td>${payment.id}</td>

                <td>${escapeHtml(payment.memberEmail)}</td>

                <td>R${payment.amount}</td>

                <td>${payment.paymentMonth || "-"}</td>

                <td>${payment.paymentDate || "-"}</td>

                <td>${payment.status}</td>

                <td>

                    Behind:
                    ${payment.monthsBehind || 0}

                    <br>

                    Ahead:
                    ${payment.monthsAhead || 0}

                </td>

                <td>

                    <a
                        href="${BASE_URL}/api/files/payment/${payment.id}"
                        target="_blank">

                        View Proof

                    </a>

                </td>

                <td>

                    <button
                        class="edit-btn"
                        onclick="approvePayment(${payment.id})">

                        Approve

                    </button>

                    <button
                        class="delete-btn"
                        onclick="rejectPayment(${payment.id})">

                        Reject

                    </button>

                </td>

            </tr>
        `;
    });
}
// =========================================
// PAYMENTS
// =========================================

async function loadPayments() {

    try {

        const payments = await apiRequest(PAYMENTS_API);

        const table = document.querySelector("#adminPaymentTable tbody");

        if (!table) return;

        table.innerHTML = "";

        payments.forEach(p => {

            table.innerHTML += `
                <tr>
                    <td>${p.id}</td>
                    <td>${p.memberEmail || "Unknown"}</td>
                    <td>R${p.amount}</td>
                    <td>${p.paymentMonth || "-"}</td>
                    <td>${p.paymentDate || "-"}</td>
                    <td>${p.status || "PENDING"}</td>

                    <td>
                        <a href="${BASE_URL}/api/files/payment/${p.id}" target="_blank">
                            View
                        </a>
                    </td>
                </tr>
            `;
        });

    } catch (err) {
        console.error(err);
    }
}
// =========================================
// PAYMENT HISTORY
// =========================================

async function loadPaymentHistory() {

    try {

        const payments =
            await apiRequest(
                PAYMENT_HISTORY_API
            );

        const table =
            document.querySelector(
                "#paymentHistoryTable tbody"
            );

        if (!table) return;

        table.innerHTML = "";

        if (!payments.length) {

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

                    <td>${payment.id || "-"}</td>

                    <td>${payment.paymentMonth || "-"}</td>

                    <td>R${payment.amount || 0}</td>

                    <td>${payment.status || "-"}</td>

                    <td>${payment.paymentDate || "-"}</td>

                    <td>

                        <a
                             href="${BASE_URL}/api/files/payment/${payment.id}"
                            target="_blank">

                            View Proof

                        </a>

                    </td>

                </tr>
            `;
        });

    } catch (error) {

        console.error(error);
    }
}

// =========================================
// USERS
// =========================================

async function loadUsers() {

    try {

        const users =
            await apiRequest(USERS_API);

        renderUsers(users);

    } catch (error) {

        console.error(error);
    }
}

function renderUsers(users) {

    const table =
        document.querySelector(
            "#userTable tbody"
        );

    if (!table) return;

    table.innerHTML = "";

    if (!users.length) {

        table.innerHTML = `
            <tr>
                <td colspan="7">
                    No users found
                </td>
            </tr>
        `;

        return;
    }

    users.forEach(user => {

        table.innerHTML += `

            <tr>

                <td>${user.id}</td>

                <td>${escapeHtml(user.email)}</td>

                <td>
                    ${escapeHtml(user.firstName)}
                    ${escapeHtml(user.lastName)}
                </td>

                <td>${escapeHtml(user.phone)}</td>

                <td>${escapeHtml(user.role)}</td>

                <td>${renderStatus(user.enabled)}</td>

                <td>

                    <button
                        class="edit-btn"
                        onclick="editUser(${user.id})">

                        Edit

                    </button>

                    <button
                        class="delete-btn"
                        onclick="deleteUser(${user.id})">

                        Delete

                    </button>

                </td>

            </tr>
        `;
    });
}

// =========================================
// APPROVE / REJECT
// =========================================

async function approvePayment(id) {

    try {

        const result =
            await apiRequest(
                `${PAYMENTS_API}/${id}/approve`,
                {
                    method: "PUT"
                }
            );

        alert(result.message);

        loadPayments();
        loadStats();

    } catch (error) {

        console.error(error);
    }
}

async function rejectPayment(id) {

    try {

        const result =
            await apiRequest(
                `${PAYMENTS_API}/${id}/reject`,
                {
                    method: "PUT"
                }
            );

        alert(result.message);

        loadPayments();
        loadStats();

    } catch (error) {

        console.error(error);
    }
}
// =========================================
// UPLOAD PAYMENT
// =========================================

async function uploadPayment() {
    const file = document.getElementById("proofFile").files[0];
    const amount = document.getElementById("amount").value;

    if (!file || !amount) {
        alert("Select file + amount");
        return;
    }

    const form = new FormData();
    form.append("file", file);
    form.append("amount", amount);

    const res = await fetch(PAYMENT_UPLOAD_API, {
        method: "POST",
        headers: {
            Authorization: `Bearer ${token}`
        },
        body: form
    });

    await handleResponse(res);

    alert("Uploaded");
    loadPaymentHistory();
}

// =========================================
// DOWNLOAD PDF
// =========================================

async function downloadStats() {

    try {

        const response = await fetch(
                                   PAYMENT_EXPORT_API,
                                   {
                                       method: "GET",
                                       headers: {
                                           Authorization: `Bearer ${token}`
                                       }
                                   }
                               );

        if (!response.ok) {
            throw new Error("Download failed");
        }

        const blob = await response.blob();

        const url =
            window.URL.createObjectURL(blob);

        const a =
            document.createElement("a");

        a.href = url;
        a.download = "payments.pdf";

        document.body.appendChild(a);

        a.click();

        a.remove();

        window.URL.revokeObjectURL(url);

    } catch (error) {

        console.error(error);

        alert(error.message);
    }
}
// =========================================
// QUERIES (FIXED - SHOW ALL + RESPOND)
// =========================================

async function loadQueries() {

    try {

        const queries = await apiRequest(QUERIES_API);

        const table = document.querySelector("#queryTable tbody");

        if (!table) return;

        table.innerHTML = "";

        if (!queries.length) {
            table.innerHTML = `
                <tr><td colspan="6">No queries found</td></tr>
            `;
            return;
        }

        queries.forEach(q => {
            table.innerHTML += `
                <tr>
                    <td>${q.id}</td>
                    <td>${q.email}</td>
                    <td>${q.subject}</td>
                    <td>${q.message}</td>
                    <td>${q.status}</td>
                    <td>${q.response || "-"}</td>

                    <td>
                        <button onclick="respondToQuery(${q.id})">
                            Respond
                        </button>
                    </td>
                </tr>
            `;
        });

    } catch (err) {
        console.error(err);
    }
}

// =========================================
// RESPOND QUERY
// =========================================

async function respondToQuery(id) {

    const message = prompt("Enter response");

    if (!message) return;

    try {

        await apiRequest(`${QUERIES_API}/${id}`, {
            method: "PUT",
            body: JSON.stringify({
                status: "RESOLVED",
                response: message
            })
        });

        alert("Response sent");

        loadQueries();

    } catch (err) {
        console.error(err);
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

// =========================================
// GLOBALS
// =========================================

window.showSection = showSection;
window.logout = logout;
window.addUser = addUser;
window.editUser = editUser;
window.deleteUser = deleteUser;
window.searchUsers = searchUsers;
window.searchQueries = searchQueries;
window.respondToQuery = respondToQuery;
window.approvePayment = approvePayment;
window.rejectPayment = rejectPayment;
window.downloadStats = downloadStats;
window.uploadPayment = uploadPayment;

// =========================================
// INIT
// =========================================

document.addEventListener(
    "DOMContentLoaded",
    () => {

        showSection("dashboard-section");

        loadProfile();
        loadStats();
        loadUsers();
        loadPayments();
        loadQueries();
        loadPaymentHistory();
        loadAiSummary();
        loadPaymentChart();

    }
);