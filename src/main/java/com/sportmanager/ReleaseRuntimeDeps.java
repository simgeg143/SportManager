package com.sportmanager;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Release installs ship {@code sport-manager.jar} + optional {@code lib/}. If JavaFX JARs are
 * missing (partial zip, deleted folder), downloads OpenJFX from Maven Central then relaunches with
 * {@code --module-path}.
 */
final class ReleaseRuntimeDeps {

    /** Keep in sync with {@code pom.xml} {@code javafx.version}. */
    private static final String JAVAFX_VERSION = "21.0.2";

    private static final String CENTRAL = "https://repo1.maven.org/maven2/org/openjfx";

    private static final String[] JAVAFX_ARTIFACTS = {
            "javafx-base", "javafx-graphics", "javafx-controls", "javafx-fxml"
    };

    private ReleaseRuntimeDeps() {}

    static boolean isChildProcess() {
        return "1".equals(System.getenv("SPORT_MANAGER_CHILD"));
    }

    static boolean anyMissing(Path libDir) throws IOException {
        String plat = javafxClassifier();
        Files.createDirectories(libDir);
        for (Path p : requiredLibFiles(libDir, plat)) {
            if (!Files.isRegularFile(p) || Files.size(p) == 0L) {
                return true;
            }
        }
        return false;
    }

    static void downloadJavaFx(Path libDir) throws IOException, InterruptedException {
        Files.createDirectories(libDir);
        String plat = javafxClassifier();
        HttpClient http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(25))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();

        for (String artifact : JAVAFX_ARTIFACTS) {
            downloadOne(http, libDir, artifact, null);
            downloadOne(http, libDir, artifact, plat);
        }
    }

    static void relaunchWithModules(Path installRoot, String[] args) throws IOException, InterruptedException {
        Path lib = installRoot.resolve("lib").toAbsolutePath().normalize();
        Path jar = installRoot.resolve("sport-manager.jar").toAbsolutePath().normalize();
        if (!Files.isRegularFile(jar)) {
            throw new IOException("sport-manager.jar not found here: " + jar);
        }

        List<String> cmd = new ArrayList<>();
        cmd.add(resolveJavaExecutable());
        cmd.add("--module-path");
        cmd.add(lib.toString());
        cmd.add("--add-modules");
        cmd.add("javafx.controls,javafx.fxml");
        cmd.add("--add-opens");
        cmd.add("javafx.graphics/javafx.scene=ALL-UNNAMED");
        cmd.add("--add-opens");
        cmd.add("javafx.graphics/com.sun.javafx.application=ALL-UNNAMED");
        cmd.add("--add-opens");
        cmd.add("javafx.base/com.sun.javafx.runtime=ALL-UNNAMED");
        cmd.add("-cp");
        cmd.add(jar.toString());
        cmd.add("com.sportmanager.JfxLauncher");
        for (String a : args) {
            cmd.add(a);
        }

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.directory(installRoot.toFile());
        pb.environment().put("SPORT_MANAGER_CHILD", "1");
        pb.inheritIO();
        int code = pb.start().waitFor();
        System.exit(code);
    }

    private static List<Path> requiredLibFiles(Path libDir, String platformClassifier) {
        List<Path> out = new ArrayList<>(8);
        for (String artifact : JAVAFX_ARTIFACTS) {
            out.add(libDir.resolve(baseJarName(artifact)));
            out.add(libDir.resolve(platformJarName(artifact, platformClassifier)));
        }
        return out;
    }

    private static String baseJarName(String artifact) {
        return artifact + "-" + JAVAFX_VERSION + ".jar";
    }

    private static String platformJarName(String artifact, String platformClassifier) {
        return artifact + "-" + JAVAFX_VERSION + "-" + platformClassifier + ".jar";
    }

    private static void downloadOne(HttpClient http, Path libDir, String artifact, String classifierSuffix)
            throws IOException, InterruptedException {
        String fileName = classifierSuffix == null
                ? baseJarName(artifact)
                : platformJarName(artifact, classifierSuffix);
        Path dest = libDir.resolve(fileName);
        if (Files.isRegularFile(dest) && Files.size(dest) > 0L) {
            return;
        }
        URI uri = jarUri(artifact, classifierSuffix);
        System.out.println("  fetching " + fileName + " ...");
        fetch(http, uri, dest);
    }

    private static URI jarUri(String artifact, String classifierSuffix) {
        String fn = classifierSuffix == null
                ? baseJarName(artifact)
                : platformJarName(artifact, classifierSuffix);
        String url = CENTRAL + "/" + artifact + "/" + JAVAFX_VERSION + "/" + fn;
        return URI.create(url);
    }

    private static void fetch(HttpClient http, URI uri, Path dest) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofMinutes(3))
                .GET()
                .build();
        HttpResponse<InputStream> resp = http.send(req, HttpResponse.BodyHandlers.ofInputStream());
        if (resp.statusCode() != 200) {
            throw new IOException("HTTP " + resp.statusCode() + " for " + uri);
        }
        Files.createDirectories(dest.getParent());
        Path tmp = dest.resolveSibling(dest.getFileName().toString() + ".tmp");
        try (InputStream in = resp.body()) {
            Files.copy(in, tmp, StandardCopyOption.REPLACE_EXISTING);
        }
        Files.move(tmp, dest, StandardCopyOption.REPLACE_EXISTING);
    }

    private static String javafxClassifier() {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        String arch = System.getProperty("os.arch", "").toLowerCase(Locale.ROOT);
        if (os.contains("mac")) {
            if (arch.contains("aarch64") || arch.contains("arm64")) {
                return "mac-aarch64";
            }
            return "mac";
        }
        if (os.contains("win")) {
            return "win";
        }
        if (os.contains("linux")) {
            if (arch.contains("aarch64") || arch.contains("arm64")) {
                return "linux-aarch64";
            }
            return "linux";
        }
        return "linux";
    }

    private static String resolveJavaExecutable() {
        Path home = Paths.get(System.getProperty("java.home"));
        boolean win = System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");
        Path exe = home.resolve("bin").resolve(win ? "java.exe" : "java");
        return exe.toAbsolutePath().normalize().toString();
    }
}
