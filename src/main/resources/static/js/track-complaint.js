document.addEventListener("DOMContentLoaded", () => {
  const urlParams = new URLSearchParams(window.location.search);
  const trackId = urlParams.get("id");

  if (trackId) {
    document.getElementById("track-input").value = trackId;
    loadComplaintDetails(trackId);
  }

  document.getElementById("track-form")?.addEventListener("submit", (e) => {
    e.preventDefault();
    const id = document.getElementById("track-input").value.trim();
    if (id) {
      loadComplaintDetails(id);
    }
  });
});

async function loadComplaintDetails(trackingId) {
  const detailsContainer = document.getElementById("complaint-details-view");
  const emptyView = document.getElementById("empty-state");
  const loadingEl = document.getElementById("loading-state");

  emptyView?.classList.add("d-none");
  detailsContainer?.classList.add("d-none");
  loadingEl?.classList.remove("d-none");

  try {
    const data = await Api.trackComplaint(trackingId);
    loadingEl?.classList.add("d-none");
    detailsContainer?.classList.remove("d-none");
    renderComplaint(data);
  } catch (err) {
    loadingEl?.classList.add("d-none");
    emptyView?.classList.remove("d-none");
    UI.showToast("Complaint not found with ID: " + trackingId, "error");
  }
}

function renderComplaint(c) {
  document.getElementById("res-tracking-id").innerText = c.trackingId;
  document.getElementById("res-title").innerText = c.title;
  document.getElementById("res-category").innerHTML = `<i class="bi ${UI.categoryIcon(c.category)} me-1"></i>${c.categoryDisplayName}`;
  document.getElementById("res-priority").innerHTML = UI.priorityBadge(c.priority);
  document.getElementById("res-sla").innerText = `Target SLA: ${c.prioritySla}`;
  document.getElementById("res-status").innerHTML = UI.statusBadge(c.status);
  document.getElementById("res-address").innerText = c.address;
  document.getElementById("res-created-at").innerText = UI.formatDate(c.createdAt);
  document.getElementById("res-description").innerText = c.description;

  document.getElementById("res-department").innerText = c.assignedDepartmentName || "Pending Municipal Allocation";
  document.getElementById("res-ai-summary").innerText = c.aiSummary || "AI summary generated upon submission.";
  document.getElementById("res-ai-explanation").innerText = c.aiExplanation || "Computed priority based on safety and public impact.";

  const dupBadge = document.getElementById("res-duplicate-flag");
  if (dupBadge) {
    if (c.isDuplicate) {
      dupBadge.classList.remove("d-none");
      dupBadge.innerHTML = `<i class="bi bi-link-45deg me-1"></i>Linked to Primary Incident: <strong>${c.duplicateOfTrackingId || 'Master'}</strong>`;
    } else {
      dupBadge.classList.add("d-none");
    }
  }

  updateStepper(c.status);

  const tbody = document.getElementById("history-table-body");
  if (tbody) {
    tbody.innerHTML = "";
    if (c.history && c.history.length > 0) {
      c.history.forEach((h, idx) => {
        const row = document.createElement("tr");
        row.innerHTML = `
          <td><strong>#${idx + 1}</strong></td>
          <td>${UI.statusBadge(h.newStatus)}</td>
          <td>${h.remarks || '-'}</td>
          <td><span class="badge bg-light text-dark border">${h.updatedByName || 'Municipal Officer'}</span></td>
          <td class="text-muted small">${UI.formatDate(h.createdAt)}</td>
        `;
        tbody.appendChild(row);
      });
    } else {
      tbody.innerHTML = `<tr><td colspan="5" class="text-center text-muted py-3">No status transitions recorded yet.</td></tr>`;
    }
  }
}

function updateStepper(status) {
  const steps = ["SUBMITTED", "UNDER_REVIEW", "ASSIGNED", "IN_PROGRESS", "RESOLVED"];
  const currentIdx = steps.indexOf(status);

  steps.forEach((step, idx) => {
    const el = document.getElementById("step-" + step.toLowerCase());
    if (el) {
      el.classList.remove("completed", "active");
      if (idx < currentIdx) {
        el.classList.add("completed");
      } else if (idx === currentIdx) {
        el.classList.add("active");
      }
    }
  });

  const progressBar = document.getElementById("stepper-progress-bar");
  if (progressBar) {
    const pct = currentIdx >= 0 ? (currentIdx / (steps.length - 1)) * 100 : 0;
    progressBar.style.width = `${pct}%`;
  }
}
