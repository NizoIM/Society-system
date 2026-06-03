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
                  alt="Member Avatar">
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