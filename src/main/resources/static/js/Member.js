// ================= AUTH =================

const token = localStorage.getItem("token");
const email = localStorage.getItem("email");

if (!token || !email) {
    window.location.href = "/pages/login.html";
}

// ================= API =================

//const BASE_URL = "http://localhost:8080/api/member";

const API_URL =
        "https://society-kwgy.onrender.com";

const PROFILE_API =
    `${BASE_URL}/profile/${email}`;

const PAYMENT_API =
    `${BASE_URL}/payment/${email}`;

const PAYMENT_HISTORY_API =
    `${BASE_URL}/payments/${email}`;

const QUERY_API =
    `${BASE_URL}/query`;

// ================= AUTH HEADERS =================

function authHeaders(includeJson = true) {

    const headers = {
        Authorization: `Bearer ${token}`
    };

    if (includeJson) {
        headers["Content-Type"] = "application/json";
    }

    return headers;
}

// ================= HANDLE 401 =================

function handleUnauthorized(response) {

    if (
        response.status === 401 ||
        response.status === 403
    ) {

        localStorage.clear();

        alert("Session expired. Login again.");

        window.location.href =
            "/pages/login.html";

        return true;
    }

    return false;
}

// ================= LOAD PROFILE =================

async function loadProfile() {

    try {

        const response = await fetch(
            PROFILE_API,
            {
                method: "GET",
                headers: authHeaders(false)
            }
        );

        if (handleUnauthorized(response)) return;

        if (!response.ok) {
            throw new Error("Failed profile");
        }

        const user = await response.json();

        document.getElementById("profile").innerHTML = `

            <div class="profile-card">

                <h2>
                    ${user.firstName ?? ""}
                    ${user.lastName ?? ""}
                </h2>

                <p>
                    <strong>Email:</strong>
                    ${user.email ?? "N/A"}
                </p>

                <p>
                    <strong>Phone:</strong>
                    ${user.phone ?? "N/A"}
                </p>

                <p>
                    <strong>Role:</strong>
                    ${user.role ?? "MEMBER"}
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

        alert("Failed to load profile");
    }
}

// ================= SEND QUERY =================

async function sendQuery() {

    const subject =
        document.getElementById("subject")
            .value
            .trim();

    const message =
        document.getElementById("message")
            .value
            .trim();

    if (!subject || !message) {

        alert("Fill all fields");

        return;
    }

    try {

        const response = await fetch(
            QUERY_API,
            {
                method: "POST",

                headers: authHeaders(),

                body: JSON.stringify({
                    email,
                    subject,
                    message,
                    status: "PENDING"
                })
            }
        );

        if (handleUnauthorized(response)) return;

        if (!response.ok) {
            throw new Error("Failed query");
        }

        alert("Query sent successfully");

        document.getElementById("subject").value = "";
        document.getElementById("message").value = "";

        loadQueries();

    } catch (error) {

        console.error(error);

        alert("Failed to send query");
    }
}

// ================= LOAD QUERIES =================

async function loadQueries() {

    try {

        const response = await fetch(
            `${QUERY_API}/${email}`,
            {
                method: "GET",
                headers: authHeaders(false)
            }
        );

        if (handleUnauthorized(response)) return;

        if (!response.ok) {
            throw new Error("Failed queries");
        }

        const data = await response.json();

        const table =
            document.querySelector(
                "#myTable tbody"
            );

        if (!table) return;

        table.innerHTML = "";

        if (!data || data.length === 0) {

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

                    <td>
                        ${q.subject ?? ""}
                    </td>

                    <td>
                        ${q.message ?? ""}
                    </td>

                    <td>
                        ${q.status ?? "PENDING"}
                    </td>

                    <td>
                        ${
                            q.response ??
                            "No response yet"
                        }
                    </td>

                </tr>
            `;
        });

    } catch (error) {

        console.error(error);

        alert("Failed to load queries");
    }
}


// ================= UPLOAD PAYMENT =================

async function uploadPayment() {

    const file =
        document.getElementById("proofFile").files[0];

    const amount =
        document.getElementById("amount").value;

    if (!file || !amount) {
        alert("Enter amount and choose file");
        return;
    }

    const formData = new FormData();
    formData.append("file", file);
    formData.append("amount", amount);

    try {

        const response = await fetch(
            PAYMENT_API,
            {
                method: "POST",
                headers: {
                    Authorization: `Bearer ${token}`
                },
                body: formData
            }
        );

        if (handleUnauthorized(response)) return;

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText);
        }

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

        const response = await fetch(
            PAYMENT_HISTORY_API,
            {
                method: "GET",
                headers: authHeaders(false)
            }
        );

        if (handleUnauthorized(response)) return;

        if (!response.ok) {
            throw new Error("Failed payments");
        }

        const payments =
            await response.json();

        const table =
            document.querySelector(
                "#paymentTable tbody"
            );

        if (!table) return;

        table.innerHTML = "";

        if (
            !payments ||
            payments.length === 0
        ) {

            table.innerHTML = `
                <tr>
                    <td colspan="6">
                        No payments uploaded
                    </td>
                </tr>
            `;

            return;
        }

        payments.forEach(payment => {

            table.innerHTML += `

                <tr>

                    <td>${payment.id}</td>

                    <td>
                        ${
                            payment.paymentMonth ??
                            "-"
                        }
                    </td>

                    <td>
                        R${
                            payment.amount ??
                            "0.00"
                        }
                    </td>

                    <td>
                        ${
                            payment.status ??
                            "PENDING"
                        }
                    </td>

                    <td>
                        ${
                            payment.paymentDate ??
                            "-"
                        }
                    </td>

                    <td>
                        ${
                            payment.originalFileName ??
                            "-"
                        }
                    </td>

                </tr>
            `;
        });

    } catch (error) {

        console.error(error);

        alert("Failed to load payments");
    }
}

// ================= LOGOUT =================

function logout() {

    localStorage.clear();

    window.location.href =
        "/pages/login.html";
}

// ================= INIT =================

document.addEventListener(
    "DOMContentLoaded",
    () => {

        loadProfile();
        loadQueries();
        loadPaymentHistory();
    }
);