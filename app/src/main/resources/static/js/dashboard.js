// dashboard.js

const API_BASE_URL = "http://localhost:8080/api/employees";

let allEmployees      = [];
let filteredEmployees = [];
let currentPage       = 1;
let employeesPerPage  = 10;

// ── Auth guard ────────────────────────────────────────────────
const currentUser = (() => {
    try { return JSON.parse(localStorage.getItem("currentUser")); }
    catch { return null; }
})();
if (!currentUser) window.location.href = "login.html";

// ── Init ──────────────────────────────────────────────────────
document.addEventListener("DOMContentLoaded", () => {
    loadEmployees();
    loadGlobalStatistics();

    document.getElementById("searchInput")?.addEventListener("input", applyFilters);
    document.getElementById("departmentFilter")?.addEventListener("change", applyFilters);
    document.getElementById("ageMinInput")?.addEventListener("input", applyFilters);
    document.getElementById("ageMaxInput")?.addEventListener("input", applyFilters);
    document.getElementById("clearFiltersBtn")?.addEventListener("click", clearFilters);
    document.getElementById("calcSelBtn")?.addEventListener("click", computeSelectionStats);
});

// ── Load all employees ────────────────────────────────────────
async function loadEmployees() {
    showTableLoading();
    try {
        const res = await fetch(API_BASE_URL);
        if (!res.ok) throw new Error("Failed to fetch employees");
        allEmployees      = await res.json();
        filteredEmployees = [...allEmployees];
        applyFilters();
    } catch (err) {
        console.error(err);
        showEmpty("Error loading employees. Is the backend running?");
    }
}

// ── Global statistics ─────────────────────────────────────────
async function loadGlobalStatistics() {
    try {
        const [empRes, salaryRes, ageRes] = await Promise.all([
            fetch(API_BASE_URL),
            fetch(`${API_BASE_URL}/statistics/average-salary`),
            fetch(`${API_BASE_URL}/statistics/average-age`)
        ]);

        const employees  = await empRes.json();
        const salaryData = await salaryRes.json();
        const ageData    = await ageRes.json();

        document.getElementById("totalEmployees").textContent = employees.length;

        const avg = salaryData.averageSalary || 0;
        document.getElementById("avgSalary").textContent =
            `₱${avg.toLocaleString("en-PH", { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;

        document.getElementById("avgAge").textContent =
            (ageData.averageAge || 0).toFixed(1);

    } catch (err) {
        console.error("Global stats error:", err);
    }
}

// ── Selection statistics ──────────────────────────────────────
function computeSelectionStats() {
    const section    = document.getElementById("selectionStatsSection");
    const count      = filteredEmployees.length;
    const selCountEl = document.getElementById("selCount");
    const selSalEl   = document.getElementById("selAvgSalary");
    const selAgeEl   = document.getElementById("selAvgAge");
    const infoEl     = document.getElementById("selFilterInfo");

    section.style.display = "block";

    if (!count) {
        selCountEl.textContent = "0";
        selSalEl.textContent   = "—";
        selAgeEl.textContent   = "—";
        infoEl.textContent     = "No employees match the current filters";
        infoEl.className       = "sel-filter-info";
        return;
    }

    const avgSalary = filteredEmployees.reduce((s, e) => s + (e.salary || 0), 0) / count;
    const avgAge    = filteredEmployees.reduce((s, e) => {
        return s + (e.age ?? calculateAge(e.dateOfBirth));
    }, 0) / count;

    selCountEl.textContent = count;
    selSalEl.textContent   =
        `₱${avgSalary.toLocaleString("en-PH", { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
    selAgeEl.textContent   = avgAge.toFixed(1);

    const filters = [];
    const search  = document.getElementById("searchInput")?.value.trim();
    const dept    = document.getElementById("departmentFilter")?.value;
    const ageMin  = document.getElementById("ageMinInput")?.value;
    const ageMax  = document.getElementById("ageMaxInput")?.value;
    if (search) filters.push(`"${search}"`);
    if (dept)   filters.push(dept);
    if (ageMin) filters.push(`age ≥ ${ageMin}`);
    if (ageMax) filters.push(`age ≤ ${ageMax}`);

    const isFiltered = filteredEmployees.length < allEmployees.length;
    infoEl.textContent = isFiltered
        ? `Filtered: ${filters.join(", ")}`
        : `All employees · no active filters`;
    infoEl.className = isFiltered ? "sel-filter-info sel-filter-info--active" : "sel-filter-info";

    document.querySelectorAll(".stat-card--sel").forEach(card => {
        card.classList.remove("card-pop");
        void card.offsetWidth;
        card.classList.add("card-pop");
    });
}

// ── Apply filters ─────────────────────────────────────────────
function applyFilters() {
    const search     = (document.getElementById("searchInput")?.value ?? "").trim().toLowerCase();
    const department = document.getElementById("departmentFilter")?.value ?? "";
    const ageMin     = parseInt(document.getElementById("ageMinInput")?.value) || 0;
    const ageMax     = parseInt(document.getElementById("ageMaxInput")?.value) || 999;

    filteredEmployees = allEmployees.filter(emp => {
        const age = emp.age ?? calculateAge(emp.dateOfBirth);
        return (
            (!search      || emp.name.toLowerCase().includes(search) || emp.employeeId.toLowerCase().includes(search)) &&
            (!department  || emp.department === department) &&
            (age >= ageMin && age <= ageMax)
        );
    });

    updateResultsCount();
    currentPage = 1;                        // reset to first page on every filter change
    renderEmployees(currentPage);
    setupPagination();
}

// ── Clear filters ─────────────────────────────────────────────
function clearFilters() {
    document.getElementById("searchInput").value      = "";
    document.getElementById("departmentFilter").value = "";
    document.getElementById("ageMinInput").value      = "";
    document.getElementById("ageMaxInput").value      = "";
    document.getElementById("selectionStatsSection").style.display = "none";
    applyFilters();
}

// ── Results count ─────────────────────────────────────────────
function updateResultsCount() {
    const el = document.getElementById("resultsCount");
    if (!el) return;
    const total    = allEmployees.length;
    const shown    = filteredEmployees.length;
    el.textContent = shown < total
        ? `Showing ${shown} of ${total} employee${total !== 1 ? "s" : ""}`
        : `${total} employee${total !== 1 ? "s" : ""} total`;
}

// ── Render table (current page slice only) ────────────────────
function renderEmployees(page) {
    const tbody   = document.getElementById("employeeTableBody");
    const emptyEl = document.getElementById("emptyState");
    const tableEl = document.getElementById("employeeTable");

    if (!filteredEmployees.length) {
        tableEl.style.display = "none";
        emptyEl.style.display = "flex";
        return;
    }

    tableEl.style.display = "table";
    emptyEl.style.display = "none";

    // Slice just the rows for this page
    const start      = (page - 1) * employeesPerPage;
    const end        = start + employeesPerPage;
    const pageItems  = filteredEmployees.slice(start, end);

    tbody.innerHTML = pageItems.map(emp => {
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

// ── Pagination ────────────────────────────────────────────────
function setupPagination() {
    const pagination = document.getElementById("pagination");
    if (!pagination) return;
    pagination.innerHTML = "";

    if (!filteredEmployees.length) return;

    const totalPages = Math.ceil(filteredEmployees.length / employeesPerPage);
    if (totalPages <= 1) return;            // no controls needed for a single page

    // Prev button
    const prevBtn = document.createElement("button");
    prevBtn.textContent = "← Prev";
    prevBtn.className   = "page-btn";
    prevBtn.disabled    = currentPage === 1;
    prevBtn.onclick     = () => {
        if (currentPage > 1) {
            currentPage--;
            renderEmployees(currentPage);
            setupPagination();
        }
    };
    pagination.appendChild(prevBtn);

    // Numbered page buttons
    for (let i = 1; i <= totalPages; i++) {
        const btn     = document.createElement("button");
        btn.textContent = i;
        btn.className   = i === currentPage ? "page-btn active-page" : "page-btn";
        btn.onclick     = () => {
            currentPage = i;
            renderEmployees(currentPage);
            setupPagination();
        };
        pagination.appendChild(btn);
    }

    // Next button
    const nextBtn = document.createElement("button");
    nextBtn.textContent = "Next →";
    nextBtn.className   = "page-btn";
    nextBtn.disabled    = currentPage === totalPages;
    nextBtn.onclick     = () => {
        if (currentPage < totalPages) {
            currentPage++;
            renderEmployees(currentPage);
            setupPagination();
        }
    };
    pagination.appendChild(nextBtn);
}

// ── Per-page selector (optional — wire up a <select> in index.html) ──
function changeEmployeesPerPage(val) {
    employeesPerPage = parseInt(val);
    currentPage      = 1;
    renderEmployees(currentPage);
    setupPagination();
}

// ── Delete ────────────────────────────────────────────────────
function openDeleteModal(id, name) {
    document.getElementById("deleteEmployeeName").textContent = name;
    document.getElementById("deleteModal").style.display      = "flex";
    document.getElementById("confirmDeleteBtn").onclick       = () => deleteEmployee(id);
}

function closeDeleteModal() {
    document.getElementById("deleteModal").style.display = "none";
}

async function deleteEmployee(id) {
    try {
        const res = await fetch(`${API_BASE_URL}/${id}`, { method: "DELETE" });
        if (!res.ok) throw new Error("Failed to delete");
        closeDeleteModal();
        showToast("Employee deleted successfully.", "success");
        await loadEmployees();
        await loadGlobalStatistics();
    } catch (err) {
        console.error(err);
        showToast("Failed to delete employee.", "error");
    }
}

// ── Helpers ───────────────────────────────────────────────────
function showTableLoading() {
    const tbody   = document.getElementById("employeeTableBody");
    const tableEl = document.getElementById("employeeTable");
    const emptyEl = document.getElementById("emptyState");
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
    const today = new Date(), birth = new Date(dob);
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
    toast.className   = `toast toast--${type}`;
    toast.textContent = msg;
    document.body.appendChild(toast);
    setTimeout(() => toast.classList.add("toast--visible"), 10);
    setTimeout(() => {
        toast.classList.remove("toast--visible");
        setTimeout(() => toast.remove(), 300);
    }, 3000);
}