function sendOtp() {

    fetch("/api/auth/send-otp?email=" +
        document.getElementById("email").value,

        { method: "POST" }

    ).then(res => res.text())

    .then(data => alert(data));
}

function resetPassword() {

    const email = document.getElementById("email").value;
    const otp = document.getElementById("otp").value;
    const newPassword = document.getElementById("newPassword").value;

    fetch("/api/auth/reset-password", {

        method: "POST",

        headers: {
            "Content-Type": "application/x-www-form-urlencoded"
        },

        body:
            "email=" + email +
            "&otp=" + otp +
            "&newPassword=" + newPassword
    })
    .then(res => res.text())
    .then(data => alert(data));
}