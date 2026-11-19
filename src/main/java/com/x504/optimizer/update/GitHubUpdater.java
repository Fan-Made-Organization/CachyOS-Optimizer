package com.x504.optimizer.update;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HexFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Comprueba releases oficiales de GitHub y prepara una actualización verificada para el próximo inicio. */
public final class GitHubUpdater {
    private static final String API = "https://api.github.com/repos/Fan-Made-Organization/Linux-Optimizer/releases/latest";
    private static final Path UPDATE_DIR = Path.of(System.getProperty("user.home"), ".local", "share", "linux-optimizer");
    private static final Path PENDING = UPDATE_DIR.resolve("pending-update.jar");
    private static final HttpClient CLIENT = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(12))
            .followRedirects(HttpClient.Redirect.NORMAL).build();

    public record Result(boolean updateAvailable, String message) {}

    public Result checkAndStage() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(API)).timeout(Duration.ofSeconds(20))
                .header("Accept", "application/vnd.github+json").header("User-Agent", "Linux-Optimizer").GET().build();
        HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 404) return new Result(false, "No hay una versión publicada todavía.");
        if (response.statusCode() != 200) throw new IOException("GitHub respondió HTTP " + response.statusCode());
        String tag = capture(response.body(), "\\"tag_name\\"\\s*:\\s*\\"([^\\"]+)\\"");
        String jarUrl = asset(response.body(), "linux-optimizer.jar");
        String sumUrl = asset(response.body(), "linux-optimizer.jar.sha256");
        if (tag == null || jarUrl == null || sumUrl == null)
            return new Result(false, "La última release no contiene el JAR y su SHA-256.");
        String current = currentVersion();
        if (tag.equalsIgnoreCase("v" + current) || tag.equalsIgnoreCase(current))
            return new Result(false, "Linux Optimizer ya está actualizado (" + tag + ").");
        byte[] jar = download(jarUrl);
        String expected = new String(download(sumUrl), java.nio.charset.StandardCharsets.UTF_8).trim().split("\\s+")[0];
        String actual = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(jar));
        if (!actual.equalsIgnoreCase(expected)) throw new IOException("La verificación SHA-256 no coincide; actualización descartada.");
        Files.createDirectories(UPDATE_DIR);
        Path temp = UPDATE_DIR.resolve("pending-update.tmp");
        Files.write(temp, jar, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        try { Files.move(temp, PENDING, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING); }
        catch (AtomicMoveNotSupportedException ex) { Files.move(temp, PENDING, StandardCopyOption.REPLACE_EXISTING); }
        return new Result(true, "Actualización " + tag + " descargada y verificada. Se instalará al próximo inicio.");
    }

    public static boolean installPending(Path targetJar) throws IOException {
        if (!Files.isRegularFile(PENDING)) return false;
        Files.createDirectories(targetJar.toAbsolutePath().getParent());
        Path temp = targetJar.resolveSibling(targetJar.getFileName() + ".new");
        Files.copy(PENDING, temp, StandardCopyOption.REPLACE_EXISTING);
        try { Files.move(temp, targetJar, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING); }
        catch (AtomicMoveNotSupportedException ex) { Files.move(temp, targetJar, StandardCopyOption.REPLACE_EXISTING); }
        Files.deleteIfExists(PENDING);
        return true;
    }

    private static String currentVersion() {
        String version = GitHubUpdater.class.getPackage().getImplementationVersion();
        return version == null ? "0.1.0-SNAPSHOT" : version;
    }
    private static byte[] download(String url) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(60))
                .header("User-Agent", "Linux-Optimizer").GET().build();
        HttpResponse<byte[]> res = CLIENT.send(req, HttpResponse.BodyHandlers.ofByteArray());
        if (res.statusCode() != 200) throw new IOException("No se pudo descargar un archivo de la release (HTTP " + res.statusCode() + ").");
        return res.body();
    }
    private static String capture(String text, String regex) {
        Matcher m = Pattern.compile(regex).matcher(text);
        return m.find() ? m.group(1) : null;
    }
    private static String asset(String json, String name) {
        Pattern p = Pattern.compile("\\"name\\"\\s*:\\s*\\""+Pattern.quote(name)+"\\"[\\s\\S]*?\\"browser_download_url\\"\\s*:\\s*\\"([^\\"]+)\\"");
        Matcher m = p.matcher(json);
        return m.find() ? m.group(1).replace("\\/", "/") : null;
    }
}
