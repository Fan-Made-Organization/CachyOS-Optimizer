package com.x504.optimizer.hardware;

import java.util.List;

public record HardwareSnapshot(String processor, int logicalCores,
                                long totalMemoryKiB, List<String> graphicsAdapters) {
    public HardwareSnapshot {
        processor = processor == null || processor.isBlank() ? "Desconocido" : processor;
        graphicsAdapters = List.copyOf(graphicsAdapters);
    }

    public String toDisplayString() {
        String memory = totalMemoryKiB > 0
                ? String.format("%.2f GiB", totalMemoryKiB / 1024.0 / 1024.0)
                : "No disponible";
        String graphics = graphicsAdapters.isEmpty()
                ? "No detectados" : String.join(", ", graphicsAdapters);
        return """
                Procesador: %s
                Núcleos lógicos: %d
                Memoria RAM total: %s
                Adaptadores gráficos: %s""".formatted(processor, logicalCores, memory, graphics);
    }
}
