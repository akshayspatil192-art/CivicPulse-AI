let pickerMap, pickerMarker;
let currentLat = 12.9716;
let currentLng = 77.5946;
let aiDebounceTimeout = null;

document.addEventListener("DOMContentLoaded", () => {
  initPickerMap();
  setupEventListeners();
});

function initPickerMap() {
  const mapEl = document.getElementById("picker-map");
  if (!mapEl) return;

  pickerMap = L.map("picker-map").setView([currentLat, currentLng], 14);

  L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
    maxZoom: 19,
    attribution: "© OpenStreetMap contributors"
  }).addTo(pickerMap);

  pickerMarker = L.marker([currentLat, currentLng], { draggable: true }).addTo(pickerMap);

  pickerMarker.on("dragend", (e) => {
    const pos = e.target.getLatLng();
    updateLocationCoords(pos.lat, pos.lng);
  });

  pickerMap.on("click", (e) => {
    pickerMarker.setLatLng(e.latlng);
    updateLocationCoords(e.latlng.lat, e.latlng.lng);
  });

  updateLocationCoords(currentLat, currentLng);
}

function updateLocationCoords(lat, lng) {
  currentLat = parseFloat(lat.toFixed(6));
  currentLng = parseFloat(lng.toFixed(6));
  const latEl = document.getElementById("latitude");
  const lngEl = document.getElementById("longitude");
  const disp = document.getElementById("coords-display");
  if (latEl) latEl.value = currentLat;
  if (lngEl) lngEl.value = currentLng;
  if (disp) disp.innerText = `Coordinates: Lat ${currentLat}, Lng ${currentLng}`;
  triggerAiPreview();
}

function setupEventListeners() {
  document.getElementById("use-my-location")?.addEventListener("click", () => {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (pos) => {
          const lat = pos.coords.latitude;
          const lng = pos.coords.longitude;
          pickerMap.setView([lat, lng], 16);
          pickerMarker.setLatLng([lat, lng]);
          updateLocationCoords(lat, lng);
          UI.showToast("Current location detected!", "success");
        },
        () => {
          UI.showToast("Could not access GPS. Please click directly on the map to set location.", "info");
        }
      );
    }
  });

  const titleInput = document.getElementById("title");
  const descInput = document.getElementById("description");
  const catInput = document.getElementById("category");

  [titleInput, descInput, catInput].forEach(el => {
    el?.addEventListener("input", () => {
      clearTimeout(aiDebounceTimeout);
      aiDebounceTimeout = setTimeout(triggerAiPreview, 600);
    });
  });

  const form = document.getElementById("complaint-form");
  form?.addEventListener("submit", async (e) => {
    e.preventDefault();
    const btn = form.querySelector("button[type=submit]");
    btn.disabled = true;
    btn.innerHTML = `<span class="spinner-border spinner-border-sm me-2"></span>Running AI & Submitting...`;

    const user = Api.getUser();
    const citizenId = user ? user.id : 1;

    const formData = new FormData();
    formData.append("title", document.getElementById("title").value.trim());
    formData.append("description", document.getElementById("description").value.trim());
    formData.append("category", document.getElementById("category").value);
    formData.append("address", document.getElementById("address").value.trim());
    formData.append("latitude", currentLat);
    formData.append("longitude", currentLng);
    formData.append("citizenId", citizenId);

    const imageFile = document.getElementById("complaint-image")?.files[0];
    if (imageFile) {
      formData.append("image", imageFile);
    }

    try {
      const result = await Api.submitComplaint(formData);
      UI.showToast("Complaint submitted successfully! Tracking ID: " + result.trackingId, "success");
      setTimeout(() => {
        window.location.href = "track-complaint.html?id=" + encodeURIComponent(result.trackingId);
      }, 1200);
    } catch (err) {
      UI.showToast(err.message, "error");
      btn.disabled = false;
      btn.innerHTML = `<i class="bi bi-send-check me-2"></i>Submit Civic Complaint`;
    }
  });
}

async function triggerAiPreview() {
  const title = document.getElementById("title")?.value.trim();
  const description = document.getElementById("description")?.value.trim();
  const category = document.getElementById("category")?.value;

  if (!title || title.length < 5) return;

  const aiCard = document.getElementById("ai-copilot-card");
  if (!aiCard) return;

  aiCard.classList.remove("d-none");
  const aiStatus = document.getElementById("ai-status-indicator");
  if (aiStatus) aiStatus.innerHTML = `<span class="spinner-border spinner-border-sm text-primary me-2"></span>AI analyzing report in real-time...`;

  try {
    const analysis = await Api.previewAi({
      title,
      description,
      category: category === "AUTO" ? null : category,
      address: document.getElementById("address")?.value || "Pin location",
      latitude: currentLat,
      longitude: currentLng
    });

    renderAiCopilot(analysis);
  } catch (err) {
    console.warn("AI preview error", err);
  }
}

function renderAiCopilot(analysis) {
  const aiStatus = document.getElementById("ai-status-indicator");
  if (aiStatus) {
    aiStatus.innerHTML = analysis.geminiPowered 
      ? `<span class="badge bg-primary me-1"><i class="bi bi-stars me-1"></i>Gemini LLM Active</span> Analysis Complete`
      : `<span class="badge me-1" style="background:#0d9488;color:#fff"><i class="bi bi-cpu me-1"></i>Deterministic NLP Engine</span> Analysis Complete`;
  }

  const catEl = document.getElementById("ai-detected-cat");
  if (catEl) catEl.innerHTML = `<span class="badge bg-dark">${analysis.detectedCategory}</span>`;

  const summaryEl = document.getElementById("ai-summary-text");
  if (summaryEl) summaryEl.innerText = analysis.summary || "Summary generated.";

  const briefEl = document.getElementById("ai-action-brief");
  if (briefEl) briefEl.innerText = analysis.actionBrief || "Review for field assignment.";

  const prioEl = document.getElementById("ai-priority-badge");
  if (prioEl) prioEl.innerHTML = UI.priorityBadge(analysis.priority);

  const scoreEl = document.getElementById("ai-priority-score");
  if (scoreEl) scoreEl.innerText = `${analysis.priorityScore} / 100`;

  updateBar("bar-safety", analysis.safetyRiskFactor);
  updateBar("bar-urgency", analysis.urgencyFactor);
  updateBar("bar-impact", analysis.publicImpactFactor);

  const expEl = document.getElementById("ai-explanation-text");
  if (expEl) expEl.innerText = analysis.explanation;

  const dupBanner = document.getElementById("duplicate-alert-banner");
  if (dupBanner) {
    if (analysis.duplicate) {
      dupBanner.classList.remove("d-none");
      document.getElementById("dup-tracking-id").innerText = analysis.duplicateTrackingId || "Existing incident";
      document.getElementById("dup-reason").innerText = "A similar complaint already exists within the vicinity. Your submission will automatically be linked to consolidate field action.";
    } else {
      dupBanner.classList.add("d-none");
    }
  }
}

function updateBar(id, val) {
  const bar = document.getElementById(id);
  if (bar) {
    bar.style.width = `${val}%`;
    bar.innerText = `${val}%`;
    if (val >= 75) bar.className = "progress-bar bg-danger";
    else if (val >= 50) bar.className = "progress-bar bg-warning";
    else bar.className = "progress-bar bg-success";
  }
}
