# CIVICPULSE AI: System Architecture Document

## 1. High-Level Architectural Diagram

```
+-----------------------------------------------------------------------------------+
|                                 CITIZEN & ADMIN UI                                |
|  Bootstrap 5.3 | Leaflet.js Maps | Chart.js Dashboards | Live AI Copilot Previews |
+------------------------------------------+----------------------------------------+
                                           | HTTP / REST JSON
                                           v
+-----------------------------------------------------------------------------------+
|                         SPRING BOOT 3.3.x WEB LAYER                               |
|   AuthController | ComplaintController | AdminDashboardController | HotspotController  |
+------------------------------------------+----------------------------------------+
                                           | Service Calls
                                           v
+-----------------------------------------------------------------------------------+
|                            CORE SERVICE & AI LAYER                                |
|  - ComplaintService: Orchestrates submission, tracking, history & transitions     |
|  - UserService: BCrypt security & role-based authentication                       |
|  - DashboardService: Real-time municipal KPIs, resolution metrics & distributions |
|  - HotspotService: Density radius clustering & compound hazard correlation        |
|                                                                                   |
|  [HYBRID AI PROCESSING ENGINE]                                                    |
|  +------------------------------+     +----------------------------------------+  |
|  | Google Gemini 1.5 Flash API  |<--->| Deterministic Rule-Based NLP Engine    |  |
|  | (when API key is configured) |     | - Weighted token/n-gram classification |  |
|  +------------------------------+     | - Multi-factor priority scorer (0-100) |  |
|                                       | - Duplicate detection (Haversine &     |  |
|                                       |   Jaccard text similarity within 250m) |  |
|                                       +----------------------------------------+  |
+------------------------------------------+----------------------------------------+
                                           | Spring Data JPA / Hibernate
                                           v
+-----------------------------------------------------------------------------------+
|                           RELATIONAL PERSISTENCE (MySQL 8)                        |
|  users | departments | complaints | complaint_status_history | complaint_assignments |
+-----------------------------------------------------------------------------------+
```

---

## 2. Database Entity Relationships

1. **`users`**:
   - `id` (PK), `full_name`, `email` (UNIQUE), `phone`, `password` (BCrypt hash), `role` (`CITIZEN`, `ADMIN`), `created_at`.
2. **`departments`**:
   - `id` (PK), `name`, `code` (`ROADS`, `DRAINAGE`, `SOLID_WASTE`, `ELECTRICAL`, `WATER_WORKS`, `GENERAL_CIVIC`), `contact_email`, `phone`, `description`.
3. **`complaints`**:
   - `id` (PK), `tracking_id` (e.g., `CP-2026-1001`), `citizen_id` (FK -> `users.id`), `title`, `description`, `category` (Enum), `address`, `latitude`, `longitude`, `image_url`.
   - `priority` (`CRITICAL`, `HIGH`, `MEDIUM`, `LOW`), `status` (`SUBMITTED`, `UNDER_REVIEW`, `ASSIGNED`, `IN_PROGRESS`, `RESOLVED`).
   - `department_id` (FK -> `departments.id`), `ai_summary` (TEXT), `ai_explanation` (TEXT), `is_duplicate` (BOOLEAN), `duplicate_of_id` (FK -> `complaints.id`).
4. **`complaint_status_history`**:
   - `id` (PK), `complaint_id` (FK -> `complaints.id`), `old_status`, `new_status`, `remarks`, `updated_by` (FK -> `users.id`), `created_at`.
5. **`complaint_assignments`**:
   - `id` (PK), `complaint_id` (FK -> `complaints.id`), `department_id` (FK -> `departments.id`), `assigned_by` (FK -> `users.id`), `notes`, `created_at`.

---

## 3. Four-Stage Lifecycle Flow

1. **Stage 1: Capture**
   - Citizen provides title, description, category (or chooses "Auto-detect by AI"), coordinates via interactive Leaflet map pin, and optional photo evidence.
   - Form submission takes under 60 seconds on any mobile or desktop screen.

2. **Stage 2: Understand**
   - The AI Copilot computes real-time category classification.
   - Computes multi-factor priority score: Hazard Risk (40%), Urgency (30%), Public Impact (30%).
   - Compares incident against existing database records using Haversine spatial proximity (d <= 250m) and text Jaccard similarity.
   - Generates an actionable 1-sentence decision-maker brief and plain-language explanation.

3. **Stage 3: Reveal**
   - Hotspot clustering aggregates incidents within 350 meters.
   - Compound hazard logic identifies cross-departmental root causes:
     * *Blocked drain + Heavy waterlogging + Potholes* -> "Compound Infrastructure Failure: Blocked arterial drainage causing localized flooding and progressive road asphalt collapse."
   - Chart.js dashboards display category distributions, priority SLA risks, and workflow throughput.

4. **Stage 4: Act**
   - Municipal administrators assign tickets to appropriate departments with custom work order notes.
   - Status updates automatically append to the audit history log with timestamp and officer attribution.
   - Citizens track progress on `track-complaint.html` with a visual 5-step progress bar stepper.
