/* navbar.js — inject shared navbar, role-aware links */

function renderNavbar() {
    const currentUser = (() => {
        try { return JSON.parse(localStorage.getItem("currentUser")); }
        catch { return null; }
    })();

    const isAdmin = currentUser?.role === "ROLE_ADMIN";

    const nav = document.createElement("nav");
    nav.className = "navbar";
    nav.innerHTML = `
    <a class="navbar-brand" href="index.html">
      <span class="navbar-logo">Staff<span class="navbar-logo-accent">Sync</span></span>
      <span class="navbar-tagline">Employee Management System</span>
    </a>

    <div class="navbar-actions">
      ${currentUser ? `
        <a class="nav-btn nav-btn--ghost" href="index.html">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
            <rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/>
            <rect x="14" y="14" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/>
          </svg>
          Employee Dashboard
        </a>

        ${isAdmin ? `
        <a class="nav-btn nav-btn--ghost" href="departments.html">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
            <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/>
            <polyline points="9 22 9 12 15 12 15 22"/>
          </svg>
          Departments
        </a>
        <a class="nav-btn nav-btn--ghost" href="users.html">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
            <circle cx="9" cy="7" r="4"/>
            <path d="M3 21v-2a4 4 0 0 1 4-4h4a4 4 0 0 1 4 4v2"/>
            <line x1="19" y1="8" x2="19" y2="14"/><line x1="16" y1="11" x2="22" y2="11"/>
          </svg>
          Users
        </a>
        ` : ""}

        <div class="navbar-user">
          <span class="navbar-user-info">
            <span class="navbar-username">${currentUser.username}</span>
            <span class="navbar-role">${currentUser.role?.replace("ROLE_", "") ?? "USER"}</span>
          </span>
          <button class="nav-btn nav-btn--logout" id="navLogoutBtn">Logout</button>
        </div>
      ` : `
        <a class="nav-btn nav-btn--ghost" href="login.html">Login</a>
        <a class="nav-btn nav-btn--primary" href="signup.html">Register</a>
      `}
    </div>

    <button class="navbar-hamburger" id="navHamburger" aria-label="Toggle menu">
      <span></span><span></span><span></span>
    </button>
  `;

    document.body.prepend(nav);

    // Logout
    document.getElementById("navLogoutBtn")?.addEventListener("click", () => {
        localStorage.removeItem("currentUser");
        window.location.href = "login.html";
    });

    // Hamburger
    const hamburger = document.getElementById("navHamburger");
    const actions   = nav.querySelector(".navbar-actions");
    hamburger?.addEventListener("click", () => {
        actions.classList.toggle("navbar-actions--open");
        hamburger.classList.toggle("is-open");
    });
}

document.addEventListener("DOMContentLoaded", renderNavbar);
