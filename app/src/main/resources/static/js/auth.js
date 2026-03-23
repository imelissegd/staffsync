// auth.js — Login & Registration
import {
    URL_AUTH_REGISTER, URL_AUTH_LOGIN,
    PAGE_LOGIN, PAGE_DASHBOARD,
    MSG
} from "./config.js";

// ── Registration ──────────────────────────────────────────────
document.getElementById("registerForm")?.addEventListener("submit", async (e) => {
    e.preventDefault();
    const btn      = e.target.querySelector("button[type=submit]");
    const username = document.getElementById("regUsername").value.trim();
    const password = document.getElementById("regPassword").value;

    setLoading(btn, true, MSG.AUTH_REGISTERING);
    clearError();

    if (password.length < 6) {
        showBanner(MSG.AUTH_PASSWORD_TOO_SHORT, "error");
        setLoading(btn, false, "Sign Up");
        return;
    }

    try {
        const res = await fetch(URL_AUTH_REGISTER, {
            method:  "POST",
            headers: { "Content-Type": "application/json" },
            body:    JSON.stringify({ username, password })
        });

        const data = await res.json();

        if (res.ok && data.success !== false) {
            showBanner(MSG.AUTH_REGISTER_SUCCESS, "success");
            setTimeout(() => window.location.href = PAGE_LOGIN, 1500);
        } else {
            showBanner(data.message || MSG.AUTH_REGISTER_FAIL, "error");
        }
    } catch (err) {
        console.error(err);
        showBanner(MSG.AUTH_SERVER_UNREACHABLE, "error");
    } finally {
        setLoading(btn, false, "Sign Up");
    }
});

// ── Login ─────────────────────────────────────────────────────
document.getElementById("loginForm")?.addEventListener("submit", async (e) => {
    e.preventDefault();
    const btn      = e.target.querySelector("button[type=submit]");
    const username = document.getElementById("username").value.trim();
    const password = document.getElementById("password").value;

    setLoading(btn, true, MSG.AUTH_LOGGING_IN);
    clearError();

    try {
        const res = await fetch(URL_AUTH_LOGIN, {
            method:      "POST",
            headers:     { "Content-Type": "application/json" },
            body:        JSON.stringify({ username, password }),
            credentials: "include"
        });

        const data = await res.json();

        if (res.ok && data.success !== false) {
            localStorage.setItem("currentUser", JSON.stringify({
                username: data.username ?? username,
                role:     data.role ?? "ROLE_USER"
            }));
            window.location.href = PAGE_DASHBOARD;
        } else {
            showBanner(data.message || MSG.AUTH_LOGIN_FAIL, "error");
        }
    } catch (err) {
        console.error(err);
        showBanner(MSG.AUTH_SERVER_UNREACHABLE, "error");
    } finally {
        setLoading(btn, false, "Login");
    }
});

// ── Helpers ───────────────────────────────────────────────────
function setLoading(btn, loading, label) {
    if (!btn) return;
    btn.disabled    = loading;
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
    const form = card.querySelector("form");
    card.insertBefore(banner, form);
}
