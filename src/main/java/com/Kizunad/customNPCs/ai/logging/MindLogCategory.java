package com.Kizunad.customNPCs.ai.logging;

public enum MindLogCategory {
    DECISION("decision"),
    PLANNING("planning"),
    EXECUTION("execution");

    private final String displayName;

    MindLogCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
