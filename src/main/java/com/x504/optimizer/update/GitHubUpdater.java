package com.x504.optimizer.update;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Descarga releases oficiales, verifica SHA-256 y prepara la instalación para el siguiente inicio. */
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
        if (response.statusCode() == 404) return new Result(false, "Todavía no hay una release publicada.");
        if (response.statusCode() != 200) throw new IOException("GitHub respondió HTTP " + response.statusCode());

        String json = response.body();
        String tag = capture(json, "\"tag_name\"\\s*:\\s*\"([^\"]+)\"");
        String jarUrl = asset(json, "linux-optimizer.jar");
        String sumUrl = asset(json, "linux-optimizer.jar.sha256");
        if (tag == null || jarUrl == null || sumUrl == null)
            return new Result(false, "La release no contiene el JAR y su checksum.");

        String current = currentVersion().replace("-SNAPSHOT", "");
        if (tag.equalsIgnoreCase("v" + current) || tag.equalsIgnoreCase(current))
            return new Result(false, "Linux Optimizer ya está actualizado (" + tag + ").");

        byte[] jar = download(jarUrl);
        String expected = new String(download(sumUrl), java.nio.charset.StandardCharsets.UTF_8).trim().split("\\s+")[0];
        String actual = sha256(jar);
        if (!actual.equalsIgnoreCase(expected)) throw new IOException("SHA-256 no coincide; actualización descartada.");

        Files.createDirectories(UPDATE_DIR);
        Path temp = UPDATE_DIR.resolve("pending-update.tmp");
        Files.write(temp, jar, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        try { Files.move(temp, PENDING, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING); }
        catch (AtomicMoveNotSupportedException ex) { Files.move(temp, PENDING, StandardCopyOption.REPLACE_EXISTING); }
        return new Result(true, "Versión " + tag + " descargada y verificada. Se instalará al próximo inicio.");
    }

    private static String currentVersion() {
        String version = GitHubUpdater.class.getPackage().getImplementationVersion();
        return version == null ? "0.1.0-SNAPSHOT" : version;
    }

    private static String sha256(byte[] data) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(data)); }
        catch (NoSuchAlgorithmException ex) { throw new IllegalStateException("SHA-256 no disponible", ex); }
    }

    private static byte[] download(String url) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(60))
                .header("User-Agent", "Linux-Optimizer").GET().build();
        HttpResponse<byte[]> res = CLIENT.send(req, HttpResponse.BodyHandlers.ofByteArray());
        if (res.statusCode() != 200) throw new IOException("Descarga fallida (HTTP " + res.statusCode() + ").");
        return res.body();
    }

    private static String capture(String json, String regex) {
        Matcher matcher = Pattern.compile(regex).matcher(json);
        return matcher.find() ? matcher.group(1) : null;
    }

    private static String asset(String json, String name) {
        String regex = "\"name\"\\s*:\\s*\"" + Pattern.quote(name)
                + "\"[\\s\\S]*?\"browser_download_url\"\\s*:\\s*\"([^\"]+)\"";
        String url = capture(json, regex);
        return url == null ? null : url.replace("\\/", "/");
    }
}
