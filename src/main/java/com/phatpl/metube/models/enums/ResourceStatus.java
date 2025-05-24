package com.phatpl.metube.models.enums;

public enum ResourceStatus {
    UPLOADING("Uploading"),
    WAITING("Waiting"),
    PROCESSING("Processing"), 
    READY("Ready");

    private final String label;

    ResourceStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
