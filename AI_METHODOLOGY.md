# CIVICPULSE AI: AI Methodology & Prioritization Engine

## 1. Hybrid Architecture

CIVICPULSE AI implements a production-ready **Hybrid AI Architecture**:
1. **Google Gemini LLM Integration:** If a `GEMINI_API_KEY` is present in the environment or properties, the system queries Gemini 1.5 Flash (`https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent`) for natural language summarization and multi-factor reasoning.
2. **Deterministic NLP & Geospatial Engine:** If no API key is configured or the external network request times out, the system automatically and seamlessly falls back to our internal, deterministic NLP and heuristic engine without breaking the user experience.

---

## 2. Multi-Factor Priority Scoring Engine

The priority engine calculates an objective score $S \in [0, 100]$ using three weighted dimensions:

$$S = 0.40 \cdot \text{SafetyRisk} + 0.30 \cdot \text{Urgency} + 0.30 \cdot \text{PublicImpact}$$

### Factor A: Safety Risk (Weight: 40%)
- **Critical Safety Triggers (Score: 90 - 100):**
  - Snapped live electrical wires, open manholes on sidewalks, deep craters, sinkholes, structural bridge collapses, gas leaks, toxic sewage backup into homes.
- **High Safety Triggers (Score: 70 - 85):**
  - Severe waterlogging concealing road hazards, unlit dark pedestrian avenues near schools, overflowing sewage in market corridors.
- **Medium / Low Triggers (Score: 30 - 55):**
  - Routine potholes, uncollected residential bins, broken curbs.

### Factor B: Urgency & Disruption (Weight: 30%)
- Evaluates emergency keywords (`emergency`, `immediate danger`, `ambulance blocked`, `traffic impassable`, `hospital access`).

### Factor C: Public Impact & Proximity Volume (Weight: 30%)
- Identifies critical infrastructure zones (`school`, `market`, `transit junction`, `highway`, `hospital`).
- Grants a volume bonus based on the number of active complaints in the immediate 350m vicinity (+8 points per report, up to +30).

### Priority Score Mapping & SLAs

| Total Score | Priority Level | Target Resolution SLA |
| :---: | :---: | :---: |
| >= 78 (or Safety >= 85) | **CRITICAL** | **4 Hours** |
| 58 - 77 (or Safety >= 65) | **HIGH** | **24 Hours** |
| 38 - 57 | **MEDIUM** | **72 Hours** |
| < 38 | **LOW** | **7 Days** |

---

## 3. Duplicate Detection Algorithm

To prevent duplicated work orders and redundant dispatches, incoming complaints are checked against existing active complaints using a dual-gate algorithm:

1. **Spatial Gate (Haversine Formula):**
   Calculates the geodesic distance $d$ between coordinates $(\text{lat}_1, \text{lng}_1)$ and $(\text{lat}_2, \text{lng}_2)$. Reports beyond $250\text{ meters}$ are considered geographically distinct.
2. **Semantic Gate (Jaccard Similarity over Normalized N-grams):**
   Text is tokenized, stripped of punctuation and municipal stopwords (`road`, `street`, `near`, `the`, `is`, `at`).
   Similarity is computed as:
   $$J(A, B) = \frac{|A \cap B|}{|A \cup B|}$$
   If categories match or are functionally related (e.g. `WATERLOGGING` and `DRAINAGE_PROBLEMS`), a $+0.20$ confidence boost is applied.
3. **Threshold:**
   If $d \le 60\text{m}$ with matching category, or $d \le 250\text{m}$ with $J \ge 0.45$, the complaint is flagged as a duplicate, linked to the master incident ID, and alerted in both the Citizen and Admin views.

---

## 4. Compound Hotspot Detection Logic

The Hotspot engine groups incidents within a 350-meter radius and inspects cross-category interactions:
- If a cluster contains `DRAINAGE_PROBLEMS`, `WATERLOGGING`, and `ROAD_DAMAGE`, it is identified as a **"Compound Infrastructure Failure"** where blocked drainage is causing localized flooding, which in turn degrades road sub-base asphalt.
- This gives municipal decision-makers actionable root-cause intelligence rather than treating symptoms in isolation.
