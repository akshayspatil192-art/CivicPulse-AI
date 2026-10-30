const API_BASE = '/api';

const Api = {
  getToken() {
    return localStorage.getItem('civicpulse_token');
  },
  
  getUser() {
    const u = localStorage.getItem('civicpulse_user');
    return u ? JSON.parse(u) : null;
  },

  setAuth(token, user) {
    localStorage.setItem('civicpulse_token', token);
    localStorage.setItem('civicpulse_user', JSON.stringify(user));
  },

  clearAuth() {
    localStorage.removeItem('civicpulse_token');
    localStorage.removeItem('civicpulse_user');
  },

  async request(endpoint, options = {}) {
    const url = API_BASE + endpoint;
    const headers = options.headers || {};

    const token = this.getToken();
    if (token) {
      headers['Authorization'] = 'Bearer ' + token;
    }

    if (!options.isFormData) {
      headers['Content-Type'] = 'application/json';
    }

    const config = {
      ...options,
      headers
    };

    try {
      const response = await fetch(url, config);
      const data = await response.json().catch(() => ({}));

      if (!response.ok) {
        throw new Error(data.message || 'Server returned status ' + response.status);
      }
      return data;
    } catch (err) {
      console.error('API Error (' + endpoint + '):', err);
      throw err;
    }
  },

  // Auth
  login(email, password) {
    return this.request('/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email, password })
    });
  },

  register(data) {
    return this.request('/auth/register', {
      method: 'POST',
      body: JSON.stringify(data)
    });
  },

  // Complaints
  getComplaints(params = {}) {
    const query = new URLSearchParams();
    if (params.category) query.append('category', params.category);
    if (params.priority) query.append('priority', params.priority);
    if (params.status) query.append('status', params.status);
    if (params.search) query.append('search', params.search);
    const qs = query.toString();
    return this.request('/complaints' + (qs ? '?' + qs : ''));
  },

  getComplaint(id) {
    return this.request('/complaints/' + id);
  },

  trackComplaint(trackingId) {
    return this.request('/complaints/track/' + encodeURIComponent(trackingId));
  },

  getCitizenComplaints(citizenId) {
    return this.request('/complaints/citizen/' + citizenId);
  },

  submitComplaint(formData) {
    return this.request('/complaints', {
      method: 'POST',
      isFormData: true,
      body: formData
    });
  },

  previewAi(data) {
    return this.request('/complaints/analyze', {
      method: 'POST',
      body: JSON.stringify(data)
    });
  },

  updateStatus(complaintId, status, remarks) {
    const user = this.getUser();
    const userId = user ? user.id : 1;
    return this.request('/complaints/' + complaintId + '/status?userId=' + userId, {
      method: 'PATCH',
      body: JSON.stringify({ status, remarks })
    });
  },

  assignDepartment(complaintId, departmentId, notes) {
    const user = this.getUser();
    const userId = user ? user.id : 1;
    return this.request('/complaints/' + complaintId + '/assign?userId=' + userId, {
      method: 'POST',
      body: JSON.stringify({ departmentId, notes })
    });
  },

  updatePriority(complaintId, priority, reason) {
    const user = this.getUser();
    const userId = user ? user.id : 1;
    return this.request('/complaints/' + complaintId + '/priority?priority=' + priority + '&reason=' + encodeURIComponent(reason) + '&userId=' + userId, {
      method: 'PATCH'
    });
  },

  // Departments
  getDepartments() {
    return this.request('/departments');
  },

  // Dashboard & Hotspots
  getMetrics() {
    return this.request('/dashboard/metrics');
  },

  getHotspots() {
    return this.request('/hotspots');
  }
};

// UI Helpers
const UI = {
  priorityBadge(priority) {
    if (!priority) return '<span class="badge bg-secondary">UNKNOWN</span>';
    const cls = 'badge-priority-' + priority.toLowerCase();
    return `<span class="badge ${cls}"><i class="bi bi-shield-fill-exclamation me-1"></i>${priority}</span>`;
  },

  statusBadge(status) {
    if (!status) return '<span class="badge bg-secondary">UNKNOWN</span>';
    const cls = 'badge-status-' + status.toLowerCase();
    const display = status.replace('_', ' ');
    return `<span class="badge ${cls}">${display}</span>`;
  },

  categoryIcon(category) {
    switch (category) {
      case 'ROAD_DAMAGE': return 'bi-cone-striped text-warning';
      case 'WATERLOGGING': return 'bi-water text-primary';
      case 'GARBAGE_OVERFLOW': return 'bi-trash3 text-danger';
      case 'DRAINAGE_PROBLEMS': return 'bi-water text-secondary';
      case 'STREETLIGHT_PROBLEMS': return 'bi-lightbulb text-warning';
      case 'WATER_SUPPLY': return 'bi-droplet-half text-info';
      default: return 'bi-exclamation-triangle text-muted';
    }
  },

  formatDate(dateStr) {
    if (!dateStr) return 'N/A';
    const d = new Date(dateStr);
    return d.toLocaleString('en-US', {
      month: 'short', day: 'numeric', year: 'numeric',
      hour: '2-digit', minute: '2-digit'
    });
  },

  showToast(message, type = 'info') {
    let container = document.getElementById('toast-container');
    if (!container) {
      container = document.createElement('div');
      container.id = 'toast-container';
      container.className = 'toast-container position-fixed bottom-0 end-0 p-3';
      container.style.zIndex = '9999';
      document.body.appendChild(container);
    }
    const bg = type === 'success' ? 'bg-success text-white' : type === 'error' ? 'bg-danger text-white' : 'bg-primary text-white';
    const toastEl = document.createElement('div');
    toastEl.className = `toast align-items-center ${bg} border-0 show`;
    toastEl.role = 'alert';
    toastEl.innerHTML = `
      <div class="d-flex">
        <div class="toast-body"><i class="bi bi-info-circle me-2"></i>${message}</div>
        <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
      </div>
    `;
    container.appendChild(toastEl);
    setTimeout(() => toastEl.remove(), 4000);
  }
};
