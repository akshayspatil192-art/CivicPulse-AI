let catChart = null;
let prioChart = null;
let statusChart = null;

document.addEventListener("DOMContentLoaded", () => {
  const user = Api.getUser();
  if (!user || user.role !== 'ADMIN') {
    UI.showToast("Admin authentication required. Redirecting to login...", "error");
    setTimeout(() => window.location.href = "admin-login.html", 1000);
    return;
  }

  loadMetrics();
});

async function loadMetrics() {
  try {
    const data = await Api.getMetrics();
    
    document.getElementById("metric-total").innerText = data.totalComplaints;
    document.getElementById("metric-pending").innerText = data.pendingComplaints;
    document.getElementById("metric-critical").innerText = data.criticalComplaints;
    document.getElementById("metric-progress").innerText = data.inProgressComplaints;
    document.getElementById("metric-resolved").innerText = data.resolvedComplaints;
    document.getElementById("metric-rate").innerText = `${data.resolutionRate}%`;

    renderCategoryChart(data.categoryCounts);
    renderPriorityChart(data.priorityCounts);
    renderStatusChart(data.statusCounts);

    renderRecentTriage(data.recentComplaints);
  } catch (err) {
    UI.showToast("Failed to load dashboard metrics: " + err.message, "error");
  }
}

function renderCategoryChart(catData) {
  const ctx = document.getElementById("chart-category")?.getContext("2d");
  if (!ctx) return;
  if (catChart) catChart.destroy();

  const labels = Object.keys(catData).map(k => k.replace(/_/g, ' '));
  const counts = Object.values(catData);

  catChart = new Chart(ctx, {
    type: 'doughnut',
    data: {
      labels,
      datasets: [{
        data: counts,
        backgroundColor: [
          '#f59e0b', '#3b82f6', '#10b981', '#6366f1', '#ec4899', '#06b6d4', '#64748b'
        ]
      }]
    },
    options: {
      responsive: true,
      plugins: {
        legend: { position: 'bottom', labels: { boxWidth: 12, font: { size: 11 } } }
      }
    }
  });
}

function renderPriorityChart(prioData) {
  const ctx = document.getElementById("chart-priority")?.getContext("2d");
  if (!ctx) return;
  if (prioChart) prioChart.destroy();

  prioChart = new Chart(ctx, {
    type: 'bar',
    data: {
      labels: ['CRITICAL', 'HIGH', 'MEDIUM', 'LOW'],
      datasets: [{
        label: 'Incident Count',
        data: [
          prioData['CRITICAL'] || 0,
          prioData['HIGH'] || 0,
          prioData['MEDIUM'] || 0,
          prioData['LOW'] || 0
        ],
        backgroundColor: ['#ef4444', '#f97316', '#eab308', '#10b981'],
        borderRadius: 6
      }]
    },
    options: {
      responsive: true,
      plugins: { legend: { display: false } },
      scales: {
        y: { beginAtZero: true, ticks: { precision: 0 } }
      }
    }
  });
}

function renderStatusChart(statusData) {
  const ctx = document.getElementById("chart-status")?.getContext("2d");
  if (!ctx) return;
  if (statusChart) statusChart.destroy();

  const labels = ['SUBMITTED', 'UNDER REVIEW', 'ASSIGNED', 'IN PROGRESS', 'RESOLVED'];
  const counts = [
    statusData['SUBMITTED'] || 0,
    statusData['UNDER_REVIEW'] || 0,
    statusData['ASSIGNED'] || 0,
    statusData['IN_PROGRESS'] || 0,
    statusData['RESOLVED'] || 0
  ];

  statusChart = new Chart(ctx, {
    type: 'polarArea',
    data: {
      labels,
      datasets: [{
        data: counts,
        backgroundColor: [
          'rgba(148, 163, 184, 0.7)',
          'rgba(56, 189, 248, 0.7)',
          'rgba(168, 85, 247, 0.7)',
          'rgba(245, 158, 11, 0.7)',
          'rgba(34, 197, 94, 0.7)'
        ]
      }]
    },
    options: {
      responsive: true,
      plugins: { legend: { position: 'bottom', labels: { boxWidth: 10, font: { size: 10 } } } }
    }
  });
}

function renderRecentTriage(list) {
  const tbody = document.getElementById("recent-triage-tbody");
  if (!tbody) return;
  tbody.innerHTML = "";

  (list || []).forEach(c => {
    const row = document.createElement("tr");
    row.innerHTML = `
      <td><span class="badge bg-light text-dark border fw-bold">${c.trackingId}</span></td>
      <td>
        <div class="fw-semibold text-truncate" style="max-width: 250px;">${c.title}</div>
        <small class="text-muted"><i class="bi bi-geo-alt me-1"></i>${c.address}</small>
      </td>
      <td><span class="badge bg-light text-dark border">${c.categoryDisplayName}</span></td>
      <td>${UI.priorityBadge(c.priority)}</td>
      <td>${UI.statusBadge(c.status)}</td>
      <td class="text-truncate small text-muted" style="max-width: 200px;" title="${c.aiSummary || ''}">${c.aiSummary || '-'}</td>
      <td>
        <a href="complaints.html?highlight=${encodeURIComponent(c.trackingId)}" class="btn btn-sm btn-outline-primary">
          <i class="bi bi-gear me-1"></i>Triage
        </a>
      </td>
    `;
    tbody.appendChild(row);
  });
}
