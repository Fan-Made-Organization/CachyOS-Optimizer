package com.x504.optimizer.compatibility;

import java.util.Objects;

public record CompatibilityItem(String component, CompatibilityStatus status, String explanation) {
    public CompatibilityItem {
        component = Objects.requireNonNull(component);
        status = Objects.requireNonNull(status);
        explanation = Objects.requireNonNullElse(explanation, "");
    }
}
