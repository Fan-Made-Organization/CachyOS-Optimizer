package com.x504.optimizer.benchmark;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

/** Pruebas sintéticas acotadas de RAM y almacenamiento. No son benchmarks de GPU. */
public final class BenchmarkSuite {
    public MemoryResult benchmarkMemory(int mebibytes) {
        if (mebibytes < 8 || mebibytes > 64) throw new IllegalArgumentException("RAM: el tamaño debe ser de 8 a 64 MiB.");
        int size = mebibytes * 1024 * 1024;
        byte[] data = new byte[size];
        long writeStart = System.nanoTime();
        for (int i=0;i<size;i++) data[i]=(byte)(i*31);
        long writeNanos = System.nanoTime()-writeStart;
        long checksum=0;
        long readStart=System.nanoTime();
        for(byte b:data) checksum += b & 0xff;
        long readNanos=System.nanoTime()-readStart;
        return new MemoryResult(mebibytes, rate(size,writeNanos), rate(size,readNanos), checksum,
                "Prueba Java en memoria; influida por JIT y caché, no equivale a ancho de banda DRAM medido por hardware.");
    }
    public StorageResult benchmarkStorage(int mebibytes) throws IOException {
        if (mebibytes < 8 || mebibytes > 32) throw new IllegalArgumentException("Almacenamiento: el tamaño debe ser de 8 a 32 MiB.");
        Path temp=Files.createTempFile("cachyos-optimizer-bench-", ".tmp");
        long bytes=(long)mebibytes*1024*1024;
        byte[] block=new byte[1024*1024];
        for(int i=0;i<block.length;i++) block[i]=(byte)(i*17);
        long writeStart=System.nanoTime();
        try {
            try(FileChannel channel=FileChannel.open(temp,StandardOpenOption.WRITE,StandardOpenOption.TRUNCATE_EXISTING)) {
                for(int i=0;i<mebibytes;i++) {
                    ByteBuffer buffer=ByteBuffer.wrap(block);
                    while(buffer.hasRemaining()) channel.write(buffer);
                }
                channel.force(true);
            }
            long writeNanos=System.nanoTime()-writeStart;
            long checksum=0;
            long readStart=System.nanoTime();
            try(FileChannel channel=FileChannel.open(temp,StandardOpenOption.READ)) {
                ByteBuffer buffer=ByteBuffer.allocate(1024*1024);
                while(channel.read(buffer)>0) {
                    buffer.flip(); while(buffer.hasRemaining()) checksum+=buffer.get()&0xff; buffer.clear();
                }
            }
            long readNanos=System.nanoTime()-readStart;
            return new StorageResult(mebibytes,rate(bytes,writeNanos),rate(bytes,readNanos),checksum,
                    "Archivo temporal propio, sincronizado y eliminado al finalizar; resultados influidos por caché, sistema de archivos y dispositivo.");
        } finally { Files.deleteIfExists(temp); }
    }
    private static double rate(long bytes,long nanos) { return nanos<=0?0:bytes/(1024.0*1024.0)/(nanos/1_000_000_000.0); }
    public record MemoryResult(int mebibytes,double writeMiBs,double readMiBs,long checksum,String note) {}
    public record StorageResult(int mebibytes,double writeMiBs,double readMiBs,long checksum,String note) {}
}