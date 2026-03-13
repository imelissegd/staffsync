// auth.js — Login & Registration

const API = "http://localhost:8080/api";

// ── Registration ──────────────────────────────────────────────
document.getElementById("registerForm")?.addEventListener("submit", async (e) => {
    e.preventDefault();
    const btn = e.target.querySelector("button[type=submit]");
    const username = document.getElementById("regUsername").value.trim();
    const password = document.getElementById("regPassword").value;

    setLoading(btn, true, "Registering…");
    clearError();

    try {
        const res = await fetch(`${API}/auth/register`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ username, password })
        });

        const data = await res.json();

        if (res.ok && data.success !== false) {
            showBanner("Account created! Redirecting to login…", "success");
            setTimeout(() => window.location.href = "login.html", 1500);
        } else {
            showBanner(data.message || "Registration failed.", "error");
        }
    } catch (err) {
        console.error(err);
        showBanner("Could not reach the server. Make sure the backend is running.", "error");
    } finally {
        setLoading(btn, false, "Sign Up");
    }
});

// ── Login ─────────────────────────────────────────────────────
document.getElementById("loginForm")?.addEventListener("submit", async (e) => {
    e.preventDefault();
    const btn = e.target.querySelector("button[type=submit]");
    const username = document.getElementById("username").value.trim();
    const password = document.getElementById("password").value;

    setLoading(btn, true, "Logging in…");
    clearError();

    try {
        const res = await fetch(`${API}/auth/login`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ username, password }),
            credentials: "include"
        });

        const data = await res.json();

        if (res.ok && data.success !== false) {
            localStorage.setItem("currentUser", JSON.stringify({
                username: data.username ?? username,
                role: data.role ?? "ROLE_USER"
            }));
            window.location.href = "index.html";
        } else {
            showBanner(data.message || "Invalid username or password.", "error");
        }
    } catch (err) {
        console.error(err);
        showBanner("Could not reach the server. Make sure the backend is running.", "error");
    } finally {
        setLoading(btn, false, "Login");
    }
});

// ── Helpers ───────────────────────────────────────────────────
function setLoading(btn, loading, label) {
    if (!btn) return;
    btn.disabled = loading;
    btn.textContent = label;
}

function clearError() {
    const el = document.getElementById("authBanner");
    if (el) el.remove();
}

function showBanner(msg, type) {
    clearError();
    const card = document.querySelector(".auth-card");
    if (!card) return;
    const banner = document.createElement("div");
    banner.id = "authBanner";
    banner.style.cssText = `
        padding: 0.7rem 1rem;
        border-radius: 8px;
        font-size: 0.83rem;
        font-weight: 500;
        margin-bottom: 1rem;
        background: ${type === "success" ? "rgba(46,125,82,0.1)" : "rgba(192,57,43,0.1)"};
        color: ${type === "success" ? "var(--success)" : "var(--error)"};
        border: 1.5px solid ${type === "success" ? "rgba(46,125,82,0.25)" : "rgba(192,57,43,0.25)"};
    `;
    banner.textContent = msg;
    // Insert before the form
    const form = card.querySelector("form");
    card.insertBefore(banner, form);
}