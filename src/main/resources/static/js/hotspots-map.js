let hotspotMap = null;
let clusterLayers = [];

document.addEventListener("DOMContentLoaded", () => {
  initHotspotMap();
  loadHotspotData();
});

function initHotspotMap() {
  const mapEl = document.getElementById("hotspot-map");
  if (!mapEl) return;

  hotspotMap = L.map("hotspot-map").setView([12.9716, 77.5946], 13);

  L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
    maxZoom: 19,
    attribution: "© OpenStreetMap contributors"
  }).addTo(hotspotMap);
}

async function loadHotspotData() {
  const listContainer = document.getElementById("hotspots-list");
  if (listContainer) {
    listContainer.innerHTML = `<div class="text-center py-4 text-muted"><span class="spinner-border spinner-border-sm me-2"></span>Detecting spatial incident clusters...</div>`;
  }

  try {
    const hotspots = await Api.getHotspots();
    renderHotspotMap(hotspots);
    renderHotspotCards(hotspots);
  } catch (err) {
    UI.showToast("Failed to load hotspot data: " + err.message, "error");
  }
}

function renderHotspotMap(hotspots) {
  if (!hotspotMap) return;

  clusterLayers.forEach(l => hotspotMap.removeLayer(l));
  clusterLayers = [];

  if (hotspots.length === 0) return;

  const bounds = L.latLngBounds();

  hotspots.forEach(h => {
    const color = h.severityLevel === 'CRITICAL' ? '#ef4444' : h.severityLevel === 'HIGH' ? '#f97316' : '#eab308';

    const circle = L.circle([h.centerLatitude, h.centerLongitude], {
      color: color,
      fillColor: color,
      fillOpacity: 0.25,
      radius: h.radiusMeters
    }).addTo(hotspotMap);

    const marker = L.circleMarker([h.centerLatitude, h.centerLongitude], {
      color: '#fff',
      fillColor: color,
      fillOpacity: 0.95,
      radius: 12,
      weight: 3
    }).addTo(hotspotMap);

    marker.bindPopup(`
      <div style="font-family: inherit; font-size: 0.9rem;">
        <span class="badge ${h.severityLevel === 'CRITICAL' ? 'bg-danger' : 'bg-warning'} text-white">${h.severityLevel} SEVERITY</span>
        <h6 class="mt-2 mb-1 fw-bold">${h.name}</h6>
        <div class="text-muted small mb-2"><i class="bi bi-stack me-1"></i>${h.complaintCount} Concentrated Reports</div>
        <p class="mb-2 small"><strong>Root Cause:</strong> ${h.compoundRiskDescription}</p>
        <div class="small fw-semibold text-primary">Radius: ${h.radiusMeters}m</div>
      </div>
    `);

    clusterLayers.push(circle);
    clusterLayers.push(marker);

    bounds.extend([h.centerLatitude, h.centerLongitude]);

    h.complaints.forEach(c => {
      const pColor = c.priority === 'CRITICAL' ? '#dc2626' : c.priority === 'HIGH' ? '#ea580c' : '#ca8a04';
      const cMarker = L.circleMarker([c.latitude, c.longitude], {
        color: '#ffffff',
        fillColor: pColor,
        fillOpacity: 0.85,
        radius: 6,
        weight: 1.5
      }).addTo(hotspotMap);

      cMarker.bindPopup(`
        <div class="small">
          <strong>${c.trackingId}</strong>: ${c.title}<br>
          <span class="text-muted">${c.categoryDisplayName}</span> | <strong>${c.priority}</strong>
        </div>
      `);
      clusterLayers.push(cMarker);
    });
  });

  if (hotspots.length > 0) {
    hotspotMap.fitBounds(bounds, { padding: [50, 50] });
  }
}

function renderHotspotCards(hotspots) {
  const container = document.getElementById("hotspots-list");
  if (!container) return;
  container.innerHTML = "";

  document.getElementById("hotspots-count-badge").innerText = `${hotspots.length} Civic Hotspots Identified`;

  if (hotspots.length === 0) {
    container.innerHTML = `<div class="p-4 text-center text-muted">No high-density incident clusters detected currently.</div>`;
    return;
  }

  hotspots.forEach(h => {
    const card = document.createElement("div");
    card.className = "custom-card p-3 mb-3 border-start border-4 " + (h.severityLevel === 'CRITICAL' ? 'border-danger' : 'border-warning');

    card.innerHTML = `
      <div class="d-flex justify-content-between align-items-start mb-2">
        <h6 class="fw-bold mb-0">${h.name}</h6>
        <span class="badge ${h.severityLevel === 'CRITICAL' ? 'bg-danger' : 'bg-warning text-dark'}">${h.severityLevel}</span>
      </div>
      <div class="text-muted small mb-2">
        <i class="bi bi-geo-alt me-1"></i>Lat: ${h.centerLatitude.toFixed(4)}, Lng: ${h.centerLongitude.toFixed(4)} | 
        <strong>${h.complaintCount} Reports</strong> in ${h.radiusMeters}m Zone
      </div>
      <div class="p-2 rounded bg-light border small mb-2 text-dark">
        <i class="bi bi-lightning-charge-fill text-warning me-1"></i>
        ${h.compoundRiskDescription}
      </div>
      <div class="d-flex justify-content-between align-items-center mt-2">
        <span class="badge bg-secondary-subtle text-dark border">Dominant: ${h.dominantCategory.replace(/_/g, ' ')}</span>
        <button class="btn btn-sm btn-outline-primary zoom-btn" data-lat="${h.centerLatitude}" data-lng="${h.centerLongitude}">
          <i class="bi bi-geo-fill me-1"></i>Focus on Map
        </button>
      </div>
    `;
    container.appendChild(card);
  });

  document.querySelectorAll(".zoom-btn").forEach(btn => {
    btn.addEventListener("click", () => {
      const lat = parseFloat(btn.getAttribute("data-lat"));
      const lng = parseFloat(btn.getAttribute("data-lng"));
      hotspotMap.setView([lat, lng], 16, { animate: true });
    });
  });
}
