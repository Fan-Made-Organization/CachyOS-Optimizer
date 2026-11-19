package com.x504.optimizer.system;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Detecta familias Linux y gestores de paquetes sin asumir que todas las distros son iguales. */
public final class LinuxPlatformSupport {
    public enum Family { ARCH, DEBIAN, FEDORA, SUSE, UNKNOWN }
    public record Platform(String id, String name, String version, String idLike, Family family, String packageManager) {}
    private final Path osRelease;

    public LinuxPlatformSupport() { this(Path.of("/etc/os-release")); }
    public LinuxPlatformSupport(Path osRelease) { this.osRelease = osRelease.toAbsolutePath().normalize(); }

    public Platform detect() {
        var values = readRelease();
        String id = values.getOrDefault("ID", "unknown").toLowerCase(Locale.ROOT);
        String name = values.getOrDefault("PRETTY_NAME", values.getOrDefault("NAME", "Linux no identificado"));
        String version = values.getOrDefault("VERSION_ID", "");
        String like = values.getOrDefault("ID_LIKE", "").toLowerCase(Locale.ROOT);
        String tokens = (id + " " + like).toLowerCase(Locale.ROOT);
        Family family = tokens.matches(".*\\b(arch|manjaro|endeavouros|cachyos)\\b.*") ? Family.ARCH
                : tokens.matches(".*\\b(debian|ubuntu|linuxmint|pop|elementary|kali)\\b.*") ? Family.DEBIAN
                : tokens.matches(".*\\b(fedora|rhel|centos|rocky|almalinux)\\b.*") ? Family.FEDORA
                : tokens.matches(".*\\b(suse|opensuse)\\b.*") ? Family.SUSE : Family.UNKNOWN;
        String manager = switch (family) {
            case ARCH -> executable("pacman") ? "pacman" : "No detectado";
            case DEBIAN -> executable("apt-get") ? "apt-get" : executable("apt") ? "apt" : "No detectado";
            case FEDORA -> executable("dnf") ? "dnf" : executable("yum") ? "yum" : "No detectado";
            case SUSE -> executable("zypper") ? "zypper" : "No detectado";
            case UNKNOWN -> firstAvailable("pacman", "apt-get", "dnf", "zypper", "apk");
        };
        return new Platform(id, name, version, like, family, manager);
    }

    public List<String> diagnosticTools(Platform platform) {
        return switch (platform.family()) {
            case ARCH -> List.of("inxi", "smartmontools", "lm_sensors", "btop", "fastfetch", "vulkan-tools", "mesa-utils");
            case DEBIAN -> List.of("inxi", "smartmontools", "lm-sensors", "btop", "fastfetch", "vulkan-tools", "mesa-utils");
            case FEDORA -> List.of("inxi", "smartmontools", "lm_sensors", "btop", "fastfetch", "vulkan-tools", "mesa-demos");
            case SUSE -> List.of("inxi", "smartmontools", "sensors", "btop", "fastfetch", "vulkan-tools", "Mesa-demo-x");
            case UNKNOWN -> List.of();
        };
    }

    public List<String> installCommand(Platform platform, List<String> packages) {
        if (packages == null || packages.isEmpty() || packages.stream().anyMatch(p -> !p.matches("[A-Za-z0-9@._+-]+")))
            throw new IllegalArgumentException("Lista de paquetes no válida.");
        List<String> command = new ArrayList<>();
        switch (platform.family()) {
            case ARCH -> { if (!platform.packageManager().equals("pacman")) throw new IllegalStateException("Pacman no está disponible."); command.addAll(List.of("sudo", "pacman", "-S", "--needed")); }
            case DEBIAN -> { if (!platform.packageManager().equals("apt-get") && !platform.packageManager().equals("apt")) throw new IllegalStateException("APT no está disponible."); command.addAll(List.of("sudo", platform.packageManager(), "install", "--no-install-recommends")); }
            case FEDORA -> { if (!platform.packageManager().equals("dnf") && !platform.packageManager().equals("yum")) throw new IllegalStateException("DNF/YUM no está disponible."); command.addAll(List.of("sudo", platform.packageManager(), "install")); }
            case SUSE -> { if (!platform.packageManager().equals("zypper")) throw new IllegalStateException("Zypper no está disponible."); command.addAll(List.of("sudo", "zypper", "install")); }
            case UNKNOWN -> throw new IllegalStateException("Distribución no reconocida: no se ejecutará ningún instalador genérico.");
        }
        command.addAll(List.copyOf(packages));
        return List.copyOf(command);
    }

    public int install(Platform platform, List<String> packages) throws IOException, InterruptedException {
        List<String> command = installCommand(platform, packages);
        Process process = new ProcessBuilder(command).inheritIO().start();
        return process.waitFor();
    }

    private java.util.Map<String,String> readRelease() {
        var values = new java.util.HashMap<String,String>();
        if (!Files.isReadable(osRelease) || Files.isSymbolicLink(osRelease)) return values;
        try {
            for (String line : Files.readAllLines(osRelease)) {
                int i = line.indexOf('=');
                if (i <= 0 || line.startsWith("#")) continue;
                String value = line.substring(i + 1).strip();
                if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\""))
                    value = value.substring(1, value.length() - 1);
                values.put(line.substring(0, i), value);
            }
        } catch (IOException ignored) { }
        return values;
    }
    private static boolean executable(String name) {
        String path=System.getenv("PATH"); if(path==null) return false;
        for(String dir:path.split(java.io.File.pathSeparator)) try { if(Files.isExecutable(Path.of(dir,name))) return true; } catch(RuntimeException ignored) { }
        return false;
    }
    private static String firstAvailable(String... names) { for(String name:names) if(executable(name)) return name; return "No detectado"; }
}