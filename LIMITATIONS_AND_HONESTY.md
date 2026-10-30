# CIVICPULSE AI: Limitations and Honesty Note

In accordance with Section 7 and Section 8 of the problem statement rules (*"PromptWars x ASTRA | AI for Civic Impact"*), here is a transparent disclosure of what is real, what is synthetic/mocked, where the AI can make errors, and future roadmap items.

---

## 1. What is Real

1. **Full-Stack Application:** Real, working Spring Boot 3.3.x backend running on Java 21 LTS with MySQL 8.0 database persistence, Hibernate ORM, and Spring Security.
2. **REST APIs:** Fully implemented, validated, and tested HTTP endpoints for authentication, multipart complaint creation, file upload storage, AI analysis, tracking, department assignment, and status transitions.
3. **Frontend Suite:** 10 responsive HTML5/CSS3/JavaScript pages utilizing Bootstrap 5.3, interactive Leaflet.js maps, and Chart.js dashboards.
4. **Deterministic AI & Prioritization Engine:** Real, fully operational algorithm computing Haversine distance, token similarity, weighted multi-factor safety scoring, SLA mapping, and spatial clustering.
5. **Live Gemini Integration:** Real REST client designed to communicate with Google's Gemini 1.5 Flash API when `gemini.api.key` is provided, with graceful, transparent fallback.

---

## 2. What is Mocked / Synthetic

1. **Synthetic Incident Dataset:** The 42 pre-seeded complaints represent synthetic civic data located around a metropolitan test grid (Bangalore Central Corridor). Real citizen personal data is **not** used.
2. **SMS / Email Dispatch:** Department dispatching records assignments and notes in the database; real-world SMS/WhatsApp gateways (e.g., Twilio) are simulated via status history logs.
3. **Image Computer Vision:** Image uploads are saved to disk (`uploads/`) and previewed. Deep convolutional image feature extraction for pothole depth measurement is simulated through text prompt reasoning.

---

## 3. Where the AI Can Be Wrong (Failure Modes)

1. **Sarcasm or Ambiguous Language:** A citizen reporting *"Wonderful water fountain in our street!"* might be misinterpreted as positive sentiment by basic token matching if not filtered for water leakage keywords.
2. **Uncalibrated GPS Pins:** If a citizen submits a complaint with incorrect browser location permissions, the spatial gate may fail to detect duplicate reports or assign the wrong ward.
3. **Cross-Border Hotspots:** Fixed-radius clustering (350m) can artificially divide a continuous problem along an extended 2km arterial highway into separate clusters.

---

## 4. Next Steps & Production Roadmap

1. **Vector Embeddings (pgvector / Milvus):** Replace token Jaccard similarity with dense neural embeddings (e.g. `text-embedding-004`) for multilingual duplicate detection in regional Indian languages (Hindi, Kannada, Tamil, etc.).
2. **Computer Vision Hazard Verification:** Implement YOLOv10 / MobileNet on uploaded photos to independently verify road craters, submerged vehicles, and exposed wires before human triage.
3. **Municipal ERP & GIS Integration:** Integrate with municipal GIS layers (e.g., cadastral ward boundaries, underground sewer pipe network maps) to automatically identify responsible contractors.
