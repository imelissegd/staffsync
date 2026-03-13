// edit-employee.js

const API_BASE_URL = "http://localhost:8080/api/employees";

// Auth guard
const currentUser = (() => {
    try { return JSON.parse(localStorage.getItem("currentUser")); }
    catch { return null; }
})();
if (!currentUser) window.location.href = "login.html";

let employeeId = null;

document.addEventListener("DOMContentLoaded", () => {
    const params = new URLSearchParams(window.location.search);
    employeeId = params.get("id");

    if (!employeeId) {
        alert("No employee ID provided.");
        window.location.href = "index.html";
        return;
    }

    loadEmployee();
    document.getElementById("employeeForm").addEventListener("submit", handleSubmit);
});

async function loadEmployee() {
    try {
        const res = await fetch(`${API_BASE_URL}/${employeeId}`);
        if (!res.ok) throw new Error("Employee not found");
        const emp = await res.json();

        document.getElementById("employeeId").value  = emp.employeeId;
        document.getElementById("name").value        = emp.name;
        document.getElementById("dateOfBirth").value = emp.dateOfBirth;
        document.getElementById("department").value  = emp.department;
        document.getElementById("salary").value      = emp.salary;
        document.getElementById("pageSubtitle").textContent = `Editing: ${emp.name}`;

        document.getElementById("loadingState").style.display  = "none";
        document.getElementById("formContainer").style.display = "block";
    } catch (err) {
        console.error(err);
        showMessage("Failed to load employee data.", "error");
        setTimeout(() => window.location.href = "index.html", 2000);
    }
}

async function handleSubmit(e) {
    e.preventDefault();
    if (!validateForm()) return;

    const employee = {
        employeeId: document.getElementById("employeeId").value.trim(),
        name:       document.getElementById("name").value.trim(),
        dateOfBirth: document.getElementById("dateOfBirth").value,
        department: document.getElementById("department").value,
        salary:     parseFloat(document.getElementById("salary").value)
    };

    const submitBtn  = document.getElementById("submitBtn");
    const submitText = document.getElementById("submitText");
    const spinner    = document.getElementById("spinner");

    submitBtn.disabled    = true;
    submitText.style.display = "none";
    spinner.style.display    = "inline-block";

    try {
        const res = await fetch(`${API_BASE_URL}/${employeeId}`, {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(employee)
        });

        if (!res.ok) {
            const err = await res.json();
            throw new Error(err.message || "Failed to update employee");
        }

        showMessage("Employee updated successfully!", "success");
        setTimeout(() => window.location.href = "index.html", 1800);

    } catch (err) {
        console.error(err);
        showMessage(err.message || "Failed to update employee. Please try again.", "error");
        submitBtn.disabled    = false;
        submitText.style.display = "inline";
        spinner.style.display    = "none";
    }
}

function validateForm() {
    let isValid = true;

    const fields = [
        { id: "employeeId", errId: "employeeIdError",
          test: v => v.trim() !== "" },
        { id: "name", errId: "nameError",
          test: v => v.trim() !== "" },
        { id: "dateOfBirth", errId: "dateOfBirthError",
          test: v => { if (!v) return false; const a = calculateAge(v); return a >= 18 && a <= 100; } },
        { id: "department", errId: "departmentError",
          test: v => v !== "" },
        { id: "salary", errId: "salaryError",
          test: v => parseFloat(v) > 0 }
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
    const today = new Date();
    const birth = new Date(dob);
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