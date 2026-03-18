// dashboard.js

const API_BASE_URL = "http://localhost:8080/api/employees";
const DEPT_API_URL = "http://localhost:8080/api/departments";

let allEmployees      = [];
let filteredEmployees = [];
let currentPage       = 1;
let employeesPerPage  = 10;

// Departments fetched from API — used by filter dropdown and CSV import
let allDepartments = [];   // array of { id, name, description }

// ── Auth guard ────────────────────────────────────────────────
const currentUser = (() => {
    try { return JSON.parse(localStorage.getItem("currentUser")); }
    catch { return null; }
})();
if (!currentUser) window.location.href = "login.html";

// ── Init ──────────────────────────────────────────────────────
document.addEventListener("DOMContentLoaded", async () => {
    await loadDepartments();   // populate filter dropdown before employees render
    loadEmployees();
    loadGlobalStatistics();

    document.getElementById("searchInput")?.addEventListener("input", applyFilters);
    document.getElementById("departmentFilter")?.addEventListener("change", applyFilters);
    document.getElementById("ageMinInput")?.addEventListener("input", applyFilters);
    document.getElementById("ageMaxInput")?.addEventListener("input", applyFilters);
    document.getElementById("clearFiltersBtn")?.addEventListener("click", clearFilters);
    document.getElementById("calcSelBtn")?.addEventListener("click", computeSelectionStats);
});

// ── Load departments — populates filter dropdown ──────────────
async function loadDepartments() {
    try {
        const res = await fetch(DEPT_API_URL);
        if (!res.ok) throw new Error();
        allDepartments = await res.json();
    } catch {
        allDepartments = [];
    }

    const select = document.getElementById("departmentFilter");
    if (!select) return;
    // Keep the "All Departments" placeholder, then add one option per dept
    select.innerHTML = `<option value="">All Departments</option>` +
        allDepartments.map(d =>
            `<option value="${escapeHtml(d.name)}">${escapeHtml(d.name)}</option>`
        ).join("");
}

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
        infoEl.innerHTML   = `<span class="sel-filter-badge sel-filter-badge--none">No employees match the current filters</span>`;
        infoEl.className   = "sel-filter-info";
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

    const search  = document.getElementById("searchInput")?.value.trim();
    const dept    = document.getElementById("departmentFilter")?.value;
    const ageMin  = document.getElementById("ageMinInput")?.value;
    const ageMax  = document.getElementById("ageMaxInput")?.value;

    const isFiltered = filteredEmployees.length < allEmployees.length;
    infoEl.className = "sel-filter-info";

    if (!isFiltered) {
        infoEl.innerHTML = `<span class="sel-filter-badge sel-filter-badge--none">All employees · no active filters</span>`;
    } else {
        const badges = [];
        if (search) badges.push(`<span class="sel-filter-badge sel-filter-badge--active">"${search}"</span>`);
        if (dept)   badges.push(`<span class="sel-filter-badge sel-filter-badge--active">${dept}</span>`);
        if (ageMin && ageMax) badges.push(`<span class="sel-filter-badge sel-filter-badge--active">Age ${ageMin}–${ageMax}</span>`);
        else if (ageMin)      badges.push(`<span class="sel-filter-badge sel-filter-badge--active">Age ≥ ${ageMin}</span>`);
        else if (ageMax)      badges.push(`<span class="sel-filter-badge sel-filter-badge--active">Age ≤ ${ageMax}</span>`);
        infoEl.innerHTML = badges.join("");
    }

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
            (!department  || emp.department?.name === department) &&
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
            <td><span class="dept-badge dept-badge--${(emp.department?.name ?? "").toLowerCase()}">${emp.department?.name ?? "—"}</span></td>
            <td>₱${emp.salary.toLocaleString("en-PH", { minimumFractionDigits: 2 })}</td>
            <td>
                <div style="display:flex;gap:0.4rem">
                    <button class="tbl-btn tbl-btn--view"   data-id="${emp.id}">View</button>
                    <a href="edit-employee.html?id=${emp.id}" class="tbl-btn tbl-btn--ghost">Edit</a>
                    <button class="tbl-btn tbl-btn--danger" data-id="${emp.id}" data-name="${escapeHtml(emp.name)}">Delete</button>
                </div>
            </td>
        </tr>`;
    }).join("");

    // Event delegation — avoids id-as-string interpolation in onclick attributes
    tbody.querySelectorAll(".tbl-btn--view").forEach(btn => {
        btn.addEventListener("click", () => openViewModal(Number(btn.dataset.id)));
    });
    tbody.querySelectorAll(".tbl-btn--danger").forEach(btn => {
        btn.addEventListener("click", () => openDeleteModal(Number(btn.dataset.id), btn.dataset.name));
    });
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

// ── Sorting ───────────────────────────────────────────────────
let sortKey = null;
let sortDir = "asc";

function sortTable(key) {
    if (sortKey === key) {
        sortDir = sortDir === "asc" ? "desc" : "asc";
    } else {
        sortKey = key;
        sortDir = "asc";
    }

    filteredEmployees.sort((a, b) => {
        let valA, valB;

        if (key === "age") {
            valA = a.age ?? calculateAge(a.dateOfBirth);
            valB = b.age ?? calculateAge(b.dateOfBirth);
        } else if (key === "salary") {
            valA = a.salary;
            valB = b.salary;
        } else if (key === "dateOfBirth") {
            valA = new Date(a.dateOfBirth);
            valB = new Date(b.dateOfBirth);
        } else if (key === "department") {
            valA = (a.department?.name ?? "").toLowerCase();
            valB = (b.department?.name ?? "").toLowerCase();
        } else {
            valA = (a[key] ?? "").toString().toLowerCase();
            valB = (b[key] ?? "").toString().toLowerCase();
        }

        if (valA < valB) return sortDir === "asc" ? -1 : 1;
        if (valA > valB) return sortDir === "asc" ?  1 : -1;
        return 0;
    });

    updateSortArrows();
    currentPage = 1;
    renderEmployees(currentPage);
    setupPagination();
}

function updateSortArrows() {
    const keys = ["employeeId", "name", "dateOfBirth", "age", "department", "salary"];
    keys.forEach(k => {
        const el = document.getElementById(`sort-${k}`);
        if (!el) return;
        el.className   = "sort-arrow";
        el.textContent = "↕";
        if (k === sortKey) {
            el.classList.add(sortDir);
            el.textContent = sortDir === "asc" ? "↑" : "↓";
        }
    });
}

// ── View modal ────────────────────────────────────────────────
async function openViewModal(id) {
    const modal = document.getElementById("viewModal");
    const body  = document.getElementById("viewModalBody");

    // Show modal with loading state
    body.innerHTML = `<p style="text-align:center;color:var(--muted);padding:2rem 0">Loading…</p>`;
    modal.style.display = "flex";

    try {
        const res = await fetch(`${API_BASE_URL}/${id}`);
        if (!res.ok) throw new Error("Failed to fetch employee");
        const emp = await res.json();
        const age = emp.age ?? calculateAge(emp.dateOfBirth);

        body.innerHTML = `
            <div class="view-field">
                <span class="view-label">Employee ID</span>
                <span class="view-value">${emp.employeeId}</span>
            </div>
            <div class="view-field">
                <span class="view-label">Full Name</span>
                <span class="view-value">${emp.name}</span>
            </div>
            <div class="view-field">
                <span class="view-label">Date of Birth</span>
                <span class="view-value">${formatDate(emp.dateOfBirth)}</span>
            </div>
            <div class="view-field">
                <span class="view-label">Age</span>
                <span class="view-value">${age} years old</span>
            </div>
            <div class="view-field">
                <span class="view-label">Department</span>
                <span class="view-value">
                    <span class="dept-badge dept-badge--${(emp.department?.name ?? "").toLowerCase()}">${emp.department?.name ?? "—"}</span>
                </span>
            </div>
            <div class="view-field">
                <span class="view-label">Salary</span>
                <span class="view-value view-value--salary">₱${emp.salary.toLocaleString("en-PH", { minimumFractionDigits: 2, maximumFractionDigits: 2 })}</span>
            </div>
            <div class="view-modal-actions">
                <a href="edit-employee.html?id=${emp.id}" class="btn-action btn-action--primary" style="font-size:0.82rem;padding:0.5rem 1.1rem">Edit Employee</a>
            </div>`;
    } catch (err) {
        console.error(err);
        body.innerHTML = `<p style="text-align:center;color:var(--error);padding:2rem 0">Failed to load employee data.</p>`;
    }
}

function closeViewModal() {
    document.getElementById("viewModal").style.display = "none";
}

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
// ── Export CSV ────────────────────────────────────────────────
function exportCSV() {
    if (!allEmployees.length) {
        showToast("No employees to export.", "error");
        return;
    }

    const headers = ["employeeId", "name", "dateOfBirth", "department", "salary"];
    const rows = allEmployees.map(emp => [
        emp.employeeId,
        `"${(emp.name || "").replace(/"/g, '""')}"`,
        emp.dateOfBirth,
        emp.department?.name ?? "",
        emp.salary
    ].join(","));

    const csv  = [headers.join(","), ...rows].join("\n");
    const blob = new Blob([csv], { type: "text/csv;charset=utf-8;" });
    const url  = URL.createObjectURL(blob);
    const a    = document.createElement("a");
    a.href     = url;
    a.download = `employees_${new Date().toISOString().slice(0,10)}.csv`;
    a.click();
    URL.revokeObjectURL(url);
    showToast("Exported successfully.", "success");
}

// ── Import CSV — modal open/close ─────────────────────────────
let importFile = null;

function openImportModal() {
    importFile = null;
    document.getElementById("importDropZone").style.display  = "block";
    document.getElementById("importPreview").style.display   = "none";
    document.getElementById("importLog").style.display       = "none";
    document.getElementById("importLogContent").innerHTML    = "";
    const btn = document.getElementById("importBtn");
    btn.disabled    = true;
    btn.textContent = "Upload";
    btn.onclick     = runImport;
    document.getElementById("csvFileInput").value            = "";
    document.getElementById("importModal").style.display     = "flex";
}

function closeImportModal() {
    document.getElementById("importModal").style.display = "none";
}

function handleFileSelect(e) {
    const file = e.target.files[0];
    if (file) setImportFile(file);
}

function handleFileDrop(e) {
    e.preventDefault();
    document.getElementById("importDropZone").classList.remove("dragover");
    const file = e.dataTransfer.files[0];
    if (file && file.name.endsWith(".csv")) setImportFile(file);
    else showToast("Please drop a .csv file.", "error");
}

function setImportFile(file) {
    importFile = file;
    document.getElementById("importDropZone").style.display  = "none";
    document.getElementById("importPreview").style.display   = "block";
    document.getElementById("importFileName").textContent    = file.name;
    const btn = document.getElementById("importBtn");
    btn.disabled    = false;
    btn.textContent = "Upload";
    btn.onclick     = runImport;
    document.getElementById("importLog").style.display       = "none";
    document.getElementById("importLogContent").innerHTML    = "";
}

function clearImportFile() {
    importFile = null;
    document.getElementById("csvFileInput").value            = "";
    document.getElementById("importDropZone").style.display  = "block";
    document.getElementById("importPreview").style.display   = "none";
    const btn = document.getElementById("importBtn");
    btn.disabled    = true;
    btn.textContent = "Upload";
    btn.onclick     = runImport;
    document.getElementById("importLog").style.display       = "none";
    document.getElementById("importLogContent").innerHTML    = "";
}

// ── Import CSV — run ──────────────────────────────────────────
async function runImport() {
    if (!importFile) return;

    const logEl  = document.getElementById("importLogContent");
    const logBox = document.getElementById("importLog");
    const btn    = document.getElementById("importBtn");

    logBox.style.display = "block";
    logEl.innerHTML      = "";
    btn.disabled         = true;
    btn.textContent      = "Importing…";

    const text  = await importFile.text();
    const lines = text.trim().split(/\r?\n/);

    // ── Empty file ────────────────────────────────────────────
    if (lines.length < 2) {
        logEl.innerHTML = `<div class="log-error">✗ File is empty or has no data rows.</div>`;
        setImportBtnState("reupload");
        return;
    }

    // ── Validate headers ──────────────────────────────────────
    const headerRaw = lines[0].split(",").map(h => h.trim().replace(/^"|"$/g, "").toLowerCase());
    const idx = {
        employeeId:  headerRaw.indexOf("employeeid"),
        name:        headerRaw.indexOf("name"),
        dateOfBirth: headerRaw.indexOf("dateofbirth"),
        department:  headerRaw.indexOf("department"),
        salary:      headerRaw.indexOf("salary")
    };
    const missing = Object.entries(idx).filter(([, v]) => v === -1).map(([k]) => k);
    if (missing.length) {
        logEl.innerHTML = `
            <div class="log-error" style="margin-bottom:0.35rem">✗ Missing required column${missing.length > 1 ? "s" : ""}:</div>
            ${missing.map(c => `<div class="log-skip" style="padding-left:1rem">— ${c}</div>`).join("")}
            <div class="log-skip" style="margin-top:0.5rem;font-style:italic">Fix the column headers and re-upload your file.</div>`;
        setImportBtnState("reupload");
        return;
    }

    // ── Process rows ──────────────────────────────────────────
    let ok = 0, skipped = 0, errors = 0;

    for (let i = 1; i < lines.length; i++) {
        const line = lines[i].trim();
        if (!line) continue;

        // Parse quoted CSV fields
        const cols = [];
        let inQ = false, cur = "";
        for (const ch of line) {
            if (ch === '"') { inQ = !inQ; }
            else if (ch === "," && !inQ) { cols.push(cur.trim()); cur = ""; }
            else { cur += ch; }
        }
        cols.push(cur.trim());

        const deptName = cols[idx.department]?.replace(/^"|"$/g, "").trim();
        const deptObj  = allDepartments.find(
            d => d.name.toLowerCase() === (deptName ?? "").toLowerCase()
        );

        const emp = {
            employeeId:  cols[idx.employeeId]?.replace(/^"|"$/g, "").trim(),
            name:        cols[idx.name]?.replace(/^"|"$/g, "").trim(),
            dateOfBirth: cols[idx.dateOfBirth]?.replace(/^"|"$/g, "").trim(),
            // Send the Department object expected by the backend ManyToOne mapping
            department:  deptObj ? { id: deptObj.id } : null,
            salary:      parseFloat(cols[idx.salary])
        };

        const badFields = [];
        if (!emp.employeeId)  badFields.push("employeeId");
        if (!emp.name)        badFields.push("name");
        if (!emp.dateOfBirth) badFields.push("dateOfBirth");
        if (!emp.department)  badFields.push(`department (unknown: "${deptName}")`);
        if (isNaN(emp.salary)) badFields.push("salary");

        if (badFields.length) {
            const label = emp.name || emp.employeeId || `row ${i}`;
            logEl.innerHTML += `<div class="log-skip">Row ${i} — <strong>${label}</strong>: missing or invalid: ${badFields.join(", ")}</div>`;
            skipped++;
            continue;
        }

        try {
            const res = await fetch(API_BASE_URL, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(emp)
            });
            if (res.ok) {
                logEl.innerHTML += `<div class="log-ok">Row ${i} ✓ — ${emp.name} (${emp.employeeId})</div>`;
                ok++;
            } else {
                const err = await res.json();
                logEl.innerHTML += `<div class="log-error">Row ${i} ✗ — ${emp.employeeId}: ${err.message || "failed"}</div>`;
                errors++;
            }
        } catch {
            logEl.innerHTML += `<div class="log-error">Row ${i} ✗ — network error</div>`;
            errors++;
        }

        logEl.scrollTop = logEl.scrollHeight;
    }

    // ── Summary bar ───────────────────────────────────────────
    const hasIssues = skipped > 0 || errors > 0;
    logEl.innerHTML += `
        <div style="margin-top:0.5rem;padding-top:0.5rem;border-top:1px solid var(--border);display:flex;gap:0.5rem;flex-wrap:wrap">
            ${ok      ? `<span style="background:rgba(46,125,82,0.1);color:var(--success,#2e7d52);border:1px solid rgba(46,125,82,0.25);padding:2px 10px;border-radius:20px;font-size:0.78rem;font-weight:600">✓ ${ok} imported</span>` : ""}
            ${skipped ? `<span style="background:var(--offwhite);color:var(--muted);border:1px solid var(--border);padding:2px 10px;border-radius:20px;font-size:0.78rem;font-weight:600">${skipped} skipped</span>` : ""}
            ${errors  ? `<span style="background:rgba(192,57,43,0.08);color:var(--error);border:1px solid rgba(192,57,43,0.2);padding:2px 10px;border-radius:20px;font-size:0.78rem;font-weight:600">✗ ${errors} errors</span>` : ""}
        </div>`;
    logEl.scrollTop = logEl.scrollHeight;

    setImportBtnState(hasIssues ? "reupload" : "done");

    if (ok > 0) {
        await loadEmployees();
        await loadGlobalStatistics();
        showToast(`${ok} employee${ok !== 1 ? "s" : ""} imported.`, "success");
    }
}

// ── Import button state helper ────────────────────────────────
function setImportBtnState(state) {
    const btn = document.getElementById("importBtn");
    btn.disabled = false;
    if (state === "reupload") {
        btn.textContent = "Re-upload File";
        btn.onclick     = () => { clearImportFile(); };
    } else {
        btn.textContent = "Done";
        btn.onclick     = closeImportModal;
    }
}