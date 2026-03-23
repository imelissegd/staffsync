// config.js — Single source of truth for all URLs and UI messages.

// ── API Base URLs ─────────────────────────────────────────────────────────────
export const API_BASE      = "http://localhost:8080";
export const API_AUTH      = `${API_BASE}/api/auth`;
export const API_EMPLOYEES = `${API_BASE}/api/employees`;
export const API_DEPTS     = `${API_BASE}/api/departments`;
export const API_USERS     = `${API_BASE}/api/users`;

// ── Auth endpoint paths ───────────────────────────────────────────────────────
export const URL_AUTH_REGISTER = `${API_AUTH}/register`;
export const URL_AUTH_LOGIN    = `${API_AUTH}/login`;

// ── Employee endpoint paths ───────────────────────────────────────────────────
export const URL_EMP_AVG_SALARY = `${API_EMPLOYEES}/statistics/average-salary`;
export const URL_EMP_AVG_AGE    = `${API_EMPLOYEES}/statistics/average-age`;

// ── Page URLs ─────────────────────────────────────────────────────────────────
export const PAGE_LOGIN      = "login.html";
export const PAGE_DASHBOARD  = "index.html";
export const PAGE_ADD_EMP    = "add-employee.html";
export const PAGE_EDIT_EMP   = "edit-employee.html";
export const PAGE_DEPTS      = "departments.html";
export const PAGE_USERS      = "users.html";

// ── Roles ─────────────────────────────────────────────────────────────────────
export const ROLE_ADMIN = "ROLE_ADMIN";
export const ROLE_USER  = "ROLE_USER";

// ── UI Messages ───────────────────────────────────────────────────────────────
export const MSG = {
    // Auth
    AUTH_REGISTERING:           "Registering…",
    AUTH_LOGGING_IN:            "Logging in…",
    AUTH_REGISTER_SUCCESS:      "Account created! Redirecting to login…",
    AUTH_REGISTER_FAIL:         "Registration failed.",
    AUTH_LOGIN_FAIL:            "Invalid username or password.",
    AUTH_PASSWORD_TOO_SHORT:    "Password must be at least 6 characters.",
    AUTH_SERVER_UNREACHABLE:    "Could not reach the server. Make sure the backend is running.",

    // Employee CRUD
    EMP_ADD_SUCCESS:            "Employee added successfully!",
    EMP_ADD_FAIL:               "Failed to add employee. Please try again.",
    EMP_UPDATE_SUCCESS:         "Employee updated successfully!",
    EMP_UPDATE_FAIL:            "Failed to update employee. Please try again.",
    EMP_DELETE_SUCCESS:         "Employee deleted successfully.",
    EMP_DELETE_FAIL:            "Failed to delete employee.",
    EMP_LOAD_FAIL:              "Failed to load employee data.",

    // Department CRUD
    DEPT_ADD_SUCCESS:           "Department added successfully!",
    DEPT_UPDATE_SUCCESS:        "Department updated successfully!",
    DEPT_DELETE_SUCCESS:        "Department deleted successfully!",
    DEPT_DELETE_FAIL:           "Failed to delete department.",
    DEPT_LOAD_FAIL:             "Failed to load departments. Is the backend running?",
    DEPT_OP_FAIL:               "An error occurred. Please try again.",

    // User management
    USER_REGISTER_SUCCESS:      "User registered successfully!",
    USER_REGISTER_FAIL:         "Failed to register user. Please try again.",
    USER_LOAD_FAIL:             "Failed to load users. Is the backend running?",
    USER_OP_FAIL:               "An error occurred. Please try again.",
    USER_PROMOTE_SUCCESS:       (name) => `${name} has been promoted to Admin.`,
    USER_DEMOTE_SUCCESS:        (name) => `${name} has been demoted to User.`,
    USER_ACTIVATE_SUCCESS:      (name) => `${name} has been activated.`,
    USER_DEACTIVATE_SUCCESS:    (name) => `${name} has been deactivated.`,

    // Dashboard / general
    DASH_LOAD_FAIL:             "Error loading employees. Is the backend running?",
    NO_EMPLOYEES_EXPORT:        "No employees to export.",
    NO_EMPLOYEES_VIEW:          "No employees in the current view.",
    DROP_CSV_ONLY:              "Please drop a .csv file.",

    // Loading states
    LOADING_DEPTS:              "Loading departments…",
    LOADING_EMPLOYEE:           "Loading employee data…",
    LOADING_EMPLOYEES:          "Loading employees…",
    DEPT_LOAD_DROPDOWN_FAIL:    "⚠ Failed to load departments",

    // Edit page
    EDIT_NO_ID:                 "No employee ID provided.",

    // Access control
    ACCESS_DENIED:              "Access denied. Redirecting…",

    // Export
    EXPORT_SUCCESS:             (count) =>
        `Exported ${count} employee${count !== 1 ? "s" : ""} successfully.`,

    // Import
    IMPORT_EMPTY_FILE:          "File is empty or has no data rows.",
    IMPORT_FIX_HEADERS:         "Fix the column headers and re-upload your file.",
};
