/* navbar.js — inject shared navbar on every page */

function renderNavbar() {
    const currentUser = (() => {
        try { return JSON.parse(localStorage.getItem("currentUser")); }
        catch { return null; }
    })();

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
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/></svg>
          Dashboard
        </a>
        <a class="nav-btn nav-btn--ghost" href="add-employee.html">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
          Add Employee
        </a>
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
    const logoutBtn = document.getElementById("navLogoutBtn");
    if (logoutBtn) {
        logoutBtn.addEventListener("click", async () => {
            try {
                await fetch("http://localhost:8080/logout", { method: "POST", credentials: "include" });
            } catch (_) {}
            localStorage.removeItem("currentUser");
            window.location.href = "login.html";
        });
    }

    // Hamburger
    const hamburger = document.getElementById("navHamburger");
    const actions = nav.querySelector(".navbar-actions");
    hamburger?.addEventListener("click", () => {
        actions.classList.toggle("navbar-actions--open");
        hamburger.classList.toggle("is-open");
    });
}

document.addEventListener("DOMContentLoaded", renderNavbar);