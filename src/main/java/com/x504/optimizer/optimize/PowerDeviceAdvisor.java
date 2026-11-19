package com.x504.optimizer.optimize;

import java.nio.file.Files;
import java.nio.file.Path;

/** Recomienda perfiles según el tipo de equipo; nunca cambia la energía automáticamente. */
public final class PowerDeviceAdvisor {
 public enum DeviceType { LAPTOP, DESKTOP, UNKNOWN }
 public record Advice(DeviceType type,String recommendedProfile,String explanation,boolean batteryPresent) {}
 public Advice inspect() {
  Path root=Path.of("/sys/class/power_supply"); boolean battery=false;
  if(Files.isDirectory(root)) try(var entries=Files.list(root)) { battery=entries.anyMatch(p -> read(p.resolve("type")).equalsIgnoreCase("Battery")); } catch(java.io.IOException ignored) {}
  if(battery) return new Advice(DeviceType.LAPTOP,"balanced","Se detectó batería. Equilibrado es un punto de partida; ahorro prioriza autonomía y rendimiento puede aumentar consumo y temperatura.",true);
  if(Files.isDirectory(root)) return new Advice(DeviceType.DESKTOP,"balanced","No se detectó batería. Equilibrado es una base conservadora; rendimiento solo cuando la carga lo justifique.",false);
  return new Advice(DeviceType.UNKNOWN,"balanced","No se pudo determinar el tipo de equipo; se recomienda equilibrado.",false);
 }
 private static String read(Path p) { try{return Files.isReadable(p)?Files.readString(p).strip():"";}catch(java.io.IOException ex){return "";} }
}
