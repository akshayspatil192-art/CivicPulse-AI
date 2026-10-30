document.addEventListener("DOMContentLoaded", () => {
  const user = Api.getUser();
  if (!user) {
    window.location.href = "login.html";
    return;
  }

  document.getElementById("citizen-name-display").innerText = user.fullName;
  loadCitizenComplaints(user.id);
});

async function loadCitizenComplaints(citizenId) {
  const tableBody = document.getElementById("citizen-complaints-tbody");
  try {
    const list = await Api.getCitizenComplaints(citizenId);
    
    const total = list.length;
    const inProgress = list.filter(c => c.status === 'IN_PROGRESS' || c.status === 'ASSIGNED').length;
    const resolved = list.filter(c => c.status === 'RESOLVED').length;
    const pending = total - (inProgress + resolved);

    document.getElementById("citizen-stat-total").innerText = total;
    document.getElementById("citizen-stat-pending").innerText = pending;
    document.getElementById("citizen-stat-active").innerText = inProgress;
    document.getElementById("citizen-stat-resolved").innerText = resolved;

    if (!tableBody) return;
    tableBody.innerHTML = "";

    if (list.length === 0) {
      tableBody.innerHTML = `
        <tr>
          <td colspan="7" class="text-center py-5 text-muted">
            <i class="bi bi-inbox fs-1 d-block mb-2 text-secondary"></i>
            You have not submitted any complaints yet.
            <div class="mt-3"><a href="submit-complaint.html" class="btn btn-sm btn-civic-teal"><i class="bi bi-plus-lg me-1"></i>Report Your First Issue</a></div>
          </td>
        </tr>
      `;
      return;
    }

    list.forEach(c => {
      const row = document.createElement("tr");
      row.innerHTML = `
        <td><a href="track-complaint.html?id=${encodeURIComponent(c.trackingId)}" class="fw-bold text-primary">${c.trackingId}</a></td>
        <td>
          <div class="fw-semibold text-truncate" style="max-width: 240px;" title="${c.title}">${c.title}</div>
          <small class="text-muted"><i class="bi bi-geo-alt me-1"></i>${c.address}</small>
        </td>
        <td><span class="badge bg-light text-dark border"><i class="bi ${UI.categoryIcon(c.category)} me-1"></i>${c.categoryDisplayName}</span></td>
        <td>${UI.priorityBadge(c.priority)}</td>
        <td>${UI.statusBadge(c.status)}</td>
        <td class="small text-muted">${UI.formatDate(c.createdAt)}</td>
        <td>
          <a href="track-complaint.html?id=${encodeURIComponent(c.trackingId)}" class="btn btn-sm btn-outline-primary">
            <i class="bi bi-radar me-1"></i>Track
          </a>
        </td>
      `;
      tableBody.appendChild(row);
    });
  } catch (err) {
    UI.showToast("Failed to load your complaints: " + err.message, "error");
  }
}
