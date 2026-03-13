// Add Employee JavaScript

const API_BASE_URL = 'http://localhost:8080/api/employees';

document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('employeeForm');
    form.addEventListener('submit', handleSubmit);
});

async function handleSubmit(e) {
    e.preventDefault();
    
    // Validate form
    if (!validateForm()) {
        return;
    }
    
    // Get form data
    const employee = {
        employeeId: document.getElementById('employeeId').value.trim(),
        name: document.getElementById('name').value.trim(),
        dateOfBirth: document.getElementById('dateOfBirth').value,
        department: document.getElementById('department').value,
        salary: parseFloat(document.getElementById('salary').value)
    };
    
    // Show loading state
    const submitBtn = document.getElementById('submitBtn');
    const submitText = document.getElementById('submitText');
    const spinner = document.getElementById('spinner');
    
    submitBtn.disabled = true;
    submitText.style.display = 'none';
    spinner.style.display = 'block';
    
    try {
        const response = await fetch(API_BASE_URL, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(employee)
        });
        
        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.message || 'Failed to add employee');
        }
        
        const savedEmployee = await response.json();
        
        // Show success message
        showMessage('Employee added successfully!', 'success');
        
        // Reset form
        document.getElementById('employeeForm').reset();
        
        // Redirect after 2 seconds
        setTimeout(() => {
            window.location.href = 'index.html';
        }, 2000);
        
    } catch (error) {
        console.error('Error adding employee:', error);
        showMessage(error.message || 'Failed to add employee. Please try again.', 'error');
        
        submitBtn.disabled = false;
        submitText.style.display = 'inline';
        spinner.style.display = 'none';
    }
}

function validateForm() {
    let isValid = true;
    
    // Employee ID
    const employeeId = document.getElementById('employeeId').value.trim();
    const employeeIdError = document.getElementById('employeeIdError');
    const employeeIdInput = document.getElementById('employeeId');
    
    if (!employeeId) {
        employeeIdInput.classList.add('error');
        employeeIdError.classList.add('visible');
        isValid = false;
    } else {
        employeeIdInput.classList.remove('error');
        employeeIdError.classList.remove('visible');
    }
    
    // Name
    const name = document.getElementById('name').value.trim();
    const nameError = document.getElementById('nameError');
    const nameInput = document.getElementById('name');
    
    if (!name) {
        nameInput.classList.add('error');
        nameError.classList.add('visible');
        isValid = false;
    } else {
        nameInput.classList.remove('error');
        nameError.classList.remove('visible');
    }
    
    // Date of Birth & Age Validation
    const dateOfBirth = document.getElementById('dateOfBirth').value;
    const dobError = document.getElementById('dateOfBirthError');
    const dobInput = document.getElementById('dateOfBirth');
    
    if (!dateOfBirth) {
        dobInput.classList.add('error');
        dobError.classList.add('visible');
        isValid = false;
    } else {
        const age = calculateAge(dateOfBirth);
        if (age < 18 || age > 100) {
            dobInput.classList.add('error');
            dobError.classList.add('visible');
            isValid = false;
        } else {
            dobInput.classList.remove('error');
            dobError.classList.remove('visible');
        }
    }
    
    // Department
    const department = document.getElementById('department').value;
    const deptError = document.getElementById('departmentError');
    const deptInput = document.getElementById('department');
    
    if (!department) {
        deptInput.classList.add('error');
        deptError.classList.add('visible');
        isValid = false;
    } else {
        deptInput.classList.remove('error');
        deptError.classList.remove('visible');
    }
    
    // Salary
    const salary = parseFloat(document.getElementById('salary').value);
    const salaryError = document.getElementById('salaryError');
    const salaryInput = document.getElementById('salary');
    
    if (!salary || salary <= 0) {
        salaryInput.classList.add('error');
        salaryError.classList.add('visible');
        isValid = false;
    } else {
        salaryInput.classList.remove('error');
        salaryError.classList.remove('visible');
    }
    
    return isValid;
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

function showMessage(text, type) {
    const container = document.getElementById('messageContainer');
    container.innerHTML = `
        <div class="message-banner message-banner--${type}">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                ${type === 'success' 
                    ? '<polyline points="20 6 9 17 4 12"/>' 
                    : '<circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>'}
            </svg>
            <span>${text}</span>
        </div>
    `;
}