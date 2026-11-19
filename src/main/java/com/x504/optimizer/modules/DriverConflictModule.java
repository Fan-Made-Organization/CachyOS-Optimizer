package com.x504.optimizer.modules;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Detecta señales observables de controladores sin asociación; no declara conflictos sin evidencia. */
public final class DriverConflictModule implements DiagnosticModule {
    public String id(){return "driver-conflict-check";}
    public String name(){return "Driver Conflict Check";}
    public ModuleReport inspect(){
        List<String> findings=new ArrayList<>(),warnings=new ArrayList<>();
        Path root=Path.of("/sys/bus/pci/devices");
        int unbound=0,checked=0;
        if(Files.isDirectory(root))try(var stream=Files.list(root)){
            for(Path p:stream.limit(100).toList()){
                checked++;
                if(!Files.isSymbolicLink(p.resolve("driver"))){unbound++;findings.add("PCI "+p.getFileName()+": sin enlace driver visible; requiere identificación del dispositivo.");}
            }
        }catch(Exception ex){warnings.add("No se pudo revisar el enlace de controladores PCI.");}
        findings.add(0,"Dispositivos PCI revisados: "+checked+"; sin enlace visible: "+unbound+".");
        warnings.add("Esto no demuestra un conflicto: dispositivos sin controlador pueden ser normales. Para confirmar conflictos se necesitan registros del kernel y contexto del dispositivo.");
        warnings.add("No se descargan, cargan ni sustituyen controladores.");
        return new ModuleReport(id(),name(),findings,warnings);
    }
}