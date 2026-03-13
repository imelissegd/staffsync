// dashboard.js — Employee Dashboard with filters

const API_BASE_URL = "http://localhost:8080/api/employees";

let allEmployees = [];
let filteredEmployees = [];

// ── Auth guard ────────────────────────────────────────────────
const currentUser = (() => {
    try { return JSON.parse(localStorage.getItem("currentUser")); }
    catch { return null; }
})();

if (!currentUser) {
    window.location.href = "login.html";
}

// ── Init ──────────────────────────────────────────────────────
document.addEventListener("DOMContentLoaded", () => {
    loadEmployees();
    loadStatistics();

    // Wire up filter inputs
    document.getElementById("searchInput")?.addEventListener("input", applyFilters);
    document.getElementById("departmentFilter")?.addEventListener("change", applyFilters);
    document.getElementById("ageMinInput")?.addEventListener("input", applyFilters);
    document.getElementById("ageMaxInput")?.addEventListener("input", applyFilters);
    document.getElementById("clearFiltersBtn")?.addEventListener("click", clearFilters);
});

// ── Load employees ────────────────────────────────────────────
async function loadEmployees() {
    showTableLoading();
    try {
        const res = await fetch(API_BASE_URL);
        if (!res.ok) throw new Error("Failed to fetch employees");
        allEmployees = await res.json();
        filteredEmployees = [...allEmployees];
        applyFilters();
    } catch (err) {
        console.error(err);
        showEmpty("Error loading employees. Is the backend running?");
    }
}

// ── Load statistics ───────────────────────────────────────────
async function loadStatistics() {
    try {
        const [empRes, salaryRes, ageRes] = await Promise.all([
            fetch(API_BASE_URL),
            fetch(`${API_BASE_URL}/statistics/average-salary`),
            fetch(`${API_BASE_URL}/statistics/average-age`)
        ]);

        const employees = await empRes.json();
        document.getElementById("totalEmployees").textContent = employees.length;

        const salaryData = await salaryRes.json();
        const avgSalary = salaryData.averageSalary || 0;
        document.getElementById("avgSalary").textContent =
            `₱${avgSalary.toLocaleString("en-PH", { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;

        const ageData = await ageRes.json();
        const avgAge = ageData.averageAge || 0;
        document.getElementById("avgAge").textContent = avgAge.toFixed(1);
    } catch (err) {
        console.error("Stats error:", err);
    }
}

// ── Apply all filters ─────────────────────────────────────────
function applyFilters() {
    const search     = (document.getElementById("searchInput")?.value ?? "").trim().toLowerCase();
    const department = document.getElementById("departmentFilter")?.value ?? "";
    const ageMin     = parseInt(document.getElementById("ageMinInput")?.value) || 0;
    const ageMax     = parseInt(document.getElementById("ageMaxInput")?.value) || 999;

    filteredEmployees = allEmployees.filter(emp => {
        const age = emp.age ?? calculateAge(emp.dateOfBirth);

        const matchesSearch =
            !search ||
            emp.name.toLowerCase().includes(search) ||
            emp.employeeId.toLowerCase().includes(search);

        const matchesDept =
            !department || emp.department === department;

        const matchesAge =
            age >= ageMin && age <= ageMax;

        return matchesSearch && matchesDept && matchesAge;
    });

    updateResultsCount();
    renderEmployees(filteredEmployees);
}

// ── Clear all filters ─────────────────────────────────────────
function clearFilters() {
    document.getElementById("searchInput").value    = "";
    document.getElementById("departmentFilter").value = "";
    document.getElementById("ageMinInput").value    = "";
    document.getElementById("ageMaxInput").value    = "";
    applyFilters();
}

// ── Update results count ──────────────────────────────────────
function updateResultsCount() {
    const el = document.getElementById("resultsCount");
    if (!el) return;
    const total  = allEmployees.length;
    const shown  = filteredEmployees.length;
    const filtered = shown < total;
    el.textContent = filtered
        ? `Showing ${shown} of ${total} employee${total !== 1 ? "s" : ""}`
        : `${total} employee${total !== 1 ? "s" : ""} total`;
}

// ── Render table ──────────────────────────────────────────────
function renderEmployees(employees) {
    const tbody    = document.getElementById("employeeTableBody");
    const emptyEl  = document.getElementById("emptyState");
    const tableEl  = document.getElementById("employeeTable");

    if (!employees.length) {
        tableEl.style.display = "none";
        emptyEl.style.display = "flex";
        return;
    }

    tableEl.style.display = "table";
    emptyEl.style.display = "none";

    tbody.innerHTML = employees.map(emp => {
        const age = emp.age ?? calculateAge(emp.dateOfBirth);
        return `
        <tr>
            <td><strong>${emp.employeeId}</strong></td>
            <td>${emp.name}</td>
            <td>${formatDate(emp.dateOfBirth)}</td>
            <td>${age}</td>
            <td><span class="dept-badge dept-badge--${emp.department.toLowerCase()}">${emp.department}</span></td>
            <td>₱${emp.salary.toLocaleString("en-PH", { minimumFractionDigits: 2 })}</td>
            <td>
                <div style="display:flex;gap:0.4rem">
                    <a href="edit-employee.html?id=${emp.id}" class="tbl-btn tbl-btn--ghost">Edit</a>
                    <button onclick="openDeleteModal(${emp.id}, '${escapeHtml(emp.name)}')"
                            class="tbl-btn tbl-btn--danger">Delete</button>
                </div>
            </td>
        </tr>`;
    }).join("");
}

// ── Delete ────────────────────────────────────────────────────
let employeeToDelete = null;

function openDeleteModal(id, name) {
    employeeToDelete = id;
    document.getElementById("deleteEmployeeName").textContent = name;
    document.getElementById("deleteModal").style.display = "flex";
    document.getElementById("confirmDeleteBtn").onclick = () => deleteEmployee(id);
}

function closeDeleteModal() {
    document.getElementById("deleteModal").style.display = "none";
    employeeToDelete = null;
}

async function deleteEmployee(id) {
    try {
        const res = await fetch(`${API_BASE_URL}/${id}`, { method: "DELETE" });
        if (!res.ok) throw new Error("Failed to delete");
        closeDeleteModal();
        showToast("Employee deleted successfully.", "success");
        await loadEmployees();
        await loadStatistics();
    } catch (err) {
        console.error(err);
        showToast("Failed to delete employee.", "error");
    }
}

// ── Helpers ───────────────────────────────────────────────────
function showTableLoading() {
    const tbody  = document.getElementById("employeeTableBody");
    const tableEl = document.getElementById("employeeTable");
    const emptyEl = document.getElementById("emptyState");
    tableEl.style.display = "none";
    emptyEl.style.display = "none";
    if (tbody) tbody.innerHTML = `<tr><td colspan="7" class="table-loading">Loading employees…</td></tr>`;
    tableEl.style.display = "table";
}

function showEmpty(msg) {
    document.getElementById("employeeTable").style.display = "none";
    const el = document.getElementById("emptyState");
    el.style.display = "flex";
    el.querySelector("p").textContent = msg;
}

function formatDate(dateStr) {
    return new Date(dateStr).toLocaleDateString("en-PH", {
        year: "numeric", month: "short", day: "numeric"
    });
}

function calculateAge(dob) {
    const today = new Date();
    const birth = new Date(dob);
    let age = today.getFullYear() - birth.getFullYear();
    const m = today.getMonth() - birth.getMonth();
    if (m < 0 || (m === 0 && today.getDate() < birth.getDate())) age--;
    return age;
}

function escapeHtml(str) {
    return str.replace(/'/g, "\\'").replace(/"/g, "&quot;");
}

function showToast(msg, type = "success") {
    const toast = document.createElement("div");
    toast.className = `toast toast--${type}`;
    toast.textContent = msg;
    document.body.appendChild(toast);
    setTimeout(() => toast.classList.add("toast--visible"), 10);
    setTimeout(() => {
        toast.classList.remove("toast--visible");
        setTimeout(() => toast.remove(), 300);
    }, 3000);
}