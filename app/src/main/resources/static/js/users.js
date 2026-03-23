// users.js
import { API_USERS, PAGE_LOGIN, PAGE_DASHBOARD, ROLE_ADMIN, MSG } from "./config.js";

// ── Auth guard — admin only ───────────────────────────────────────────────────
const currentUser = (() => {
    try { return JSON.parse(localStorage.getItem("currentUser")); }
    catch { return null; }
})();
if (!currentUser)                          window.location.href = PAGE_LOGIN;
if (currentUser.role !== ROLE_ADMIN)       window.location.href = PAGE_DASHBOARD;

// ── State ─────────────────────────────────────────────────────────────────────
let users = [];

// ── Init ──────────────────────────────────────────────────────────────────────
document.addEventListener("DOMContentLoaded", () => {
    loadUsers();
    document.getElementById("userForm").addEventListener("submit", handleSubmit);
});

// ── Load all users ────────────────────────────────────────────────────────────
async function loadUsers() {
    try {
        const res = await fetch(API_USERS);
        if (!res.ok) throw new Error(MSG.USER_LOAD_FAIL);
        users = await res.json();
        renderTable();
    } catch {
        document.getElementById("userTableBody").innerHTML =
            `<tr><td class="state-cell" colspan="4">${MSG.USER_LOAD_FAIL}</td></tr>`;
    }
}

// ── Render table ──────────────────────────────────────────────────────────────
function renderTable() {
    const tbody   = document.getElementById("userTableBody");
    const countEl = document.getElementById("userCount");

    countEl.textContent = `${users.length} user${users.length !== 1 ? "s" : ""}`;

    if (users.length === 0) {
        tbody.innerHTML = `<tr><td class="state-cell" colspan="4">No users found.</td></tr>`;
        return;
    }

    tbody.innerHTML = users.map(u => {
        const isCurrentUser = u.username === currentUser.username;
        const roleLabel     = u.role === ROLE_ADMIN ? "Admin" : "User";
        const roleBadge     = u.role === ROLE_ADMIN
            ? `<span class="badge badge--admin">Admin</span>`
            : `<span class="badge badge--user">User</span>`;
        const statusBadge   = u.active
            ? `<span class="badge badge--active">Active</span>`
            : `<span class="badge badge--inactive">Inactive</span>`;

        const promoteBtn = u.role !== ROLE_ADMIN
            ? `<button class="btn-action-sm btn-promote" data-id="${u.id}" data-name="${escapeHtml(u.username)}">Promote</button>`
            : "";
        const demoteBtn  = u.role === ROLE_ADMIN
            ? `<button class="btn-action-sm btn-demote" data-id="${u.id}" data-name="${escapeHtml(u.username)}">Demote</button>`
            : "";
        const activateBtn   = !u.active
            ? `<button class="btn-action-sm btn-activate" data-id="${u.id}" data-name="${escapeHtml(u.username)}">Activate</button>`
            : "";
        const deactivateBtn = u.active
            ? `<button class="btn-action-sm btn-deactivate" data-id="${u.id}" data-name="${escapeHtml(u.username)}">Deactivate</button>`
            : "";

        return `
        <tr class="${isCurrentUser ? "row--self" : ""}">
            <td>
                <span class="user-name">${escapeHtml(u.username)}</span>
                ${isCurrentUser ? `<span class="self-label">(you)</span>` : ""}
            </td>
            <td>${roleBadge}</td>
            <td>${statusBadge}</td>
            <td>
                <div class="row-actions">
                    ${promoteBtn}
                    ${demoteBtn}
                    ${activateBtn}
                    ${deactivateBtn}
                </div>
            </td>
        </tr>`;
    }).join("");

    // Wire up all action buttons via event delegation
    tbody.querySelectorAll(".btn-promote").forEach(btn =>
        btn.addEventListener("click", () => updateRole(Number(btn.dataset.id), btn.dataset.name, "promote")));
    tbody.querySelectorAll(".btn-demote").forEach(btn =>
        btn.addEventListener("click", () => updateRole(Number(btn.dataset.id), btn.dataset.name, "demote")));
    tbody.querySelectorAll(".btn-activate").forEach(btn =>
        btn.addEventListener("click", () => updateStatus(Number(btn.dataset.id), btn.dataset.name, "activate")));
    tbody.querySelectorAll(".btn-deactivate").forEach(btn =>
        btn.addEventListener("click", () => updateStatus(Number(btn.dataset.id), btn.dataset.name, "deactivate")));
}

// ── Register new user ─────────────────────────────────────────────────────────
async function handleSubmit(e) {
    e.preventDefault();
    if (!validateForm()) return;

    const payload = {
        username: document.getElementById("username").value.trim(),
        password: document.getElementById("password").value,
        role:     document.getElementById("role").value
    };

    setSubmitLoading(true);

    try {
        const res = await fetch(API_USERS, {
            method:  "POST",
            headers: { "Content-Type": "application/json" },
            body:    JSON.stringify(payload)
        });

        if (!res.ok) {
            const err = await res.json();
            throw new Error(err.message || MSG.USER_REGISTER_FAIL);
        }

        showMessage(MSG.USER_REGISTER_SUCCESS, "success");
        resetForm();
        await loadUsers();

    } catch (err) {
        showMessage(err.message || MSG.USER_REGISTER_FAIL, "error");
    } finally {
        setSubmitLoading(false);
    }
}

// ── Promote / Demote ──────────────────────────────────────────────────────────
async function updateRole(id, name, action) {
    try {
        const res = await fetch(`${API_USERS}/${id}/role`, {
            method:  "PATCH",
            headers: { "Content-Type": "application/json" },
            body:    JSON.stringify({ action })
        });

        const data = await res.json();
        if (!res.ok) throw new Error(data.message || MSG.USER_OP_FAIL);

        const successMsg = action === "promote"
            ? MSG.USER_PROMOTE_SUCCESS(name)
            : MSG.USER_DEMOTE_SUCCESS(name);

        showMessage(successMsg, "success");
        await loadUsers();

        // If admin demoted themselves, update localStorage and reload navbar
        if (action === "demote" && name === currentUser.username) {
            currentUser.role = "ROLE_USER";
            localStorage.setItem("currentUser", JSON.stringify(currentUser));
            // Redirect away — they no longer have admin access
            window.location.href = PAGE_DASHBOARD;
        }

    } catch (err) {
        showMessage(err.message || MSG.USER_OP_FAIL, "error");
    }
}

// ── Activate / Deactivate ─────────────────────────────────────────────────────
async function updateStatus(id, name, action) {
    try {
        const res = await fetch(`${API_USERS}/${id}/status`, {
            method:  "PATCH",
            headers: { "Content-Type": "application/json" },
            body:    JSON.stringify({ action })
        });

        const data = await res.json();
        if (!res.ok) throw new Error(data.message || MSG.USER_OP_FAIL);

        const successMsg = action === "activate"
            ? MSG.USER_ACTIVATE_SUCCESS(name)
            : MSG.USER_DEACTIVATE_SUCCESS(name);

        showMessage(successMsg, "success");
        await loadUsers();

    } catch (err) {
        showMessage(err.message || MSG.USER_OP_FAIL, "error");
    }
}

// ── Validation ────────────────────────────────────────────────────────────────
function validateForm() {
    let isValid = true;

    const usernameInput = document.getElementById("username");
    const usernameError = document.getElementById("usernameError");
    if (!usernameInput.value.trim()) {
        usernameInput.classList.add("error");
        usernameError.classList.add("visible");
        isValid = false;
    } else {
        usernameInput.classList.remove("error");
        usernameError.classList.remove("visible");
    }

    const passwordInput = document.getElementById("password");
    const passwordError = document.getElementById("passwordError");
    if (passwordInput.value.length < 6) {
        passwordInput.classList.add("error");
        passwordError.classList.add("visible");
        isValid = false;
    } else {
        passwordInput.classList.remove("error");
        passwordError.classList.remove("visible");
    }

    return isValid;
}

// ── Reset form ────────────────────────────────────────────────────────────────
function resetForm() {
    document.getElementById("userForm").reset();
    document.getElementById("username").classList.remove("error");
    document.getElementById("password").classList.remove("error");
    document.getElementById("usernameError").classList.remove("visible");
    document.getElementById("passwordError").classList.remove("visible");
}

// ── Helpers ───────────────────────────────────────────────────────────────────
function setSubmitLoading(loading) {
    document.getElementById("submitBtn").disabled         = loading;
    document.getElementById("submitText").style.display   = loading ? "none" : "inline";
    document.getElementById("spinner").style.display      = loading ? "inline-block" : "none";
}

function showMessage(text, type) {
    const c = document.getElementById("messageContainer");
    c.innerHTML = `
        <div class="message-banner message-banner--${type}">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                ${type === "success"
        ? `<polyline points="20 6 9 17 4 12"/>`
        : `<circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>`}
            </svg>
            <span>${text}</span>
        </div>`;
    setTimeout(() => { c.innerHTML = ""; }, 4000);
}

function escapeHtml(str) {
    if (!str) return "";
    return str.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;").replace(/'/g, "&#039;");
}
