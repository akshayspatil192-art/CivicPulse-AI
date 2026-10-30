let allComplaints = [];
let allDepartments = [];
let currentSelectedComplaint = null;

document.addEventListener("DOMContentLoaded", async () => {
  const user = Api.getUser();
  if (!user || user.role !== 'ADMIN') {
    UI.showToast("Admin access required.", "error");
    setTimeout(() => window.location.href = "admin-login.html", 800);
    return;
  }

  await loadDepartments();
  await loadComplaints();
  setupFilterHandlers();
  setupModalActionHandlers();
});

async function loadDepartments() {
  try {
    allDepartments = await Api.getDepartments();
    const select = document.getElementById("modal-assign-dept");
    if (select) {
      select.innerHTML = '<option value="">-- Choose Department --</option>';
      allDepartments.forEach(d => {
        select.innerHTML += `<option value="${d.id}">${d.name} (${d.code})</option>`;
      });
    }
  } catch (err) {
    console.error("Failed to load departments", err);
  }
}

async function loadComplaints() {
  const tbody = document.getElementById("complaints-tbody");
  if (tbody) tbody.innerHTML = `<tr><td colspan="8" class="text-center py-4 text-muted"><span class="spinner-border spinner-border-sm me-2"></span>Loading civic complaints...</td></tr>`;

  try {
    const category = document.getElementById("filter-category")?.value || "";
    const priority = document.getElementById("filter-priority")?.value || "";
    const status = document.getElementById("filter-status")?.value || "";
    const search = document.getElementById("search-input")?.value.trim() || "";

    const params = {};
    if (category) params.category = category;
    if (priority) params.priority = priority;
    if (status) params.status = status;
    if (search) params.search = search;

    allComplaints = await Api.getComplaints(params);
    renderComplaintsTable(allComplaints);
  } catch (err) {
    UI.showToast("Error loading complaints: " + err.message, "error");
  }
}

function renderComplaintsTable(list) {
  const tbody = document.getElementById("complaints-tbody");
  if (!tbody) return;
  tbody.innerHTML = "";

  document.getElementById("complaints-count-display").innerText = `${list.length} Records Found`;

  if (list.length === 0) {
    tbody.innerHTML = `<tr><td colspan="8" class="text-center py-5 text-muted">No complaints match your selected filters.</td></tr>`;
    return;
  }

  list.forEach(c => {
    const row = document.createElement("tr");
    if (c.isDuplicate) row.classList.add("table-warning");

    row.innerHTML = `
      <td>
        <strong>${c.trackingId}</strong>
        ${c.isDuplicate ? '<span class="badge bg-warning text-dark ms-1" title="Duplicate report"><i class="bi bi-copy"></i></span>' : ''}
      </td>
      <td>
        <div class="fw-semibold text-truncate" style="max-width: 240px;">${c.title}</div>
        <small class="text-muted"><i class="bi bi-geo-alt me-1"></i>${c.address}</small>
      </td>
      <td><span class="badge bg-light text-dark border"><i class="bi ${UI.categoryIcon(c.category)} me-1"></i>${c.categoryDisplayName}</span></td>
      <td>${UI.priorityBadge(c.priority)}</td>
      <td>${UI.statusBadge(c.status)}</td>
      <td><span class="badge bg-secondary-subtle text-dark border">${c.assignedDepartmentName || 'Unassigned'}</span></td>
      <td class="small text-muted">${UI.formatDate(c.createdAt)}</td>
      <td>
        <button class="btn btn-sm btn-primary view-details-btn" data-id="${c.id}">
          <i class="bi bi-eye me-1"></i>Manage
        </button>
      </td>
    `;
    tbody.appendChild(row);
  });

  document.querySelectorAll(".view-details-btn").forEach(btn => {
    btn.addEventListener("click", () => {
      const id = btn.getAttribute("data-id");
      openComplaintModal(id);
    });
  });
}

function setupFilterHandlers() {
  ["filter-category", "filter-priority", "filter-status"].forEach(id => {
    document.getElementById(id)?.addEventListener("change", loadComplaints);
  });

  let searchTimeout = null;
  document.getElementById("search-input")?.addEventListener("input", () => {
    clearTimeout(searchTimeout);
    searchTimeout = setTimeout(loadComplaints, 400);
  });

  document.getElementById("reset-filters")?.addEventListener("click", () => {
    document.getElementById("filter-category").value = "";
    document.getElementById("filter-priority").value = "";
    document.getElementById("filter-status").value = "";
    document.getElementById("search-input").value = "";
    loadComplaints();
  });
}

function openComplaintModal(id) {
  const c = allComplaints.find(item => item.id == id);
  if (!c) return;
  currentSelectedComplaint = c;

  document.getElementById("modal-tracking-id").innerText = c.trackingId;
  document.getElementById("modal-title").innerText = c.title;
  document.getElementById("modal-desc").innerText = c.description;
  document.getElementById("modal-category").innerHTML = `<i class="bi ${UI.categoryIcon(c.category)} me-1"></i>${c.categoryDisplayName}`;
  document.getElementById("modal-priority").innerHTML = UI.priorityBadge(c.priority);
  document.getElementById("modal-status").innerHTML = UI.statusBadge(c.status);
  document.getElementById("modal-address").innerText = c.address;
  document.getElementById("modal-created").innerText = UI.formatDate(c.createdAt);
  document.getElementById("modal-citizen").innerText = `${c.citizenName} (${c.citizenEmail} / ${c.citizenPhone || 'No phone'})`;

  document.getElementById("modal-ai-summary").innerText = c.aiSummary || "AI summary not available.";
  document.getElementById("modal-ai-explanation").innerText = c.aiExplanation || "Priority factors calculated.";

  document.getElementById("modal-update-status").value = c.status;
  document.getElementById("modal-update-priority").value = c.priority;
  if (c.assignedDepartmentId) {
    document.getElementById("modal-assign-dept").value = c.assignedDepartmentId;
  }

  const dupAlert = document.getElementById("modal-duplicate-box");
  if (dupAlert) {
    if (c.isDuplicate) {
      dupAlert.classList.remove("d-none");
      dupAlert.innerHTML = `<i class="bi bi-info-circle-fill me-2"></i>Flagged as potential duplicate of report <strong>${c.duplicateOfTrackingId || 'Master'}</strong>.`;
    } else {
      dupAlert.classList.add("d-none");
    }
  }

  const modal = new bootstrap.Modal(document.getElementById("complaintDetailModal"));
  modal.show();
}

function setupModalActionHandlers() {
  document.getElementById("modal-btn-save-status")?.addEventListener("click", async () => {
    if (!currentSelectedComplaint) return;
    const newStatus = document.getElementById("modal-update-status").value;
    const remarks = document.getElementById("modal-status-remarks").value.trim();

    try {
      await Api.updateStatus(currentSelectedComplaint.id, newStatus, remarks);
      UI.showToast("Status updated to " + newStatus, "success");
      bootstrap.Modal.getInstance(document.getElementById("complaintDetailModal")).hide();
      await loadComplaints();
    } catch (err) {
      UI.showToast("Status update failed: " + err.message, "error");
    }
  });

  document.getElementById("modal-btn-assign-dept")?.addEventListener("click", async () => {
    if (!currentSelectedComplaint) return;
    const deptId = document.getElementById("modal-assign-dept").value;
    const notes = document.getElementById("modal-assign-notes").value.trim();
    if (!deptId) {
      UI.showToast("Please choose a department", "error");
      return;
    }

    try {
      await Api.assignDepartment(currentSelectedComplaint.id, deptId, notes);
      UI.showToast("Complaint assigned successfully!", "success");
      bootstrap.Modal.getInstance(document.getElementById("complaintDetailModal")).hide();
      await loadComplaints();
    } catch (err) {
      UI.showToast("Assignment failed: " + err.message, "error");
    }
  });

  document.getElementById("modal-btn-override-prio")?.addEventListener("click", async () => {
    if (!currentSelectedComplaint) return;
    const prio = document.getElementById("modal-update-priority").value;
    const reason = document.getElementById("modal-prio-reason").value.trim();

    try {
      await Api.updatePriority(currentSelectedComplaint.id, prio, reason);
      UI.showToast("Priority changed to " + prio, "success");
      bootstrap.Modal.getInstance(document.getElementById("complaintDetailModal")).hide();
      await loadComplaints();
    } catch (err) {
      UI.showToast("Priority adjustment failed: " + err.message, "error");
    }
  });
}
