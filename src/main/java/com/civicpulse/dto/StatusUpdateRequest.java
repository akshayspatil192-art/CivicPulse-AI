package com.civicpulse.dto;

import com.civicpulse.entity.ComplaintStatus;
import jakarta.validation.constraints.NotNull;

public class StatusUpdateRequest {
    @NotNull(message = "New status is required")
    private ComplaintStatus status;

    private String remarks;

    public StatusUpdateRequest() {}

    public ComplaintStatus getStatus() { return status; }
    public void setStatus(ComplaintStatus status) { this.status = status; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
