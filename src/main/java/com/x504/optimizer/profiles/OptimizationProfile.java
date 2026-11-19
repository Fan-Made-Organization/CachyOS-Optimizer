package com.x504.optimizer.profiles;

import java.util.List;

/** Perfil descriptivo; no aplica ajustes por sí solo. */
public record OptimizationProfile(String id, String name, String description,
                                  List<String> recommendations) {
    public OptimizationProfile {
        recommendations = List.copyOf(recommendations);
    }
}
