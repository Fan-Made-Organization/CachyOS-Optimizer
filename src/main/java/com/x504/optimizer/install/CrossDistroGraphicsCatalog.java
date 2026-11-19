package com.x504.optimizer.install;

import com.x504.optimizer.system.LinuxPlatformSupport;
import java.util.List;

/** Perfiles explícitos y conservadores de paquetes gráficos por familia Linux. */
public final class CrossDistroGraphicsCatalog {
    public enum Vendor { INTEL, AMD, NVIDIA, COMMON }
    public List<String> packages(Vendor vendor, LinuxPlatformSupport.Family family) {
        return switch(family) {
            case ARCH -> switch(vendor) {
                case INTEL -> List.of("mesa","lib32-mesa","vulkan-intel","lib32-vulkan-intel","intel-media-driver","vulkan-icd-loader","lib32-vulkan-icd-loader");
                case AMD -> List.of("mesa","lib32-mesa","vulkan-radeon","lib32-vulkan-radeon","libva-mesa-driver","vulkan-icd-loader","lib32-vulkan-icd-loader");
                case NVIDIA -> List.of("nvidia-open","nvidia-utils","lib32-nvidia-utils","vulkan-icd-loader","lib32-vulkan-icd-loader");
                case COMMON -> List.of("mesa","vulkan-icd-loader");
            };
            case DEBIAN -> switch(vendor) {
                case INTEL -> List.of("mesa-vulkan-drivers","libgl1-mesa-dri","intel-media-va-driver","vulkan-tools");
                case AMD -> List.of("mesa-vulkan-drivers","libgl1-mesa-dri","mesa-va-drivers","vulkan-tools");
                case NVIDIA -> List.of("nvidia-driver","nvidia-vulkan-icd","vulkan-tools");
                case COMMON -> List.of("mesa-vulkan-drivers","libgl1-mesa-dri","vulkan-tools");
            };
            case FEDORA -> vendor==Vendor.NVIDIA ? List.of() : List.of("mesa-dri-drivers","mesa-vulkan-drivers","mesa-va-drivers","vulkan-tools");
            case SUSE -> vendor==Vendor.NVIDIA ? List.of() : List.of("Mesa","Mesa-dri","libvulkan1","vulkan-tools");
            case ALPINE -> vendor==Vendor.NVIDIA ? List.of() : List.of("mesa-dri-gallium","mesa-vulkan-intel","mesa-vulkan-radeon","vulkan-tools");
            case GENTOO, VOID, NIXOS, UNKNOWN -> List.of();
        };
    }
    public List<String> compatibilityPackages(LinuxPlatformSupport.Family family) {
        return switch(family) {
            case ARCH -> List.of("dxvk","vkd3d","wine-staging","winetricks","gamescope","vulkan-icd-loader","lib32-vulkan-icd-loader");
            case DEBIAN -> List.of("wine","winetricks","gamescope","vulkan-tools");
            case FEDORA, SUSE -> List.of("wine","winetricks","gamescope","vulkan-tools");
            case ALPINE -> List.of("wine","winetricks","vulkan-tools");
            case GENTOO, VOID, NIXOS, UNKNOWN -> List.of();
        };
    }
}
