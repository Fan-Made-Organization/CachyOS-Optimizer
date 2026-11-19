package com.x504.optimizer.benchmark;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.List;

/** Historial CSV sencillo para comparar ejecuciones en el mismo equipo. */
public final class BenchmarkHistory {
    private static final String HEADER = "timestamp,mib,cpu_ops_per_second,ram_write_mib_s,ram_read_mib_s,storage_write_mib_s,storage_read_mib_s";
    public void append(Path file, int mib, long cpuOps, BenchmarkSuite.MemoryResult ram,
                       BenchmarkSuite.StorageResult storage) throws IOException {
        Path target=file.toAbsolutePath().normalize();
        if(Files.isSymbolicLink(target)) throw new IOException("No se escribe en enlaces simbólicos.");
        if(Files.exists(target) && Files.size(target)>2_000_000) throw new IOException("El historial supera 2 MiB.");
        boolean fresh=!Files.exists(target);
        if(target.getParent()!=null) Files.createDirectories(target.getParent());
        String row=String.join(",",Instant.now().toString(),Integer.toString(mib),Long.toString(cpuOps),
                Double.toString(ram.writeMiBs()),Double.toString(ram.readMiBs()),
                Double.toString(storage.writeMiBs()),Double.toString(storage.readMiBs()));
        Files.writeString(target,(fresh?HEADER+System.lineSeparator():"")+row+System.lineSeparator(),
                StandardOpenOption.CREATE,StandardOpenOption.APPEND,LinkOption.NOFOLLOW_LINKS);
    }
    public Comparison compare(Path before,Path after) throws IOException {
        Values a=last(before),b=last(after);
        return new Comparison(change(a.cpu,b.cpu),change(a.ramRead,b.ramRead),
                change(a.diskRead,b.diskRead),a.timestamp,b.timestamp);
    }
    private static Values last(Path file)throws IOException{
        if(Files.isSymbolicLink(file))throw new IOException("No se leen enlaces simbólicos.");
        List<String> lines=Files.readAllLines(file);
        if(lines.size()<2||!lines.getFirst().equals(HEADER))throw new IOException("Historial vacío o formato desconocido: "+file);
        String[] p=lines.getLast().split(",",-1);
        if(p.length!=7)throw new IOException("Última fila incompleta: "+file);
        return new Values(p[0],Double.parseDouble(p[2]),Double.parseDouble(p[4]),Double.parseDouble(p[6]));
    }
    private static double change(double before,double after){return before==0?0:(after-before)*100.0/Math.abs(before);}
    private record Values(String timestamp,double cpu,double ramRead,double diskRead){}
    public record Comparison(double cpuPercent,double ramReadPercent,double storageReadPercent,
                             String beforeTimestamp,String afterTimestamp){}
}