package com.civicpulse.config;

import com.civicpulse.entity.*;
import com.civicpulse.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final ComplaintRepository complaintRepository;
    private final ComplaintStatusHistoryRepository historyRepository;
    private final ComplaintAssignmentRepository assignmentRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(
            UserRepository userRepository,
            DepartmentRepository departmentRepository,
            ComplaintRepository complaintRepository,
            ComplaintStatusHistoryRepository historyRepository,
            ComplaintAssignmentRepository assignmentRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.complaintRepository = complaintRepository;
        this.historyRepository = historyRepository;
        this.assignmentRepository = assignmentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Database already initialized with demo data.");
            return;
        }

        log.info("Initializing CivicPulse database with seed users, departments, and synthetic complaints...");

        // 1. Create Users
        User admin = new User("Municipal Administrator", "admin@civicpulse.gov", "+91 98765 00001", passwordEncoder.encode("Admin@123"), Role.ADMIN);
        User citizen1 = new User("Rahul Sharma", "citizen@example.com", "+91 98765 11111", passwordEncoder.encode("Citizen@123"), Role.CITIZEN);
        User citizen2 = new User("Priya Patel", "priya.patel@example.com", "+91 98765 22222", passwordEncoder.encode("Citizen@123"), Role.CITIZEN);
        User citizen3 = new User("Amit Verma", "amit.verma@example.com", "+91 98765 33333", passwordEncoder.encode("Citizen@123"), Role.CITIZEN);
        User citizen4 = new User("Sneha Reddy", "sneha.reddy@example.com", "+91 98765 44444", passwordEncoder.encode("Citizen@123"), Role.CITIZEN);

        userRepository.saveAll(List.of(admin, citizen1, citizen2, citizen3, citizen4));

        // 2. Create Departments
        Department deptRoads = new Department("Road Infrastructure & Maintenance", "ROADS", "roads@civicpulse.gov", "080-2222-1001", "Pothole repair, road resurfacing, asphalt maintenance");
        Department deptDrainage = new Department("Stormwater Drainage & Sewerage Board", "DRAINAGE", "drainage@civicpulse.gov", "080-2222-1002", "Desilting, flood mitigation, sewer blockage removal");
        Department deptWaste = new Department("Solid Waste Management", "SOLID_WASTE", "waste@civicpulse.gov", "080-2222-1003", "Garbage collection, public bins, composting facilities");
        Department deptElectrical = new Department("Electricity & Public Lighting", "ELECTRICAL", "electrical@civicpulse.gov", "080-2222-1004", "Streetlight illumination, exposed wire hazards, transformers");
        Department deptWater = new Department("Potable Water Supply & Pipeline Works", "WATER_WORKS", "water@civicpulse.gov", "080-2222-1005", "Water distribution, mains leakage repairs, water tankers");
        Department deptGeneral = new Department("General Civic Grievance Cell", "GENERAL_CIVIC", "grievances@civicpulse.gov", "080-2222-1006", "Encroachments, signage, miscellaneous civic complaints");

        departmentRepository.saveAll(List.of(deptRoads, deptDrainage, deptWaste, deptElectrical, deptWater, deptGeneral));

        // 3. Create Synthetic Complaints (40+ items with clusters & deliberate duplicates)
        List<User> citizens = List.of(citizen1, citizen2, citizen3, citizen4);

        // Core Metro Cluster center: ~12.9716, 77.5946 (MG Road / Brigade Road Corridor)
        double baseLat = 12.9716;
        double baseLng = 77.5946;

        List<ComplaintData> seedList = new ArrayList<>();

        // Cluster A: MG Road Drainage, Waterlogging & Road Damage Failure Zone (Hotspot 1)
        seedList.add(new ComplaintData(
            "CP-2026-1001",
            "Severe water accumulation blocking MG Road junction",
            "Heavy rainwater is accumulated on the main road near metro pillar 42. Vehicles and city buses cannot pass properly. Traffic is completely stalled.",
            ComplaintCategory.WATERLOGGING,
            "MG Road Junction near Metro Pillar 42, Central Zone",
            baseLat, baseLng,
            Priority.HIGH, ComplaintStatus.IN_PROGRESS, deptDrainage,
            "Water accumulation severely obstructing arterial traffic movement.",
            "Assigned HIGH priority (24 Hours SLA). Elevated safety concern creating dangerous transit conditions; High operational urgency affecting emergency transit; Widespread public disruption affecting a high-density transit corridor.",
            false, null
        ));

        // Deliberate Duplicate of 1001
        seedList.add(new ComplaintData(
            "CP-2026-1002",
            "Huge waterlogging on MG road near metro station",
            "Heavy rainwater logging near metro pillar 43. Roads submerged in 2 feet water, cars getting stuck.",
            ComplaintCategory.WATERLOGGING,
            "Opposite MG Road Metro Station, Central Zone",
            baseLat + 0.0003, baseLng + 0.0002, // ~35 meters away!
            Priority.HIGH, ComplaintStatus.UNDER_REVIEW, deptDrainage,
            "Water accumulation severely obstructing arterial traffic movement.",
            "Assigned HIGH priority (24 Hours SLA). Potential duplicate detected of CP-2026-1001 ('Severe water accumulation blocking MG Road junction'). Located 38.5m away with 78% semantic/category correlation.",
            true, "CP-2026-1001"
        ));

        seedList.add(new ComplaintData(
            "CP-2026-1003",
            "Major storm drain completely blocked with plastic debris",
            "The main stormwater drain along the pedestrian footpath is choked with waste. Black foul water is regurgitating onto the road.",
            ComplaintCategory.DRAINAGE_PROBLEMS,
            "Footpath adjacent to MG Road Bus Bay, Central Zone",
            baseLat + 0.0005, baseLng - 0.0002,
            Priority.HIGH, ComplaintStatus.ASSIGNED, deptDrainage,
            "Sewer/drainage blockage causing sewage overflow and public sanitation hazard.",
            "Assigned HIGH priority (24 Hours SLA). Sewage overflow creates elevated public health risk; compounding factor for localized street flooding.",
            false, null
        ));

        seedList.add(new ComplaintData(
            "CP-2026-1004",
            "Deep submerged crater pothole near pedestrian crossing",
            "A massive pothole has opened up under the waterlogged street. Two two-wheelers already skidded and fell. Extremely hazardous for commuters.",
            ComplaintCategory.ROAD_DAMAGE,
            "MG Road pedestrian crossing near Brigade signal",
            baseLat - 0.0004, baseLng + 0.0003,
            Priority.CRITICAL, ComplaintStatus.IN_PROGRESS, deptRoads,
            "Significant roadway deterioration and surface craters impairing traffic safety.",
            "Assigned CRITICAL priority (4 Hours SLA). Severe safety hazard identified posing imminent risk of injury or structural damage; submerged crater invisible to drivers.",
            false, null
        ));

        // Critical safety issue: Live wire near school
        seedList.add(new ComplaintData(
            "CP-2026-1005",
            "Live electrical wire sparking dangerously near school gate",
            "A high-tension cable snapped from the streetlight pole and is dangling onto the pavement right outside the primary school gate. Sparks are flying.",
            ComplaintCategory.STREETLIGHT_PROBLEMS,
            "St. Joseph School Lane, Residency Road",
            12.9680, 77.6010,
            Priority.CRITICAL, ComplaintStatus.ASSIGNED, deptElectrical,
            "Non-operational public illumination leading to safety and security vulnerability.",
            "Assigned CRITICAL priority (4 Hours SLA). Severe safety hazard identified: Live wire snapping near school children poses imminent risk of electrocution.",
            false, null
        ));

        // Critical safety issue: Open manhole
        seedList.add(new ComplaintData(
            "CP-2026-1006",
            "Open manhole with missing cast iron cover on busy footpath",
            "Manhole cover is completely missing on the crowded market sidewalk. Anyone walking after dusk could easily fall in and sustain fatal injuries.",
            ComplaintCategory.DRAINAGE_PROBLEMS,
            "Commercial Street Market entrance, Shivajinagar",
            12.9822, 77.6083,
            Priority.CRITICAL, ComplaintStatus.IN_PROGRESS, deptDrainage,
            "Sewer/drainage blockage causing sewage overflow and public sanitation hazard.",
            "Assigned CRITICAL priority (4 Hours SLA). Missing cover over deep trunk sewer line presents immediate life-safety hazard in dense pedestrian corridor.",
            false, null
        ));

        // Deliberate Duplicate of 1006
        seedList.add(new ComplaintData(
            "CP-2026-1007",
            "Uncovered open manhole on market street sidewalk",
            "Dangerous open gutter hole without any lid on Commercial Street walkway. Urgent action needed before an accident happens.",
            ComplaintCategory.DRAINAGE_PROBLEMS,
            "Commercial Street 2nd Cross, Shivajinagar",
            12.9824, 77.6084, // ~25 meters away!
            Priority.CRITICAL, ComplaintStatus.UNDER_REVIEW, deptDrainage,
            "Sewer/drainage blockage causing sewage overflow and public sanitation hazard.",
            "Assigned CRITICAL priority (4 Hours SLA). Potential duplicate detected of CP-2026-1006 ('Open manhole with missing cast iron cover'). Located 24.2m away with 84% semantic/category correlation.",
            true, "CP-2026-1006"
        ));

        // Cluster B: Indiranagar 100ft Road Waste & Lighting Cluster (Hotspot 2)
        double indLat = 12.9784;
        double indLng = 77.6408;

        seedList.add(new ComplaintData(
            "CP-2026-1008",
            "Overflowing garbage dump attracting stray animals on 100 Feet Road",
            "Large community dumpster has not been cleared for 5 days. Rotting organic waste is spilling across the service road, foul stench spreading to restaurants.",
            ComplaintCategory.GARBAGE_OVERFLOW,
            "100 Feet Road opposite 12th Main junction, Indiranagar",
            indLat, indLng,
            Priority.MEDIUM, ComplaintStatus.IN_PROGRESS, deptWaste,
            "Accumulated municipal solid waste requiring immediate clearance and sanitization.",
            "Assigned MEDIUM priority (72 Hours SLA). Public health nuisance and commercial corridor aesthetic impact without immediate trauma risk.",
            false, null
        ));

        seedList.add(new ComplaintData(
            "CP-2026-1009",
            "Non-functional streetlights creating pitch dark stretch",
            "Entire row of 6 streetlights from 100ft road to 14th Main is out of order. Dark stretch causing safety concerns for women returning from work at night.",
            ComplaintCategory.STREETLIGHT_PROBLEMS,
            "100 Feet Road 14th Main Corner, Indiranagar",
            indLat + 0.0010, indLng + 0.0008,
            Priority.HIGH, ComplaintStatus.ASSIGNED, deptElectrical,
            "Non-operational public illumination leading to safety and security vulnerability.",
            "Assigned HIGH priority (24 Hours SLA). Complete darkness across key transit stretch elevates public security vulnerability and pedestrian hazard.",
            false, null
        ));

        seedList.add(new ComplaintData(
            "CP-2026-1010",
            "Garbage accumulation blocking pedestrian sidewalk",
            "Commercial waste bags piled high on the pedestrian walkway, forcing pedestrians to walk on the speeding roadway.",
            ComplaintCategory.GARBAGE_OVERFLOW,
            "100 Feet Road near Metro Station Exit B, Indiranagar",
            indLat - 0.0008, indLng - 0.0005,
            Priority.MEDIUM, ComplaintStatus.SUBMITTED, deptWaste,
            "Accumulated municipal solid waste requiring immediate clearance and sanitization.",
            "Assigned MEDIUM priority (72 Hours SLA). Obstruction of pedestrian path by commercial refuse.",
            false, null
        ));

        // Cluster C: Koramangala 80ft Road Water & Road Cluster (Hotspot 3)
        double korLat = 12.9352;
        double korLng = 77.6245;

        seedList.add(new ComplaintData(
            "CP-2026-1011",
            "Major drinking water pipeline burst wasting thousands of gallons",
            "Underground distribution main ruptured at 4 AM. Clean drinking water is shooting 10 feet into the air and flooding the entire residential block.",
            ComplaintCategory.WATER_SUPPLY,
            "80 Feet Road 4th Block, Koramangala",
            korLat, korLng,
            Priority.HIGH, ComplaintStatus.IN_PROGRESS, deptWater,
            "Municipal potable water disruption or distribution pipeline leakage.",
            "Assigned HIGH priority (24 Hours SLA). Severe clean water wastage, localized flooding, and loss of supply to 500+ households.",
            false, null
        ));

        seedList.add(new ComplaintData(
            "CP-2026-1012",
            "Cluster of dangerous potholes along 80 Feet Road",
            "Series of consecutive deep potholes formed along the bus route. Heavy vehicles are swerving abruptly to avoid them, causing near-misses.",
            ComplaintCategory.ROAD_DAMAGE,
            "80 Feet Road near Sony World Signal, Koramangala",
            korLat + 0.0012, korLng - 0.0009,
            Priority.HIGH, ComplaintStatus.ASSIGNED, deptRoads,
            "Significant roadway deterioration and surface craters impairing traffic safety.",
            "Assigned HIGH priority (24 Hours SLA). Structural roadway failure along high-volume transit route causing severe traffic friction.",
            false, null
        ));

        seedList.add(new ComplaintData(
            "CP-2026-1013",
            "Sewage backing up into residential basements",
            "Main sewer line is backed up. Dirty sewage water is flowing into apartment basements and parking lots.",
            ComplaintCategory.DRAINAGE_PROBLEMS,
            "5th Block 1st Cross, Koramangala",
            korLat - 0.0015, korLng + 0.0011,
            Priority.CRITICAL, ComplaintStatus.UNDER_REVIEW, deptDrainage,
            "Sewer/drainage blockage causing sewage overflow and public sanitation hazard.",
            "Assigned CRITICAL priority (4 Hours SLA). Direct residential contamination and biohazard risk affecting multi-family housing complex.",
            false, null
        ));

        // Resolved complaints for analytics and lifecycle demo
        seedList.add(new ComplaintData(
            "CP-2026-1014",
            "Broken street lamp post repaired after vehicle collision",
            "Street light pole bent and unlit after mini-truck impact.",
            ComplaintCategory.STREETLIGHT_PROBLEMS,
            "Richmond Circle Flyover Approach",
            12.9665, 77.5992,
            Priority.MEDIUM, ComplaintStatus.RESOLVED, deptElectrical,
            "Non-operational public illumination leading to safety and security vulnerability.",
            "Assigned MEDIUM priority. Resolved by Electrical Works Team: Luminaire replaced and pole realigned.",
            false, null
        ));

        seedList.add(new ComplaintData(
            "CP-2026-1015",
            "Asphalt patch repair completed on Double Road",
            "Pothole repaired with hot-mix bitumen.",
            ComplaintCategory.ROAD_DAMAGE,
            "Kengal Hanumanthaiah Road, Shanti Nagar",
            12.9554, 77.5928,
            Priority.LOW, ComplaintStatus.RESOLVED, deptRoads,
            "Significant roadway deterioration and surface craters impairing traffic safety.",
            "Assigned LOW priority. Resolved: Bitumen patch compacted and inspected by Municipal Engineer.",
            false, null
        ));

        // Generate 25 more diverse complaints across the municipal grid
        String[] titles = {
            "Damaged storm drainage culvert collapsing under sidewalk",
            "Low water pressure affecting 3rd stage residential layout",
            "Uncollected construction debris dumped on vacant plot",
            "Flickering sodium vapor streetlight on residential avenue",
            "Water stagnation near government hospital entrance gate",
            "Cracked road surface expanding along bus route 201",
            "Foul smell from uncovered sewer inspection chamber",
            "Drinking water pipeline leaking near water meter junction",
            "Overflowing public trash can outside public park",
            "Streetlight pole junction box open with exposed terminals",
            "Waterlogging in underpass after moderate showers",
            "Deep rutting and asphalt deformation near truck terminal",
            "Blocked roadside rainwater grating preventing drainage",
            "Contaminated tap water with brownish tint in block C",
            "Illegal garbage dumping behind commercial complex",
            "Non-working streetlights outside girls high school",
            "Pothole cluster expanding near railway level crossing",
            "Sewage backflow into storm canal creating vector hazard",
            "Pipeline valve leak causing road sub-base erosion",
            "Overflowing recycling station bins spilling on road",
            "High mast light failure at major transit circle",
            "Sewer chamber overflowing near fruit market",
            "Road sunken after utility trench backfilling",
            "Stagnant pool of water harboring mosquito larvae",
            "Water tanker supply needed due to mains maintenance"
        };

        ComplaintCategory[] cats = {
            ComplaintCategory.DRAINAGE_PROBLEMS, ComplaintCategory.WATER_SUPPLY, ComplaintCategory.GARBAGE_OVERFLOW,
            ComplaintCategory.STREETLIGHT_PROBLEMS, ComplaintCategory.WATERLOGGING, ComplaintCategory.ROAD_DAMAGE,
            ComplaintCategory.DRAINAGE_PROBLEMS, ComplaintCategory.WATER_SUPPLY, ComplaintCategory.GARBAGE_OVERFLOW,
            ComplaintCategory.STREETLIGHT_PROBLEMS, ComplaintCategory.WATERLOGGING, ComplaintCategory.ROAD_DAMAGE,
            ComplaintCategory.DRAINAGE_PROBLEMS, ComplaintCategory.WATER_SUPPLY, ComplaintCategory.GARBAGE_OVERFLOW,
            ComplaintCategory.STREETLIGHT_PROBLEMS, ComplaintCategory.ROAD_DAMAGE, ComplaintCategory.DRAINAGE_PROBLEMS,
            ComplaintCategory.WATER_SUPPLY, ComplaintCategory.GARBAGE_OVERFLOW, ComplaintCategory.STREETLIGHT_PROBLEMS,
            ComplaintCategory.DRAINAGE_PROBLEMS, ComplaintCategory.ROAD_DAMAGE, ComplaintCategory.WATERLOGGING,
            ComplaintCategory.WATER_SUPPLY
        };

        Priority[] prios = {
            Priority.HIGH, Priority.LOW, Priority.LOW, Priority.LOW, Priority.CRITICAL,
            Priority.MEDIUM, Priority.HIGH, Priority.MEDIUM, Priority.LOW, Priority.CRITICAL,
            Priority.HIGH, Priority.MEDIUM, Priority.HIGH, Priority.CRITICAL, Priority.MEDIUM,
            Priority.HIGH, Priority.HIGH, Priority.CRITICAL, Priority.HIGH, Priority.LOW,
            Priority.MEDIUM, Priority.HIGH, Priority.MEDIUM, Priority.MEDIUM, Priority.LOW
        };

        ComplaintStatus[] statuses = {
            ComplaintStatus.ASSIGNED, ComplaintStatus.RESOLVED, ComplaintStatus.SUBMITTED,
            ComplaintStatus.RESOLVED, ComplaintStatus.IN_PROGRESS, ComplaintStatus.ASSIGNED,
            ComplaintStatus.IN_PROGRESS, ComplaintStatus.RESOLVED, ComplaintStatus.SUBMITTED,
            ComplaintStatus.ASSIGNED, ComplaintStatus.IN_PROGRESS, ComplaintStatus.ASSIGNED,
            ComplaintStatus.UNDER_REVIEW, ComplaintStatus.IN_PROGRESS, ComplaintStatus.SUBMITTED,
            ComplaintStatus.ASSIGNED, ComplaintStatus.IN_PROGRESS, ComplaintStatus.IN_PROGRESS,
            ComplaintStatus.RESOLVED, ComplaintStatus.SUBMITTED, ComplaintStatus.ASSIGNED,
            ComplaintStatus.IN_PROGRESS, ComplaintStatus.ASSIGNED, ComplaintStatus.UNDER_REVIEW,
            ComplaintStatus.RESOLVED
        };

        Department[] depts = {
            deptDrainage, deptWater, deptWaste, deptElectrical, deptDrainage, deptRoads,
            deptDrainage, deptWater, deptWaste, deptElectrical, deptDrainage, deptRoads,
            deptDrainage, deptWater, deptWaste, deptElectrical, deptRoads, deptDrainage,
            deptWater, deptWaste, deptElectrical, deptDrainage, deptRoads, deptDrainage, deptWater
        };

        Random rand = new Random(42);

        for (int i = 0; i < titles.length; i++) {
            String trackId = "CP-2026-" + (1016 + i);
            double offsetLat = (rand.nextDouble() - 0.5) * 0.08;
            double offsetLng = (rand.nextDouble() - 0.5) * 0.08;

            seedList.add(new ComplaintData(
                trackId,
                titles[i],
                titles[i] + ". Reported by concerned resident for prompt civic attention and remediation.",
                cats[i],
                "Sector " + (rand.nextInt(15) + 1) + ", Municipal Ward " + (rand.nextInt(40) + 10),
                baseLat + offsetLat,
                baseLng + offsetLng,
                prios[i],
                statuses[i],
                depts[i],
                "AI Summary: " + titles[i] + " logged.",
                "Assigned " + prios[i].name() + " priority (" + prios[i].getSla() + " SLA) based on location impact and safety score.",
                false, null
            ));
        }

        // Persist complaints and build status history
        Map<String, Complaint> savedMap = new HashMap<>();

        int userIdx = 0;
        for (ComplaintData data : seedList) {
            User citizen = citizens.get(userIdx % citizens.size());
            userIdx++;

            Complaint c = new Complaint();
            c.setTrackingId(data.trackingId);
            c.setCitizen(citizen);
            c.setTitle(data.title);
            c.setDescription(data.description);
            c.setCategory(data.category);
            c.setAddress(data.address);
            c.setLatitude(data.latitude);
            c.setLongitude(data.longitude);
            c.setPriority(data.priority);
            c.setStatus(data.status);
            c.setAssignedDepartment(data.department);
            c.setAiSummary(data.aiSummary);
            c.setAiExplanation(data.aiExplanation);
            c.setIsDuplicate(data.isDuplicate);
            c.setCreatedAt(LocalDateTime.now().minusHours(rand.nextInt(72) + 2));
            c.setUpdatedAt(LocalDateTime.now().minusMinutes(rand.nextInt(60)));

            Complaint saved = complaintRepository.save(c);
            savedMap.put(data.trackingId, saved);

            // Record initial SUBMITTED history
            ComplaintStatusHistory h1 = new ComplaintStatusHistory(
                saved,
                ComplaintStatus.SUBMITTED,
                ComplaintStatus.SUBMITTED,
                "Complaint filed via CivicPulse Portal. AI assigned " + saved.getCategory().getDisplayName() + " with " + saved.getPriority().name() + " priority.",
                citizen
            );
            h1.setCreatedAt(saved.getCreatedAt());
            historyRepository.save(h1);

            // If ASSIGNED or higher, add history and assignment
            if (saved.getStatus() != ComplaintStatus.SUBMITTED && saved.getStatus() != ComplaintStatus.UNDER_REVIEW) {
                ComplaintAssignment assign = new ComplaintAssignment(
                    saved,
                    data.department,
                    admin,
                    "Dispatched to field maintenance unit for site assessment."
                );
                assign.setCreatedAt(saved.getCreatedAt().plusMinutes(20));
                assignmentRepository.save(assign);

                ComplaintStatusHistory h2 = new ComplaintStatusHistory(
                    saved,
                    ComplaintStatus.SUBMITTED,
                    ComplaintStatus.ASSIGNED,
                    "Assigned to " + data.department.getName(),
                    admin
                );
                h2.setCreatedAt(saved.getCreatedAt().plusMinutes(25));
                historyRepository.save(h2);
            }

            // If IN_PROGRESS or higher
            if (saved.getStatus() == ComplaintStatus.IN_PROGRESS || saved.getStatus() == ComplaintStatus.RESOLVED) {
                ComplaintStatusHistory h3 = new ComplaintStatusHistory(
                    saved,
                    ComplaintStatus.ASSIGNED,
                    ComplaintStatus.IN_PROGRESS,
                    "Field crew dispatched on-site; work underway.",
                    admin
                );
                h3.setCreatedAt(saved.getCreatedAt().plusHours(1));
                historyRepository.save(h3);
            }

            // If RESOLVED
            if (saved.getStatus() == ComplaintStatus.RESOLVED) {
                ComplaintStatusHistory h4 = new ComplaintStatusHistory(
                    saved,
                    ComplaintStatus.IN_PROGRESS,
                    ComplaintStatus.RESOLVED,
                    "Remediation verified complete by Municipal Zonal Inspector.",
                    admin
                );
                h4.setCreatedAt(saved.getCreatedAt().plusHours(4));
                historyRepository.save(h4);
            }
        }

        // Link duplicates
        for (ComplaintData data : seedList) {
            if (data.isDuplicate && data.duplicateOfTrackingId != null) {
                Complaint current = savedMap.get(data.trackingId);
                Complaint master = savedMap.get(data.duplicateOfTrackingId);
                if (current != null && master != null) {
                    current.setDuplicateOf(master);
                    complaintRepository.save(current);
                }
            }
        }

        log.info("Successfully seeded {} complaints, 6 municipal departments, and 5 demo accounts.", seedList.size());
    }

    private static class ComplaintData {
        String trackingId;
        String title;
        String description;
        ComplaintCategory category;
        String address;
        double latitude;
        double longitude;
        Priority priority;
        ComplaintStatus status;
        Department department;
        String aiSummary;
        String aiExplanation;
        boolean isDuplicate;
        String duplicateOfTrackingId;

        public ComplaintData(String trackingId, String title, String description, ComplaintCategory category, String address, double latitude, double longitude, Priority priority, ComplaintStatus status, Department department, String aiSummary, String aiExplanation, boolean isDuplicate, String duplicateOfTrackingId) {
            this.trackingId = trackingId;
            this.title = title;
            this.description = description;
            this.category = category;
            this.address = address;
            this.latitude = latitude;
            this.longitude = longitude;
            this.priority = priority;
            this.status = status;
            this.department = department;
            this.aiSummary = aiSummary;
            this.aiExplanation = aiExplanation;
            this.isDuplicate = isDuplicate;
            this.duplicateOfTrackingId = duplicateOfTrackingId;
        }
    }
}
