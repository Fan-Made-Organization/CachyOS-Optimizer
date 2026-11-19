package com.x504.optimizer.system;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/** Inspección avanzada de solo lectura: almacenamiento, servicios, módulos y gaming. */
public final class AdvancedDiagnostics {
    public record Report(List<String> cpu, List<String> memory, List<String> storage, List<String> network, List<String> graphics, List<String> services, List<String> kernel, List<String> gaming) {
        public Report { cpu=List.copyOf(cpu); memory=List.copyOf(memory); storage=List.copyOf(storage); network=List.copyOf(network); graphics=List.copyOf(graphics); services=List.copyOf(services); kernel=List.copyOf(kernel); gaming=List.copyOf(gaming); }
    }
    public Report inspect() { return new Report(inspectCpu(),inspectMemory(),inspectStorage(),inspectNetwork(),inspectGraphics(),inspectServices(),inspectKernel(),inspectGaming()); }
    List<String> inspectCpu() {
        List<String> out = new ArrayList<>();
        Path cpu = Path.of("/sys/devices/system/cpu");
        long online = 0;
        try (var paths = Files.list(cpu)) {
            for (Path path : paths.filter(p -> p.getFileName().toString().matches("cpu[0-9]+")).toList()) {
                if (read(path.resolve("online"), "1").equals("1")) online++;
            }
        } catch (IOException ignored) { }
        if (online == 0) online = Runtime.getRuntime().availableProcessors();
        out.add("Procesadores lógicos en línea (estimación): " + online);
        String model = "";
        try { for (String line : Files.readAllLines(Path.of("/proc/cpuinfo"))) if (line.startsWith("model name") || line.startsWith("Hardware")) { model = line.substring(line.indexOf(":") + 1).strip(); break; } }
        catch (IOException ignored) { }
        if (!model.isBlank()) out.add("Modelo detectado: " + model);
        Path freq = Path.of("/sys/devices/system/cpu/cpu0/cpufreq");
        out.add("Frecuencia actual CPU0: " + read(freq.resolve("scaling_cur_freq"), "no expuesta") + " kHz");
        out.add("Gobernador CPU0: " + read(freq.resolve("scaling_governor"), "no expuesto"));
        out.add("Rango de frecuencia CPU0: " + read(freq.resolve("scaling_min_freq"), "?") + "–" + read(freq.resolve("scaling_max_freq"), "?") + " kHz");
        out.add("La frecuencia puede variar dinámicamente; una lectura aislada no representa el rendimiento sostenido.");
        return List.copyOf(out);
    }
    List<String> inspectMemory() {
        List<String> out = new ArrayList<>();
        Path mem = Path.of("/proc/meminfo");
        try {
            for (String line : Files.readAllLines(mem)) {
                if (line.startsWith("MemTotal:") || line.startsWith("MemAvailable:") || line.startsWith("SwapTotal:") || line.startsWith("SwapFree:")) out.add(line.strip());
            }
        } catch (IOException ex) { out.add("No se pudo leer /proc/meminfo."); }
        Path swaps = Path.of("/proc/swaps");
        try {
            List<String> lines = Files.readAllLines(swaps);
            if (lines.size() <= 1) out.add("Swap: no hay dispositivos/archivos activos reportados.");
            else for (int i=1;i<lines.size();i++) out.add("Swap activo: " + lines.get(i).strip());
        } catch (IOException ex) { out.add("No se pudo leer /proc/swaps."); }
        Path pressure = Path.of("/proc/pressure");
        for (String resource : List.of("cpu","memory","io")) {
            Path file = pressure.resolve(resource);
            if (Files.isReadable(file)) try { for (String line : Files.readAllLines(file)) out.add("PSI " + resource + ": " + line.strip()); } catch (IOException ignored) { }
        }
        if (out.isEmpty()) out.add("No hay métricas de memoria disponibles.");
        out.add("PSI (Pressure Stall Information) mide tiempo de espera por recursos; no es un porcentaje directo de uso.");
        return List.copyOf(out);
    }
    List<String> inspectNetwork() {
        List<String> out = new ArrayList<>();
        Path root = Path.of("/sys/class/net");
        try (var paths = Files.list(root)) {
            for (Path p : paths.sorted().toList()) {
                String name = p.getFileName().toString();
                if (name.equals("lo")) continue;
                String state = read(p.resolve("operstate"), "desconocido");
                String carrier = read(p.resolve("carrier"), "");
                String mtu = read(p.resolve("mtu"), "n/d");
                String speed = read(p.resolve("speed"), "n/d");
                String mac = read(p.resolve("address"), "n/d");
                String wireless = Files.exists(p.resolve("wireless")) ? "Wi-Fi/radio" : "Ethernet/virtual u otro";
                out.add(name + ": " + wireless + " | estado " + state + " | carrier " + (carrier.isBlank()?"n/d":carrier) + " | MTU " + mtu + " | velocidad anunciada " + speed + " Mb/s | MAC " + mac);
            }
        } catch (IOException ex) { out.add("No se pudo consultar /sys/class/net."); }
        try {
            for (String line : Files.readAllLines(Path.of("/proc/net/route"))) {
                String[] fields = line.trim().split("\\s+");
                if (fields.length > 7 && fields[1].equals("00000000")) out.add("Ruta IPv4 predeterminada: interfaz " + fields[0] + " (tabla kernel).");
            }
        } catch (IOException ex) { out.add("Tabla de rutas IPv4 no disponible."); }
        if (out.isEmpty()) out.add("No se detectaron interfaces de red no-loopback.");
        out.add("No se realizan pruebas de velocidad, DNS, latencia ni conectividad externa.");
        return List.copyOf(out);
    }
    List<String> inspectGraphics() {
        List<String> out = new ArrayList<>();
        Path drm = Path.of("/sys/class/drm");
        try (var paths = Files.list(drm)) {
            for (Path card : paths.filter(p -> p.getFileName().toString().matches("card[0-9]+")).sorted().toList()) {
                Path device = card.resolve("device");
                String vendor = read(device.resolve("vendor"), "n/d");
                String id = read(device.resolve("device"), "n/d");
                String driver = "sin enlace de driver visible";
                try { Path link = device.resolve("driver"); if (Files.isSymbolicLink(link)) driver = Files.readSymbolicLink(link).getFileName().toString(); } catch (IOException ignored) { }
                String name = switch (vendor.toLowerCase()) { case "0x8086" -> "Intel"; case "0x1002" -> "AMD"; case "0x10de" -> "NVIDIA"; default -> "desconocido"; };
                String vramTotal = read(device.resolve("mem_info_vram_total"), "");
                String vramUsed = read(device.resolve("mem_info_vram_used"), "");
                String vram = vramTotal.isBlank() ? "VRAM no expuesta por sysfs" : "VRAM " + (Long.parseLong(vramTotal) / (1024L * 1024L)) + " MiB" + (vramUsed.isBlank() ? "" : " | usada " + (Long.parseLong(vramUsed) / (1024L * 1024L)) + " MiB");
                out.add(card.getFileName() + ": " + name + " | vendor " + vendor + " | device " + id + " | driver " + driver + " | " + vram);
            }
        } catch (IOException ex) { out.add("No se pudo consultar el subsistema DRM."); }
        if (out.isEmpty()) out.add("No se detectaron tarjetas DRM en /sys/class/drm.");
        for (String tool : List.of("vulkaninfo","glxinfo","vainfo","vdpauinfo")) out.add(tool + ": " + (available(tool) ? "disponible" : "no instalado/en PATH"));
        out.add("La presencia de un driver no garantiza aceleración funcional; valida Vulkan/OpenGL con las herramientas correspondientes.");
        return List.copyOf(out);
    }
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
        out.add("SMART: " + (available("smartctl") ? "smartctl disponible (no se ejecutan pruebas)" : "smartmontools no detectado"));
        out.add("NVMe CLI: " + (available("nvme") ? "nvme disponible (solo se detecta la herramienta)" : "nvme-cli no detectado"));
        out.add("La interfaz sysfs no distingue por sí sola SATA SSD de NVMe; usa lsblk/nvme-cli para detalle adicional.");
        out.add("No se ejecutan pruebas SMART, autodiagnósticos ni escrituras en los discos.");

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