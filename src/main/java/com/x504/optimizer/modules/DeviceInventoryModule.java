package com.x504.optimizer.modules;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Inventario básico de PCI y USB mediante sysfs, sin requerir herramientas externas. */
public final class DeviceInventoryModule implements DiagnosticModule {
    public String id(){return "device-inventory";}
    public String name(){return "Device Inventory";}
    public ModuleReport inspect(){
        List<String> findings=new ArrayList<>(),warnings=new ArrayList<>();
        inventory(Path.of("/sys/bus/pci/devices"),"PCI",findings,warnings);
        inventory(Path.of("/sys/bus/usb/devices"),"USB",findings,warnings);
        return new ModuleReport(id(),name(),findings,warnings);
    }
    private static void inventory(Path root,String kind,List<String> out,List<String>warnings){
        if(!Files.isDirectory(root)){warnings.add("Inventario "+kind+" no disponible.");return;}
        try(var stream=Files.list(root)){
            var devices=stream.filter(p->Files.exists(p.resolve("idVendor"))||Files.exists(p.resolve("vendor"))).limit(100).toList();
            out.add(kind+": "+devices.size()+" dispositivos (máximo 100 mostrados).");
            for(Path p:devices){
                String vendor=LinuxProbe.firstLine(p.resolve(Files.exists(p.resolve("idVendor"))?"idVendor":"vendor"));
                String product=LinuxProbe.firstLine(p.resolve(Files.exists(p.resolve("idProduct"))?"idProduct":"device"));
                String driver="sin enlace visible";
                if(Files.isSymbolicLink(p.resolve("driver"))) try{driver=p.resolve("driver").toRealPath().getFileName().toString();}catch(Exception ignored){}
                out.add(kind+" "+p.getFileName()+" ["+vendor+":"+product+"] controlador="+driver);
            }
        }catch(Exception ex){warnings.add("No se pudo leer el inventario "+kind+".");}
    }
}