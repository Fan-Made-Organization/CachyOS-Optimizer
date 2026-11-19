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
        if (List.of("cachyos","arch","manjaro","endeavouros","garuda","arcolinux","artix","rebornos","steamos","holoiso").contains(id) || has(tokens,"arch","manjaro","endeavouros","cachyos")) return Family.ARCH;
        if (List.of("debian","ubuntu","linuxmint","pop","pop os","elementary","kali"," zorin","zorin","mx","mx linux","deepin","raspbian","parrot","devuan").contains(id) || has(tokens,"debian","ubuntu","linuxmint","pop","elementary","kali")) return Family.DEBIAN;
        if (List.of("fedora","rhel","centos","rocky","almalinux","ol","oracle").contains(id) || has(tokens,"fedora","rhel","centos","rocky","almalinux")) return Family.FEDORA;
        if (id.startsWith("opensuse") || List.of("sles","sled","opensuse","tumbleweed").contains(id) || has(tokens,"opensuse","suse")) return Family.SUSE;
        if (id.equals("alpine") || has(tokens,"alpine")) return Family.ALPINE;
        if (id.equals("gentoo") || id.equals("calculate") || has(tokens,"gentoo")) return Family.GENTOO;
        if (id.equals("void") || has(tokens,"void")) return Family.VOID;
        if (id.equals("nixos") || has(tokens,"nixos")) return Family.NIXOS;
        return Family.UNKNOWN;
    }
    private static boolean has(String text, String... words) { for (String word : words) if (java.util.regex.Pattern.compile("(^|\\\\s)" + java.util.regex.Pattern.quote(word) + "(\\\\s|$)").matcher(text).find()) return true; return false; }
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
    public List<String> diagnosticTools(Platform p) {
        return switch (p.family()) {
            case ARCH -> List.of("inxi","lshw","smartmontools","lm_sensors","btop","fastfetch","vulkan-tools","mesa-utils","libva-utils","nvtop","pciutils","usbutils","iperf3","ethtool");
            case DEBIAN -> List.of("inxi","lshw","smartmontools","lm-sensors","btop","fastfetch","vulkan-tools","mesa-utils","vainfo","pciutils","usbutils","iperf3","ethtool");
            case FEDORA -> List.of("inxi","lshw","smartmontools","lm_sensors","btop","fastfetch","vulkan-tools","mesa-demos","libva-utils","pciutils","usbutils","iperf3","ethtool");
            case SUSE -> List.of("inxi","lshw","smartmontools","sensors","btop","fastfetch","vulkan-tools","Mesa-demo-x","libva-utils","pciutils","usbutils","iperf3","ethtool");
            case ALPINE -> List.of("inxi","smartmontools","lm-sensors","btop","fastfetch","vulkan-tools","mesa-utils","pciutils","usbutils");
            case GENTOO -> List.of("inxi","smartmontools","lm_sensors","btop","fastfetch","vulkan-tools","mesa-progs","pciutils","usbutils");
            case VOID -> List.of("inxi","smartmontools","lm_sensors","btop","fastfetch","vulkan-tools","mesa-demos","pciutils","usbutils");
            case NIXOS, UNKNOWN -> List.of();
        };
    }
    public List<String> installCommand(Platform p, List<String> packages) {
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
            try { for(String line:Files.readAllLines(candidate)) { int i=line.indexOf('='); if(i<=0 || line.startsWith("#")) continue; String value=line.substring(i+1).strip(); if(value.length()>=2 && ((value.startsWith("\\\"")&&value.endsWith("\\\""))||(value.startsWith("'")&&value.endsWith("'")))) value=value.substring(1,value.length()-1); values.put(line.substring(0,i),value); } }
            catch(IOException ignored) { }
            if(!values.isEmpty()) return values;
        }
        return values;
    }
    private static boolean available(String name) { String path=System.getenv("PATH"); if(path==null)return false; for(String d:path.split(java.io.File.pathSeparator)) try { if(Files.isExecutable(Path.of(d,name))) return true; } catch(RuntimeException ignored){} return false; }
    private static String firstAvailable(String... names) { for(String n:names) if(available(n)) return n; return "No detectado"; }
}