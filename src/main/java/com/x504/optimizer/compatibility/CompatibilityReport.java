package com.x504.optimizer.compatibility;

import java.util.List;

public record CompatibilityReport(List<CompatibilityItem> items, int percentage,
                                  int evaluatedComponents, int unknownComponents) {
    public CompatibilityReport {
        items = List.copyOf(items);
        if (percentage < 0 || percentage > 100) throw new IllegalArgumentException("Porcentaje fuera de rango");
    }

    public String toDisplayString() {
        StringBuilder out = new StringBuilder("Compatibilidad estimada: ")
                .append(percentage).append("% (")
                .append(evaluatedComponents).append(" componentes evaluados, ")
                .append(unknownComponents).append(" desconocidos)\n");
        for (CompatibilityItem item : items) {
            out.append("- ").append(item.component()).append(": ")
                    .append(item.status()).append(" — ").append(item.explanation()).append('\n');
        }
        return out.toString().stripTrailing();
    }
}
