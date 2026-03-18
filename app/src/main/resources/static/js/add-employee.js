// add-employee.js

const API_BASE_URL  = "http://localhost:8080/api/employees";
const DEPT_API_URL  = "http://localhost:8080/api/departments";

// Auth guard
const currentUser = (() => {
    try { return JSON.parse(localStorage.getItem("currentUser")); }
    catch { return null; }
})();
if (!currentUser) window.location.href = "login.html";

document.addEventListener("DOMContentLoaded", async () => {
    await loadDepartments();
    document.getElementById("employeeForm").addEventListener("submit", handleSubmit);
});

// ── Populate department dropdown from API ─────────────────────────────────────
async function loadDepartments() {
    const select = document.getElementById("department");
    try {
        const res = await fetch(DEPT_API_URL);
        if (!res.ok) throw new Error();
        const departments = await res.json();

        select.innerHTML = `<option value="">Select Department</option>` +
            departments.map(d =>
                `<option value="${d.id}">${escapeHtml(d.name)}</option>`
            ).join("");
    } catch {
        select.innerHTML = `<option value="">⚠ Failed to load departments</option>`;
    }
}

async function handleSubmit(e) {
    e.preventDefault();
    if (!validateForm()) return;

    const deptRaw = document.getElementById("department").value;
    const deptId  = deptRaw ? Number(deptRaw) : null;
    const employee = {
        employeeId:  document.getElementById("employeeId").value.trim(),
        name:        document.getElementById("name").value.trim(),
        dateOfBirth: document.getElementById("dateOfBirth").value,
        department:  deptId ? { id: deptId } : null,
        salary:      parseFloat(document.getElementById("salary").value)
    };

    const submitBtn  = document.getElementById("submitBtn");
    const submitText = document.getElementById("submitText");
    const spinner    = document.getElementById("spinner");

    submitBtn.disabled       = true;
    submitText.style.display = "none";
    spinner.style.display    = "inline-block";

    try {
        const res = await fetch(API_BASE_URL, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(employee)
        });

        if (!res.ok) {
            const err = await res.json();
            throw new Error(err.message || "Failed to add employee");
        }

        showMessage("Employee added successfully!", "success");
        document.getElementById("employeeForm").reset();
        await loadDepartments(); // re-populate after reset clears select
        setTimeout(() => window.location.href = "index.html", 1800);

    } catch (err) {
        console.error(err);
        showMessage(err.message || "Failed to add employee. Please try again.", "error");
        submitBtn.disabled       = false;
        submitText.style.display = "inline";
        spinner.style.display    = "none";
    }
}

function validateForm() {
    let isValid = true;

    const fields = [
        { id: "employeeId",  errId: "employeeIdError",  test: v => v.trim() !== "" },
        { id: "name",        errId: "nameError",        test: v => v.trim() !== "" },
        { id: "dateOfBirth", errId: "dateOfBirthError",
            test: v => { if (!v) return false; const a = calculateAge(v); return a >= 18 && a <= 100; } },
        { id: "department",  errId: "departmentError",  test: v => v !== "" },
        { id: "salary",      errId: "salaryError",      test: v => parseFloat(v) > 0 }
    ];

    fields.forEach(f => {
        const input = document.getElementById(f.id);
        const errEl = document.getElementById(f.errId);
        if (!f.test(input.value)) {
            input.classList.add("error");
            errEl.classList.add("visible");
            isValid = false;
        } else {
            input.classList.remove("error");
            errEl.classList.remove("visible");
        }
    });

    return isValid;
}

function calculateAge(dob) {
    const today = new Date(), birth = new Date(dob);
    let age = today.getFullYear() - birth.getFullYear();
    const m = today.getMonth() - birth.getMonth();
    if (m < 0 || (m === 0 && today.getDate() < birth.getDate())) age--;
    return age;
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
}

function escapeHtml(str) {
    return str.replace(/&/g,"&amp;").replace(/</g,"&lt;").replace(/>/g,"&gt;")
        .replace(/"/g,"&quot;").replace(/'/g,"&#039;");
}