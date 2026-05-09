package com.sportmanager;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

/**
 * Entry point for {@code mvn exec:java}: configures OpenJFX native cache before any JavaFX
 * class loads, then delegates to {@link App}. On Windows, uses {@code %PUBLIC%} so the
 * cache path stays ASCII (avoids broken native paths when the project lives under a non-ASCII
 * profile directory). Elsewhere uses {@code target/javafx-cache} under the process cwd.
 */
public final class JfxLauncher {

    private JfxLauncher() {}

    public static void main(String[] args) throws Exception {
        Path marker = Paths.get("target/classes/com/sportmanager/App.class");
        if (!Files.isRegularFile(marker)) {
            System.err.println("Project is not compiled yet. From the project root run:");
            System.err.println("  mvn compile");
            System.err.println("or simply:");
            System.err.println("  mvn");
            System.err.println("(defaultGoal is compile then exec:java.)");
            System.exit(1);
        }
        Path cache = resolveJavaFxCacheDir();
        Files.createDirectories(cache);
        System.setProperty("javafx.cachedir", cache.toAbsolutePath().toString());
        App.main(args);
    }

    private static Path resolveJavaFxCacheDir() {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if (os.contains("win")) {
            String pub = System.getenv("PUBLIC");
            if (pub != null && !pub.isBlank()) {
                return Paths.get(pub, "SportManagerJavaFXCache");
            }
        }
        return Paths.get(System.getProperty("user.dir", "."), "target", "javafx-cache");
    }
}
