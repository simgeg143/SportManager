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
 *
 * <p>Release zip: if {@code lib/} is missing JavaFX JARs, downloads them from Maven Central
 * (needs internet once), then restarts the JVM with {@code --module-path lib}.
 */
public final class JfxLauncher {

    private JfxLauncher() {}

    public static void main(String[] args) throws Exception {
        Path cwd = Paths.get(System.getProperty("user.dir", ".")).toAbsolutePath().normalize();

        if (!isDevelopmentWorkspace()) {
            Path lib = cwd.resolve("lib");
            if (!ReleaseRuntimeDeps.isChildProcess() && ReleaseRuntimeDeps.anyMissing(lib)) {
                System.out.println("JavaFX libraries missing under lib\\.");
                System.out.println("Downloading OpenJFX from Maven Central (internet required for this step).");
                System.out.println();
                try {
                    ReleaseRuntimeDeps.downloadJavaFx(lib);
                } catch (Exception ex) {
                    System.err.println("Download failed: " + ex.getMessage());
                    System.err.println("Use the full zip from \"mvn package\", or check firewall/proxy.");
                    ex.printStackTrace(System.err);
                    System.exit(1);
                }
                ReleaseRuntimeDeps.relaunchWithModules(cwd, args);
                return;
            }
        }

        launchApplication(args);
    }

    private static boolean isDevelopmentWorkspace() {
        return Files.isRegularFile(Paths.get("target/classes/com/sportmanager/App.class"));
    }

    private static void launchApplication(String[] args) throws Exception {
        try {
            Class.forName("com.sportmanager.App");
        } catch (ClassNotFoundException e) {
            System.err.println("Could not load application classes.");
            System.err.println("Development: run \"mvn compile\" from the project root, then launch again.");
            System.err.println("Release: keep sport-manager.jar next to the lib folder from the zip.");
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
        return Paths.get(System.getProperty("user.home"), ".sportmanager", "javafx-cache");
    }
}
