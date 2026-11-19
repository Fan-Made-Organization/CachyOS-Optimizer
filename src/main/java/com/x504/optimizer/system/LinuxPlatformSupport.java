package com.x504.optimizer.system;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;

/** Detección conservadora de distribuciones y perfiles de herramientas Linux. */
public final class LinuxPlatformSupport {
    public enum Family { ARCH, DEBIAN, FEDORA, SUSE, ALPINE, GENTOO, VOID, NIXOS, UNKNOWN }
    public record Platform(String id, String name, String version, String idLike, Family family, String packageManager) {}
    private final Path osRelease;
    public LinuxPlatformSupport() { this(Path.of("/etc/os-release")); }
    public LinuxPlatformSupport(Path osRelease) { this.osRelease = osRelease.toAbsolutePath().normalize(); }

    public Platform detect() {
        Map<String,String> values = readRelease();
        String id = values.getOrDefault("ID", "unknown").toLowerCase(Locale.ROOT);
        String name = values.getOrDefault("PRETTY_NAME", values.getOrDefault("NAME", "Linux no identificado"));
        String version = values.getOrDefault("VERSION_ID", "");
        String like = values.getOrDefault("ID_LIKE", "").toLowerCase(Locale.ROOT);
        String tokens = (id + " " + like).replace('_',' ').replace('-',' ');
        Family family = classify(id, tokens);
        String manager = managerFor(family);
        if (family == Family.UNKNOWN && manager.equals("No detectado")) manager = firstAvailable("pacman","apt-get","apt","dnf","yum","zypper","apk","emerge","xbps-install","nix-env");
        return new Platform(id, name, version, like, family, manager);
    }

    static Family classify(String id, String tokens) {
        if (List.of("cachyos","arch","archlinux","manjaro","endeavouros","garuda","arcolinux","artix","rebornos","steamos","holoiso","archcraft","blendos").contains(id) || has(tokens,"arch","manjaro","endeavouros","cachyos")) return Family.ARCH;
        if (List.of("debian","ubuntu","linuxmint","pop","elementary","kali","zorin","mx","deepin","raspbian","parrot","devuan","neon","kubuntu","xubuntu","lubuntu","ubuntu-mate","ubuntu-budgie","bodhi","antix","siduction","trisquel","pureos").contains(id) || has(tokens,"debian","ubuntu","linuxmint","elementary","kali")) return Family.DEBIAN;
        if (List.of("fedora","rhel","centos","rocky","almalinux","ol","oracle","nobara","ultramarine","bazzite","bluefin","silverblue","kinoite").contains(id) || has(tokens,"fedora","rhel","centos","rocky","almalinux")) return Family.FEDORA;
        if (id.startsWith("opensuse") || List.of("sles","sled","tumbleweed","geckolinux").contains(id) || has(tokens,"opensuse","suse")) return Family.SUSE;
        if (id.equals("alpine") || has(tokens,"alpine")) return Family.ALPINE;
        if (id.equals("gentoo") || id.equals("calculate") || id.equals("funtoo") || has(tokens,"gentoo")) return Family.GENTOO;
        if (id.equals("void") || has(tokens,"void")) return Family.VOID;
        if (id.equals("nixos") || has(tokens,"nixos")) return Family.NIXOS;
        return Family.UNKNOWN;
    }
    private static boolean has(String text, String... words) { List<String> tokens = List.of(text.trim().split("\\s+")); for (String word : words) if (tokens.contains(word)) return true; return false; }
    private String managerFor(Family family) {
        return switch (family) {
            case ARCH -> available("pacman") ? "pacman" : "No detectado";
            case DEBIAN -> available("apt-get") ? "apt-get" : available("apt") ? "apt" : "No detectado";
            case FEDORA -> available("dnf5") ? "dnf5" : available("dnf") ? "dnf" : available("yum") ? "yum" : "No detectado";
            case SUSE -> available("zypper") ? "zypper" : "No detectado";
            case ALPINE -> available("apk") ? "apk" : "No detectado";
            case GENTOO -> available("emerge") ? "emerge" : "No detectado";
            case VOID -> available("xbps-install") ? "xbps-install" : "No detectado";
            case NIXOS -> available("nix-env") ? "nix-env" : "No detectado";
            case UNKNOWN -> "No detectado";
        };
    }
    /** Categorías de herramientas con nombres de paquete adaptados a cada familia. */
    public Map<String,List<String>> toolCategories(Platform p) {
        Map<String,List<String>> result = new java.util.LinkedHashMap<>();
        if (isImmutableOrImageBased(p)) return java.util.Collections.unmodifiableMap(result);
        switch (p.family()) {
            case ARCH -> {
                result.put("Diagnóstico y hardware", List.of("inxi","lshw","dmidecode","smartmontools","lm_sensors","pciutils","usbutils"));
                result.put("Gráficos y vídeo", List.of("vulkan-tools","mesa-utils","libva-utils","vdpauinfo"));
                result.put("Almacenamiento", List.of("nvme-cli","hdparm"));
                result.put("Red", List.of("iperf3","ethtool","bind","traceroute"));
                result.put("Gaming", List.of("gamemode","mangohud","gamescope","goverlay","vkbasalt","protontricks","lib32-gamemode","lib32-mangohud"));
                result.put("Audio y multimedia", List.of("alsa-utils","pavucontrol","ffmpeg","v4l-utils"));
                result.put("Desarrollo y depuración", List.of("strace","gdb","valgrind"));
                result.put("Energía y sensores", List.of("acpi","upower"));
            }
            case DEBIAN -> {
                result.put("Diagnóstico y hardware", List.of("inxi","lshw","dmidecode","smartmontools","lm-sensors","pciutils","usbutils"));
                result.put("Gráficos y vídeo", List.of("vulkan-tools","mesa-utils","vainfo",));
                result.put("Almacenamiento", List.of("nvme-cli","hdparm",));
                result.put("Red", List.of("iperf3","ethtool","dnsutils","traceroute"));
                result.put("Gaming", List.of("gamemode","mangohud","gamescope","protontricks"));
                result.put("Audio y multimedia", List.of("alsa-utils","pavucontrol","ffmpeg","v4l-utils"));
                result.put("Desarrollo y depuración", List.of("strace","gdb","valgrind"));
                result.put("Energía y sensores", List.of(,"acpi","upower"));
            }
            case FEDORA -> {
                result.put("Diagnóstico y hardware", List.of("inxi","lshw","dmidecode","smartmontools","lm_sensors","pciutils","usbutils"));
                result.put("Gráficos y vídeo", List.of("vulkan-tools","mesa-demos","libva-utils",));
                result.put("Almacenamiento", List.of("nvme-cli","hdparm",));
                result.put("Red", List.of("iperf3","ethtool","bind-utils","traceroute"));
                result.put("Gaming", List.of("gamemode","mangohud","gamescope","protontricks"));
            }
            case SUSE -> {
                result.put("Diagnóstico y hardware", List.of("inxi","lshw","dmidecode","smartmontools","sensors","pciutils","usbutils"));
                result.put("Gráficos y vídeo", List.of("vulkan-tools","Mesa-demo-x","libva-utils",));
                result.put("Almacenamiento", List.of("nvme-cli","hdparm",));
                result.put("Red", List.of("iperf3","ethtool","bind-utils","traceroute"));
                result.put("Gaming", List.of("gamemode","mangohud","gamescope"));
                result.put("Audio y multimedia", List.of("alsa-utils","pavucontrol","ffmpeg","v4l-utils"));
                result.put("Desarrollo y depuración", List.of("strace","gdb","valgrind"));
                result.put("Energía y sensores", List.of(,"acpi","upower"));
            }
            case ALPINE -> {
                result.put("Diagnóstico y hardware", List.of("inxi","smartmontools","lm-sensors","pciutils","usbutils"));
                result.put("Gráficos y vídeo", List.of("vulkan-tools","mesa-utils"));
                result.put("Almacenamiento", List.of("nvme-cli","hdparm"));
                result.put("Red", List.of("iperf3","ethtool","bind-tools"));
                result.put("Audio y multimedia", List.of("alsa-utils","pavucontrol","ffmpeg","v4l-utils"));
                result.put("Desarrollo y depuración", List.of("strace","gdb","valgrind"));
            }
            case GENTOO, VOID, NIXOS, UNKNOWN -> { }
        }
        return java.util.Collections.unmodifiableMap(result);
    }
    public List<String> diagnosticTools(Platform p) {
        return toolCategories(p).values().stream().flatMap(List::stream).distinct().toList();
    }

    /**
     * Detecta sistemas de imagen inmutable o gestionados por el proveedor.
     * En ellos no se deben ofrecer instalaciones genéricas con el gestor del host.
     */
    public boolean isImmutableOrImageBased(Platform p) {
        String id = p.id().toLowerCase(Locale.ROOT);
        if (List.of("steamos", "bazzite", "bluefin", "silverblue", "kinoite",
                "aurora", "coreos", "fedora-iot").contains(id)) return true;
        return Files.exists(Path.of("/run/ostree-booted"))
                || Files.exists(Path.of("/etc/steamos-release"));
    }

    public List<String> installCommand(Platform p, List<String> packages) {
        if (isImmutableOrImageBased(p)) {
            throw new IllegalStateException("Sistema inmutable o basado en imagen: usa el método de instalación recomendado por su distribución.");
        }
        if (packages == null || packages.isEmpty() || packages.stream().anyMatch(x -> !x.matches("[A-Za-z0-9@._+-]+"))) throw new IllegalArgumentException("Lista de paquetes no válida.");
        List<String> cmd = new ArrayList<>();
        switch (p.family()) {
            case ARCH -> { require(p,"pacman"); cmd.addAll(List.of("sudo","pacman","-S","--needed")); }
            case DEBIAN -> { requireAny(p,"apt-get","apt"); cmd.addAll(List.of("sudo",p.packageManager(),"install","--no-install-recommends")); }
            case FEDORA -> { requireAny(p,"dnf5","dnf","yum"); cmd.addAll(List.of("sudo",p.packageManager(),"install")); }
            case SUSE -> { require(p,"zypper"); cmd.addAll(List.of("sudo","zypper","install")); }
            case ALPINE -> { require(p,"apk"); cmd.addAll(List.of("sudo","apk","add")); }
            case GENTOO -> { require(p,"emerge"); cmd.addAll(List.of("sudo","emerge","--ask")); }
            case VOID -> { require(p,"xbps-install"); cmd.addAll(List.of("sudo","xbps-install")); }
            case NIXOS, UNKNOWN -> throw new IllegalStateException("No hay instalador automático validado para esta distribución.");
        }
        cmd.addAll(List.copyOf(packages)); return List.copyOf(cmd);
    }
    private static void require(Platform p,String manager) { if (!p.packageManager().equals(manager)) throw new IllegalStateException(manager+" no está disponible."); }
    private static void requireAny(Platform p,String... managers) { if (!List.of(managers).contains(p.packageManager())) throw new IllegalStateException("Gestor de paquetes no disponible."); }
    public int install(Platform p,List<String> packages) throws IOException,InterruptedException {
        List<String> command=installCommand(p,packages); return new ProcessBuilder(command).inheritIO().start().waitFor();
    }
    private Map<String,String> readRelease() {
        Map<String,String> values=new HashMap<>();
        List<Path> candidates=List.of(osRelease,Path.of("/usr/lib/os-release"));
        for (Path candidate:candidates) {
            if (!Files.isReadable(candidate) || !Files.isRegularFile(candidate)) continue;
            try { for(String line:Files.readAllLines(candidate)) { int i=line.indexOf('='); if(i<=0 || line.startsWith("#")) continue; String value=line.substring(i+1).strip(); if(value.length()>=2 && ((value.startsWith("\"")&&value.endsWith("\""))||(value.startsWith("'")&&value.endsWith("'")))) value=value.substring(1,value.length()-1); values.put(line.substring(0,i),value); } }
            catch(IOException ignored) { }
            if(!values.isEmpty()) return values;
        }
        if (values.isEmpty()) readLegacyReleaseMarkers(values);
        return values;
    }

    /** Respaldo para instalaciones minimalistas que no exponen os-release. */
    private static void readLegacyReleaseMarkers(Map<String,String> values) {
        if (Files.exists(Path.of("/etc/cachyos-release"))) { values.put("ID", "cachyos"); values.put("ID_LIKE", "arch"); values.put("PRETTY_NAME", "CachyOS"); return; }
        if (Files.exists(Path.of("/etc/arch-release"))) { values.put("ID", "arch"); values.put("PRETTY_NAME", "Arch Linux"); return; }
        if (Files.exists(Path.of("/etc/alpine-release"))) { values.put("ID", "alpine"); values.put("PRETTY_NAME", "Alpine Linux"); try { values.put("VERSION_ID", Files.readString(Path.of("/etc/alpine-release")).strip()); } catch (IOException ignored) { } return; }
        if (Files.exists(Path.of("/etc/debian_version"))) { values.put("ID", "debian"); values.put("PRETTY_NAME", "Debian (identificado por marcador)"); try { values.put("VERSION_ID", Files.readString(Path.of("/etc/debian_version")).strip()); } catch (IOException ignored) { } return; }
        if (Files.exists(Path.of("/etc/fedora-release")) || Files.exists(Path.of("/etc/redhat-release"))) { values.put("ID", "fedora"); values.put("PRETTY_NAME", "Fedora/RHEL (identificado por marcador)"); return; }
        if (Files.exists(Path.of("/etc/SuSE-release"))) { values.put("ID", "opensuse"); values.put("ID_LIKE", "suse"); values.put("PRETTY_NAME", "SUSE (identificado por marcador)"); return; }
        if (Files.exists(Path.of("/etc/gentoo-release"))) { values.put("ID", "gentoo"); values.put("PRETTY_NAME", "Gentoo"); return; }
        if (Files.exists(Path.of("/etc/void-release"))) { values.put("ID", "void"); values.put("PRETTY_NAME", "Void Linux"); return; }
        if (Files.exists(Path.of("/etc/NIXOS"))) { values.put("ID", "nixos"); values.put("PRETTY_NAME", "NixOS"); }
    }
    private static boolean available(String name) { String path=System.getenv("PATH"); if(path==null)return false; for(String d:path.split(java.io.File.pathSeparator)) try { if(Files.isExecutable(Path.of(d,name))) return true; } catch(RuntimeException ignored){} return false; }
    private static String firstAvailable(String... names) { for(String n:names) if(available(n)) return n; return "No detectado"; }
}