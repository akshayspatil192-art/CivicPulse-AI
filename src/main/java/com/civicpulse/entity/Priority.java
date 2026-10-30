package com.civicpulse.entity;

public enum Priority {
    CRITICAL(1, "4 Hours"),
    HIGH(2, "24 Hours"),
    MEDIUM(3, "72 Hours"),
    LOW(4, "7 Days");

    private final int rank;
    private final String sla;

    Priority(int rank, String sla) {
        this.rank = rank;
        this.sla = sla;
    }

    public int getRank() {
        return rank;
    }

    public String getSla() {
        return sla;
    }
}
