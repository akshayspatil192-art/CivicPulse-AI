document.addEventListener("DOMContentLoaded", () => {
  const user = Api.getUser();
  const authNav = document.getElementById("auth-nav");

  if (authNav) {
    if (user) {
      authNav.innerHTML = `
        <li class="nav-item dropdown">
          <a class="nav-link dropdown-toggle text-white d-flex align-items-center" href="#" role="button" data-bs-toggle="dropdown">
            <div class="rounded-circle d-flex align-items-center justify-content-center me-2 text-white" style="width:32px;height:32px;background:#0d9488;font-weight:700;">
              ${user.fullName.charAt(0).toUpperCase()}
            </div>
            <span>${user.fullName}</span>
            <span class="badge ms-2 ${user.role === 'ADMIN' ? 'bg-danger' : 'bg-info'}">${user.role}</span>
          </a>
          <ul class="dropdown-menu dropdown-menu-end shadow">
            ${user.role === 'ADMIN' 
              ? '<li><a class="dropdown-item" href="admin-dashboard.html"><i class="bi bi-speedometer2 me-2"></i>Admin Dashboard</a></li>' 
              : '<li><a class="dropdown-item" href="citizen-dashboard.html"><i class="bi bi-person-workspace me-2"></i>Citizen Dashboard</a></li>'}
            <li><hr class="dropdown-divider"></li>
            <li><a class="dropdown-item text-danger" href="#" id="logout-btn"><i class="bi bi-box-arrow-right me-2"></i>Sign Out</a></li>
          </ul>
        </li>
      `;
      document.getElementById("logout-btn")?.addEventListener("click", (e) => {
        e.preventDefault();
        Api.clearAuth();
        window.location.href = "login.html";
      });
    } else {
      authNav.innerHTML = `
        <li class="nav-item"><a class="nav-link" href="login.html">Citizen Login</a></li>
        <li class="nav-item"><a class="nav-link" href="admin-login.html"><i class="bi bi-shield-lock me-1"></i>Admin Portal</a></li>
        <li class="nav-item ms-lg-2"><a class="btn btn-sm btn-outline-light px-3" href="register.html">Register</a></li>
      `;
    }
  }

  // Citizen Login Form
  const loginForm = document.getElementById("citizen-login-form");
  if (loginForm) {
    loginForm.addEventListener("submit", async (e) => {
      e.preventDefault();
      const email = document.getElementById("login-email").value.trim();
      const password = document.getElementById("login-password").value;
      const btn = loginForm.querySelector("button[type=submit]");
      btn.disabled = true;
      btn.innerHTML = `<span class="spinner-border spinner-border-sm me-2"></span>Signing in...`;

      try {
        const res = await Api.login(email, password);
        Api.setAuth(res.token, { id: res.id, fullName: res.fullName, email: res.email, role: res.role });
        UI.showToast("Welcome back, " + res.fullName + "!", "success");
        setTimeout(() => {
          if (res.role === 'ADMIN') {
            window.location.href = "admin-dashboard.html";
          } else {
            window.location.href = "citizen-dashboard.html";
          }
        }, 800);
      } catch (err) {
        UI.showToast(err.message, "error");
        btn.disabled = false;
        btn.innerHTML = `Sign In`;
      }
    });

    document.getElementById("fill-citizen-demo")?.addEventListener("click", () => {
      document.getElementById("login-email").value = "citizen@example.com";
      document.getElementById("login-password").value = "Citizen@123";
    });
  }

  // Admin Login Form
  const adminLoginForm = document.getElementById("admin-login-form");
  if (adminLoginForm) {
    adminLoginForm.addEventListener("submit", async (e) => {
      e.preventDefault();
      const email = document.getElementById("admin-email").value.trim();
      const password = document.getElementById("admin-password").value;
      const btn = adminLoginForm.querySelector("button[type=submit]");
      btn.disabled = true;
      btn.innerHTML = `<span class="spinner-border spinner-border-sm me-2"></span>Authenticating...`;

      try {
        const res = await Api.login(email, password);
        if (res.role !== 'ADMIN') {
          throw new Error("Access denied: You do not have municipal administrative privileges.");
        }
        Api.setAuth(res.token, { id: res.id, fullName: res.fullName, email: res.email, role: res.role });
        UI.showToast("Welcome to Municipal Command Center, " + res.fullName, "success");
        setTimeout(() => {
          window.location.href = "admin-dashboard.html";
        }, 800);
      } catch (err) {
        UI.showToast(err.message, "error");
        btn.disabled = false;
        btn.innerHTML = `<i class="bi bi-shield-lock me-2"></i>Access Admin Command Center`;
      }
    });

    document.getElementById("fill-admin-demo")?.addEventListener("click", () => {
      document.getElementById("admin-email").value = "admin@civicpulse.gov";
      document.getElementById("admin-password").value = "Admin@123";
    });
  }

  // Registration Form
  const regForm = document.getElementById("register-form");
  if (regForm) {
    regForm.addEventListener("submit", async (e) => {
      e.preventDefault();
      const fullName = document.getElementById("reg-name").value.trim();
      const email = document.getElementById("reg-email").value.trim();
      const phone = document.getElementById("reg-phone").value.trim();
      const password = document.getElementById("reg-password").value;
      const confirmPassword = document.getElementById("reg-confirm-password").value;

      if (password !== confirmPassword) {
        UI.showToast("Passwords do not match.", "error");
        return;
      }

      const btn = regForm.querySelector("button[type=submit]");
      btn.disabled = true;
      btn.innerHTML = `<span class="spinner-border spinner-border-sm me-2"></span>Registering...`;

      try {
        const res = await Api.register({ fullName, email, phone, password });
        Api.setAuth(res.token, { id: res.id, fullName: res.fullName, email: res.email, role: res.role });
        UI.showToast("Registration successful! Redirecting to dashboard...", "success");
        setTimeout(() => {
          window.location.href = "citizen-dashboard.html";
        }, 1000);
      } catch (err) {
        UI.showToast(err.message, "error");
        btn.disabled = false;
        btn.innerHTML = `Create Citizen Account`;
      }
    });
  }
});
