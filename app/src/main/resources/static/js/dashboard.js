// Employee Dashboard JavaScript

const API_BASE_URL = 'http://localhost:8080/api/employees';

let allEmployees = [];
let filteredEmployees = [];

// Load employees on page load
document.addEventListener('DOMContentLoaded', () => {
    loadEmployees();
    loadStatistics();
    loadDepartments();
});

// Load all employees
async function loadEmployees() {
    try {
        const response = await fetch(API_BASE_URL);
        if (!response.ok) throw new Error('Failed to fetch employees');
        
        allEmployees = await response.json();
        filteredEmployees = [...allEmployees];
        renderEmployees(filteredEmployees);
    } catch (error) {
        console.error('Error loading employees:', error);
        showEmptyState();
    }
}

// Load statistics
async function loadStatistics() {
    try {
        // Get total count
        const countResponse = await fetch(API_BASE_URL);
        const employees = await countResponse.json();
        document.getElementById('totalEmployees').textContent = employees.length;

        // Get average salary
        const salaryResponse = await fetch(`${API_BASE_URL}/statistics/average-salary`);
        const salaryData = await salaryResponse.json();
        const avgSalary = salaryData.averageSalary || 0;
        document.getElementById('avgSalary').textContent = `₱${avgSalary.toLocaleString('en-PH', {minimumFractionDigits: 2, maximumFractionDigits: 2})}`;

        // Get average age
        const ageResponse = await fetch(`${API_BASE_URL}/statistics/average-age`);
        const ageData = await ageResponse.json();
        const avgAge = ageData.averageAge || 0;
        document.getElementById('avgAge').textContent = avgAge.toFixed(1);
    } catch (error) {
        console.error('Error loading statistics:', error);
    }
}

// Load unique departments for filter
function loadDepartments() {
    const departments = [...new Set(allEmployees.map(emp => emp.department))];
    const select = document.getElementById('departmentFilter');
    
    departments.forEach(dept => {
        const option = document.createElement('option');
        option.value = dept;
        option.textContent = dept;
        select.appendChild(option);
    });
}

// Render employees in table
function renderEmployees(employees) {
    const tbody = document.getElementById('employeeTableBody');
    const emptyState = document.getElementById('emptyState');
    const table = document.getElementById('employeeTable');

    if (employees.length === 0) {
        showEmptyState();
        return;
    }

    table.style.display = 'table';
    emptyState.style.display = 'none';
    
    tbody.innerHTML = employees.map(employee => `
        <tr>
            <td><strong>${employee.employeeId}</strong></td>
            <td>${employee.name}</td>
            <td>${formatDate(employee.dateOfBirth)}</td>
            <td>${employee.age || calculateAge(employee.dateOfBirth)}</td>
            <td>${employee.department}</td>
            <td>₱${employee.salary.toLocaleString('en-PH', {minimumFractionDigits: 2})}</td>
            <td>
                <div style="display: flex; gap: 0.5rem;">
                    <a href="edit-employee.html?id=${employee.id}" 
                       class="btn-action btn-action--secondary" 
                       style="padding: 0.5rem 0.9rem; font-size: 0.75rem;">
                        Edit
                    </a>
                    <button onclick="openDeleteModal(${employee.id}, '${employee.name}')" 
                            class="btn-action btn-action--danger" 
                            style="padding: 0.5rem 0.9rem; font-size: 0.75rem;">
                        Delete
                    </button>
                </div>
            </td>
        </tr>
    `).join('');
}

// Show empty state
function showEmptyState() {
    document.getElementById('employeeTable').style.display = 'none';
    document.getElementById('emptyState').style.display = 'block';
}

// Search employees
function searchEmployees() {
    const searchTerm = document.getElementById('searchInput').value.toLowerCase();
    const department = document.getElementById('departmentFilter').value;
    
    filteredEmployees = allEmployees.filter(employee => {
        const matchesSearch = employee.name.toLowerCase().includes(searchTerm) || 
                            employee.employeeId.toLowerCase().includes(searchTerm);
        const matchesDepartment = !department || employee.department === department;
        return matchesSearch && matchesDepartment;
    });
    
    renderEmployees(filteredEmployees);
}

// Filter by department
function filterByDepartment() {
    searchEmployees();
}

// Delete employee
let employeeToDelete = null;

function openDeleteModal(id, name) {
    employeeToDelete = id;
    document.getElementById('deleteModal').style.display = 'flex';
    
    const confirmBtn = document.getElementById('confirmDeleteBtn');
    confirmBtn.onclick = () => deleteEmployee(id);
}

function closeDeleteModal() {
    document.getElementById('deleteModal').style.display = 'none';
    employeeToDelete = null;
}

async function deleteEmployee(id) {
    try {
        const response = await fetch(`${API_BASE_URL}/${id}`, {
            method: 'DELETE'
        });
        
        if (!response.ok) throw new Error('Failed to delete employee');
        
        closeDeleteModal();
        loadEmployees();
        loadStatistics();
        
        showMessage('Employee deleted successfully', 'success');
    } catch (error) {
        console.error('Error deleting employee:', error);
        showMessage('Failed to delete employee', 'error');
    }
}

// Show message
function showMessage(text, type) {
    const message = document.createElement('div');
    message.className = `message-banner message-banner--${type}`;
    message.innerHTML = `
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            ${type === 'success' 
                ? '<polyline points="20 6 9 17 4 12"/>' 
                : '<circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>'}
        </svg>
        <span>${text}</span>
    `;
    
    const main = document.querySelector('.page-main');
    main.insertBefore(message, main.firstChild);
    
    setTimeout(() => message.remove(), 3000);
}

// Utility functions
function formatDate(dateString) {
    const options = { year: 'numeric', month: 'short', day: 'numeric' };
    return new Date(dateString).toLocaleDateString('en-US', options);
}

function calculateAge(dateOfBirth) {
    const today = new Date();
    const birthDate = new Date(dateOfBirth);
    let age = today.getFullYear() - birthDate.getFullYear();
    const monthDiff = today.getMonth() - birthDate.getMonth();
    
    if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthDate.getDate())) {
        age--;
    }
    
    return age;
}