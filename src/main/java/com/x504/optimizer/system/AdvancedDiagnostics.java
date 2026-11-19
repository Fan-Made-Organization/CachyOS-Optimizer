package com.x504.optimizer.system;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/** Inspección avanzada de solo lectura: almacenamiento, servicios, módulos y gaming. */
public final class AdvancedDiagnostics {
    public record Report(List<String> storage, List<String> services, List<String> kernel, List<String> gaming) {
        public Report { storage=List.copyOf(storage); services=List.copyOf(services); kernel=List.copyOf(kernel); gaming=List.copyOf(gaming); }
    }
    public Report inspect() { return new Report(inspectStorage(),inspectServices(),inspectKernel(),inspectGaming()); }
    List<String> inspectStorage() {
        List<String> out=new ArrayList<>(); Path root=Path.of("/sys/block");
        if(!Files.isDirectory(root)) return List.of("No está disponible /sys/block.");
        try(var devices=Files.list(root)) {
            for(Path d:devices.sorted().toList()) {
                String n=d.getFileName().toString();
                if(n.startsWith("loop")||n.startsWith("ram")||n.startsWith("zram")) continue;
                String rot=read(d.resolve("queue/rotational"),"desconocido");
                String kind=rot.equals("0")?"no rotacional (SSD/NVMe u otro)":rot.equals("1")?"rotacional (HDD)":"tipo no determinado";
                out.add(n+": "+kind+" | sectores: "+read(d.resolve("size"),"n/d")+" | scheduler: "+read(d.resolve("queue/scheduler"),"n/d"));
            }
        } catch(IOException ex) { out.add("No se pudo leer el inventario: "+ex.getMessage()); }
        if(out.isEmpty()) out.add("No se detectaron dispositivos de bloque físicos.");
        out.add("La interfaz sysfs no distingue por sí sola SATA SSD de NVMe; usa lsblk/nvme-cli para detalle adicional.");

        java.util.Set<String> filesystems = new java.util.LinkedHashSet<>();
        for (String mount : List.of("/", "/home", "/boot", "/boot/efi", "/efi")) {
            Path path = Path.of(mount);
            if (!Files.exists(path)) continue;
            try {
                var store = Files.getFileStore(path);
                String key = store.name() + "|" + store.type();
                if (filesystems.add(key)) {
                    out.add("Sistema de archivos en " + mount + ": " + store.type()
                            + " | dispositivo: " + store.name()
                            + " | libre: " + (store.getUsableSpace() / (1024L * 1024L)) + " MiB");
                }
            } catch (IOException | SecurityException ignored) {
                out.add("No se pudo consultar el sistema de archivos de " + mount + ".");
            }
        }
        return List.copyOf(out);
    }
    List<String> inspectServices() {
        CommandResult r=run(List.of("systemctl","--failed","--no-legend","--plain"),8);
        if(r==null) return List.of("systemctl no está disponible o el sistema no usa systemd.");
        if(r.code!=0) return List.of("No se pudo consultar servicios fallidos: "+r.output);
        return r.output.isBlank()?List.of("systemd no reporta unidades fallidas."):List.of("Unidades fallidas (revisar antes de actuar):",r.output.strip());
    }
    List<String> inspectKernel() {
        List<String> out=new ArrayList<>(); out.add("Kernel activo: "+System.getProperty("os.version")); out.add("Arquitectura: "+System.getProperty("os.arch"));
        CommandResult r=run(List.of("lsmod"),8);
        if(r==null||r.code!=0) out.add("No se pudo consultar lsmod.");
        else { String[] lines=r.output.split("\\R"); out.add("Módulos cargados: "+Math.max(0,lines.length-1));
            for(String line:lines) if(line.matches("(?i).*(nvidia|nouveau|amdgpu|i915|xe|vfio|vbox|vmwgfx).*")) out.add("Módulo relevante: "+line.strip()); }
        out.add("Kernel tainted: "+read(Path.of("/proc/sys/kernel/tainted"),"no disponible")+" (indicador orientativo, no diagnóstico concluyente).");
        return List.copyOf(out);
    }
    List<String> inspectGaming() {
        List<String> out=new ArrayList<>();
        for(String tool:List.of("steam","gamemoderun","mangohud","gamescope","protontricks","vulkaninfo","glxinfo","wine","winetricks"))
            out.add(tool+": "+(available(tool)?"disponible en PATH":"no detectado en PATH"));
        out.add("DXVK/VKD3D y Proton suelen gestionarse dentro de Steam/Proton o prefijos Wine; PATH no confirma su presencia.");
        out.add("No se ejecutan juegos, pruebas de estrés ni cambios.");
        return List.copyOf(out);
    }
    private static String read(Path p,String fallback) { try{return Files.isReadable(p)?Files.readString(p).strip():fallback;}catch(IOException e){return fallback;} }
    private static boolean available(String name) {
        String path=System.getenv("PATH"); if(path==null)return false;
        for(String dir:path.split(java.io.File.pathSeparator)) try{if(Files.isExecutable(Path.of(dir,name)))return true;}catch(RuntimeException ignored){}
        return false;
    }
    private static CommandResult run(List<String> cmd,int seconds) {
        Path output = null;
        try {
            output = Files.createTempFile("linux-optimizer-diagnostic-", ".log");
            Process process = new ProcessBuilder(cmd).redirectErrorStream(true).redirectOutput(output.toFile()).start();
            if (!process.waitFor(seconds, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                process.waitFor(1, TimeUnit.SECONDS);
                return null;
            }
            return new CommandResult(process.exitValue(),
                    Files.readString(output, java.nio.charset.StandardCharsets.UTF_8));
        } catch (IOException e) {
            return null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } finally {
            if (output != null) {
                try { Files.deleteIfExists(output); } catch (IOException ignored) { }
            }
        }
    }
    private record CommandResult(int code,String output){}
}