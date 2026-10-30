package com.civicpulse.entity;

public enum ComplaintCategory {
    ROAD_DAMAGE("Road Damage"),
    WATERLOGGING("Waterlogging"),
    GARBAGE_OVERFLOW("Garbage Overflow"),
    DRAINAGE_PROBLEMS("Drainage Problems"),
    STREETLIGHT_PROBLEMS("Streetlight Problems"),
    WATER_SUPPLY("Water Supply"),
    OTHER("Other Civic Issues");

    private final String displayName;

    ComplaintCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
