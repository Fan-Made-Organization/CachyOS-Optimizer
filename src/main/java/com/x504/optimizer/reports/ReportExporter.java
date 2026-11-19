package com.x504.optimizer.reports;

import com.x504.optimizer.modules.DiagnosticModule;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

/** Exporta hallazgos a JSON y HTML sin dependencias externas. */
public final class ReportExporter {
    public Path exportJson(List<DiagnosticModule.ModuleReport> reports, Path destination) throws IOException {
        StringBuilder out = new StringBuilder("{\n  \"generatedAt\": \"").append(escape(Instant.now().toString())).append("\",\n  \"modules\": [");
        for (int i = 0; i < reports.size(); i++) {
            var r = reports.get(i);
            out.append(i == 0 ? "\n" : ",\n").append("    {\"id\":\"").append(escape(r.moduleId()))
                    .append("\",\"title\":\"").append(escape(r.title())).append("\",\"findings\":")
                    .append(array(r.findings())).append(",\"warnings\":").append(array(r.warnings())).append("}");
        }
        out.append("\n  ]\n}\n");
        return write(destination, out.toString());
    }
    public Path exportHtml(List<DiagnosticModule.ModuleReport> reports, Path destination) throws IOException {
        StringBuilder out = new StringBuilder("<!doctype html><html lang=\"es\"><meta charset=\"utf-8\"><meta name=\"viewport\" content=\"width=device-width\"><title>CachyOS Optimizer - Informe</title><style>body{font:16px system-ui;max-width:1000px;margin:2rem auto;padding:0 1rem;background:#101318;color:#e8edf2}section{background:#1b222c;padding:1rem;margin:1rem 0;border-radius:8px}li{margin:.35rem 0}.warning{color:#ffd166}</style><h1>CachyOS Optimizer</h1><p>Informe generado: ")
                .append(escape(Instant.now().toString())).append("</p>");
        for (var r : reports) {
            out.append("<section><h2>").append(html(r.title())).append("</h2><ul>");
            for (String f : r.findings()) out.append("<li>").append(html(f)).append("</li>");
            out.append("</ul>");
            if (!r.warnings().isEmpty()) {
                out.append("<h3>Advertencias</h3><ul class=\"warning\">");
                for (String w : r.warnings()) out.append("<li>").append(html(w)).append("</li>");
                out.append("</ul>");
            }
            out.append("</section>");
        }
        out.append("</html>");
        return write(destination, out.toString());
    }
    private static Path write(Path destination, String content) throws IOException {
        Path target = destination.toAbsolutePath().normalize();
        if (Files.exists(target)) throw new IOException("El destino ya existe; no se sobrescriben informes.");
        Path parent = target.getParent();
        if (parent != null) Files.createDirectories(parent);
        return Files.writeString(target, content, StandardCharsets.UTF_8);
    }
    private static String array(List<String> values) {
        return values.stream().map(v -> "\"" + escape(v) + "\"").reduce((a,b) -> a + "," + b).map(s -> "[" + s + "]").orElse("[]");
    }
    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }
    private static String html(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }
}