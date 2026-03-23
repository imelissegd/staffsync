// departments.js
import { API_DEPTS, PAGE_LOGIN, PAGE_DASHBOARD, ROLE_ADMIN, MSG } from "./config.js";

// Auth guard — admin only
const currentUser = (() => {
    try { return JSON.parse(localStorage.getItem("currentUser")); }
    catch { return null; }
})();
if (!currentUser)                    window.location.href = PAGE_LOGIN;
if (currentUser.role !== ROLE_ADMIN) window.location.href = PAGE_DASHBOARD;

let departments     = [];
let pendingDeleteId = null;

document.addEventListener("DOMContentLoaded", () => {
    loadDepartments();
    document.getElementById("deptForm").addEventListener("submit", handleSubmit);
});

// ── Load all departments ──────────────────────────────────────────────────────
async function loadDepartments() {
    try {
        const res = await fetch(API_DEPTS);
        if (!res.ok) throw new Error(MSG.DEPT_LOAD_FAIL);
        departments = await res.json();
        renderTable();
    } catch {
        document.getElementById("deptTableBody").innerHTML =
            `<tr><td class="state-cell" colspan="2">${MSG.DEPT_LOAD_FAIL}</td></tr>`;
    }
}

// ── Render table ──────────────────────────────────────────────────────────────
function renderTable() {
    const tbody   = document.getElementById("deptTableBody");
    const countEl = document.getElementById("deptCount");

    countEl.textContent = `${departments.length} department${departments.length !== 1 ? "s" : ""}`;

    if (departments.length === 0) {
        tbody.innerHTML = `<tr><td class="state-cell" colspan="2">No departments yet. Add one using the form.</td></tr>`;
        return;
    }

    tbody.innerHTML = departments.map(d => `
        <tr>
            <td>
                <div class="dept-name">${escapeHtml(d.name)}</div>
                ${d.description
        ? `<div class="dept-desc">${escapeHtml(d.description)}</div>`
        : ""}
            </td>
            <td>
                <div class="row-actions">
                    <button class="btn-edit"   data-id="${d.id}">Edit</button>
                    <button class="btn-delete" data-id="${d.id}" data-name="${escapeHtml(d.name)}">Delete</button>
                </div>
            </td>
        </tr>
    `).join("");

    tbody.querySelectorAll(".btn-edit").forEach(btn => {
        btn.addEventListener("click", () => startEdit(Number(btn.dataset.id)));
    });
    tbody.querySelectorAll(".btn-delete").forEach(btn => {
        btn.addEventListener("click", () => confirmDelete(Number(btn.dataset.id), btn.dataset.name));
    });
}

// ── Create / Update ───────────────────────────────────────────────────────────
async function handleSubmit(e) {
    e.preventDefault();
    if (!validateForm()) return;

    const editingIdRaw = document.getElementById("editingId").value;
    const editingId    = editingIdRaw && editingIdRaw !== "null" ? Number(editingIdRaw) : null;
    const payload = {
        name:        document.getElementById("deptName").value.trim(),
        description: document.getElementById("deptDesc").value.trim() || null
    };

    setSubmitLoading(true);

    try {
        const url    = editingId ? `${API_DEPTS}/${editingId}` : API_DEPTS;
        const method = editingId ? "PUT" : "POST";

        const res = await fetch(url, {
            method,
            headers: { "Content-Type": "application/json" },
            body:    JSON.stringify(payload)
        });

        if (!res.ok) {
            const err = await res.json();
            throw new Error(err.message || MSG.DEPT_OP_FAIL);
        }

        showMessage(
            editingId ? MSG.DEPT_UPDATE_SUCCESS : MSG.DEPT_ADD_SUCCESS,
            "success"
        );
        resetForm();
        await loadDepartments();

    } catch (err) {
        showMessage(err.message || MSG.DEPT_OP_FAIL, "error");
    } finally {
        setSubmitLoading(false);
    }
}

// ── Edit ──────────────────────────────────────────────────────────────────────
function startEdit(id) {
    const dept = departments.find(d => d.id === id);
    if (!dept || !dept.id) return;

    document.getElementById("editingId").value             = String(dept.id);
    document.getElementById("deptName").value              = dept.name;
    document.getElementById("deptDesc").value              = dept.description || "";
    document.getElementById("formTitle").textContent       = "Edit Department";
    document.getElementById("submitText").textContent      = "Save Changes";
    document.getElementById("cancelBtn").classList.add("visible");

    document.querySelector(".form-card").scrollIntoView({ behavior: "smooth", block: "start" });
}

// ── Delete ────────────────────────────────────────────────────────────────────
function confirmDelete(id, name) {
    pendingDeleteId = id;
    document.getElementById("deleteModalMsg").textContent =
        `Are you sure you want to delete "${name}"? This cannot be undone.`;
    document.getElementById("deleteModal").classList.add("active");
    document.getElementById("confirmDeleteBtn").onclick = executeDelete;
}

function closeDeleteModal() {
    pendingDeleteId = null;
    document.getElementById("deleteModal").classList.remove("active");
}

async function executeDelete() {
    const idToDelete = pendingDeleteId;
    if (!idToDelete || isNaN(idToDelete)) return;
    closeDeleteModal();

    try {
        const res = await fetch(`${API_DEPTS}/${idToDelete}`, { method: "DELETE" });

        if (!res.ok) {
            const err = await res.json();
            throw new Error(err.message || MSG.DEPT_DELETE_FAIL);
        }

        showMessage(MSG.DEPT_DELETE_SUCCESS, "success");
        await loadDepartments();

    } catch (err) {
        showMessage(err.message || MSG.DEPT_DELETE_FAIL, "error");
    }
}

// ── Reset form ────────────────────────────────────────────────────────────────
function resetForm() {
    document.getElementById("deptForm").reset();
    document.getElementById("editingId").value        = "";
    document.getElementById("formTitle").textContent  = "Add Department";
    document.getElementById("submitText").textContent = "Add Department";
    document.getElementById("cancelBtn").classList.remove("visible");
    document.getElementById("deptName").classList.remove("error");
    document.getElementById("deptNameError").classList.remove("visible");
}

// ── Validation ────────────────────────────────────────────────────────────────
function validateForm() {
    const nameInput = document.getElementById("deptName");
    const nameError = document.getElementById("deptNameError");
    const valid     = nameInput.value.trim() !== "";

    if (!valid) {
        nameInput.classList.add("error");
        nameError.classList.add("visible");
    } else {
        nameInput.classList.remove("error");
        nameError.classList.remove("visible");
    }
    return valid;
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
        ? "<polyline points=\"20 6 9 17 4 12\"/>"
        : "<circle cx=\"12\" cy=\"12\" r=\"10\"/><line x1=\"12\" y1=\"8\" x2=\"12\" y2=\"12\"/><line x1=\"12\" y1=\"16\" x2=\"12.01\" y2=\"16\"/>"}
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

// Expose modal close to onclick in HTML
window.closeDeleteModal = closeDeleteModal;
window.resetForm        = resetForm;
