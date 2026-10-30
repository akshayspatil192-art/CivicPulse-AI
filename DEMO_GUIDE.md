# CIVICPULSE AI: 5-Minute Live Hackathon Demonstration Guide

Follow this step-by-step script for a high-impact, live demonstration to hackathon judges.

---

## ⏱️ Demo Timeline (Target: 5 Minutes)

### Minute 0:00 - 0:45 | Introduction & Problem Context
1. Open the Landing Page: **`http://localhost:8080/index.html`**.
2. **Pitch:** *"Municipal authorities receive hundreds of unstructured complaints across WhatsApp, phone calls, and portals. Urgent hazards sit in the same queue as routine requests, identical problems are reported dozens of times, and citizens are left in the dark. CIVICPULSE AI transforms this chaos into prioritized, explainable, and trackable action across 4 stages: Capture, Understand, Reveal, and Act."*
3. Highlight the live municipal metrics counter: 40+ incidents already triaged in real time.

---

### Minute 0:45 - 2:00 | Stage 1 (Capture) & Stage 2 (Understand) Live Submission
1. Click **"Report Issue"** (`http://localhost:8080/submit-complaint.html`).
2. Show the Leaflet map: Click on the map pin near MG Road (Coordinates: `12.9716, 77.5946`).
3. Enter the Title:
   ```text
   Severe water accumulation blocking main road
   ```
4. Enter the Description:
   ```text
   Heavy rainwater is accumulated on the main road. Vehicles cannot pass properly. Traffic is completely stalled and ambulances are struggling to get through.
   ```
5. Leave Category as **"Auto-detect using AI"**.
6. **Showcase the Live AI Copilot Panel:**
   - Point out how the engine immediately detected **WATERLOGGING** without manual selection.
   - Show the **Priority Score (62/100 -> HIGH)** and the transparent factor breakdown (Safety Risk, Urgency, Public Impact).
   - Point out the **Duplicate Alert Banner**: The system detected existing report `CP-2026-1001` within 50m and alerted the user that field work orders will be consolidated!
7. Click **"Submit Civic Complaint"**.

---

### Minute 2:00 - 3:00 | Stage 4 (Act): Citizen Tracking
1. The submission automatically redirects to the Tracking View (**`http://localhost:8080/track-complaint.html`**).
2. Showcase the **5-Step Timeline Stepper**: Visual progression from `SUBMITTED` -> `UNDER REVIEW` -> `ASSIGNED` -> `IN PROGRESS` -> `RESOLVED`.
3. Point out the auto-assigned department (**Stormwater Drainage & Sewerage Board**) and the initial audit log entry.

---

### Minute 3:00 - 4:00 | Stage 3 (Reveal): Admin Command Center & Hotspots
1. Log in to the Municipal Command Center (**`http://localhost:8080/admin-login.html`**).
   - Click **"Auto-fill Demo Admin Credentials"** (`admin@civicpulse.gov` / `Admin@123`).
2. On `admin-dashboard.html`, showcase:
   - Real-time KPIs (Total Incidents, Critical Hazards, Resolution Rate).
   - Chart.js interactive charts (Category Distribution, Priority SLA Risks, Stage Progression).
3. Click **"Hotspots Map"** (`http://localhost:8080/hotspots.html`):
   - Show the interactive spatial clusters.
   - Click on **Hotspot Area 2 (MG Road Corridor)**.
   - Point out the **Compound Infrastructure Risk Analysis**:
     > *"Compound Infrastructure Failure: Blocked arterial drainage causing localized flooding and progressive road asphalt collapse."*
   - Explain to judges: *"Instead of sending three different uncoordinated teams for a drain, a flood, and a pothole, the city can now dispatch one coordinated intervention unit."*

---

### Minute 4:00 - 4:45 | Incident Triage & Resolution Workflow
1. Navigate to **Incident Management** (`http://localhost:8080/complaints.html`).
2. Filter by Category (`WATERLOGGING`) or Priority (`HIGH`).
3. Click **"Manage"** on the newly submitted complaint.
4. Show the AI Explainability box.
5. Transition the status to **`IN_PROGRESS`** with the remark:
   ```text
   High-capacity dewatering pump unit deployed to clear MG Road corridor.
   ```
6. Click **"Transition Status"**.
7. Switch back to the citizen tracking tab, refresh, and show the updated timeline and official remark!

---

### Minute 4:45 - 5:00 | Conclusion & Q&A
1. Reiterate the technology stack: Java 21, Spring Boot 3.3, MySQL 8, Leaflet.js, Chart.js.
2. Highlight our transparency note: Clear boundary between our deterministic NLP engine and the optional Gemini LLM integration.
3. Open for judges' questions!
