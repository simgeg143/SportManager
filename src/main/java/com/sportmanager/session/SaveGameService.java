package com.sportmanager.session;

import com.sportmanager.core.League;
import com.sportmanager.core.Sport;
import com.sportmanager.core.Team;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

/**
<<<<<<< Updated upstream
 * Multiple save files under ~/.sportmanager/saves/. Each file stores a
 * SaveGameBundle (display name, timestamp, session snapshot).
 * The legacy single file ~/.sportmanager/savegame.dat is still loadable.
=======
<<<<<<< HEAD
 * Multiple save files under {@code ~/.sportmanager/saves/}. Each file stores a
 * {@link SaveGameBundle} (display name, timestamp, session snapshot).
 * The legacy single file {@code ~/.sportmanager/savegame.dat} is still loadable as id {@link #LEGACY_SAVE_ID}.
=======
 * Multiple save files under ~/.sportmanager/saves/. Each file stores a
 * SaveGameBundle (display name, timestamp, session snapshot).
 * The legacy single file ~/.sportmanager/savegame.dat is still loadable.
>>>>>>> 500fd5138fc06faaeedc747d2b64dae2724e5e08
>>>>>>> Stashed changes
 */
public final class SaveGameService {

    public static final String LEGACY_SAVE_ID = "__legacy__";

    private static final Path LEGACY_SINGLE_FILE = Path.of(
            System.getProperty("user.home"),
            ".sportmanager",
            "savegame.dat"
    );

    private static final Path SAVES_DIR = Path.of(
            System.getProperty("user.home"),
            ".sportmanager",
            "saves"
    );

    private SaveGameService() {}

    public record SaveGameBundle(
            String displayName,
            long savedAtEpochMs,
            GameSession.SessionSnapshot snapshot
    ) implements Serializable {}

<<<<<<< Updated upstream
=======
<<<<<<< HEAD
    /** Summary row for load UI; {@code id} is safe to pass back to {@link #loadById(String)}. */
=======
>>>>>>> 500fd5138fc06faaeedc747d2b64dae2724e5e08
>>>>>>> Stashed changes
    public record SaveSlotSummary(
            String id,
            String displayName,
            long savedAtEpochMs,
            String detailsLine
    ) {}

<<<<<<< Updated upstream
=======
<<<<<<< HEAD
    /**
     * Writes a new save file; returns the generated id (filename without path).
     */
=======
>>>>>>> 500fd5138fc06faaeedc747d2b64dae2724e5e08
>>>>>>> Stashed changes
    public static String saveNew(String displayName, GameSession.SessionSnapshot snapshot) throws IOException {
        Files.createDirectories(SAVES_DIR);
        String name = displayName == null || displayName.isBlank() ? "Save" : displayName.trim();
        long now = System.currentTimeMillis();
        String id = now + "_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12) + ".sav";
        Path file = SAVES_DIR.resolve(id);
        if (!file.normalize().startsWith(SAVES_DIR.normalize())) {
            throw new IOException("Invalid save path");
        }
        SaveGameBundle bundle = new SaveGameBundle(name, now, snapshot);
        try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(file))) {
            out.writeObject(bundle);
        }
        return id;
    }

    public static List<SaveSlotSummary> listSaves() {
        List<SaveSlotSummary> list = new ArrayList<>();
        if (Files.isDirectory(SAVES_DIR)) {
            try (Stream<Path> stream = Files.list(SAVES_DIR)) {
                stream.filter(p -> p.getFileName().toString().endsWith(".sav"))
                        .forEach(p -> {
                            try {
                                SaveGameBundle b = readBundle(p);
                                list.add(toSummary(p.getFileName().toString(), b));
<<<<<<< Updated upstream
                            } catch (Exception ignored) {}
                        });
            } catch (IOException ignored) {}
=======
<<<<<<< HEAD
                            } catch (Exception ignored) {
                                /* skip corrupt */
                            }
                        });
            } catch (IOException ignored) { /* empty */ }
=======
                            } catch (Exception ignored) {}
                        });
            } catch (IOException ignored) {}
>>>>>>> 500fd5138fc06faaeedc747d2b64dae2724e5e08
>>>>>>> Stashed changes
        }
        if (Files.isRegularFile(LEGACY_SINGLE_FILE)) {
            try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(LEGACY_SINGLE_FILE))) {
                Object o = in.readObject();
                GameSession.SessionSnapshot snap;
                if (o instanceof SaveGameBundle bundle) {
                    snap = bundle.snapshot();
                } else if (o instanceof GameSession.SessionSnapshot legacy) {
                    snap = legacy;
                } else {
                    throw new ClassNotFoundException();
                }
                long ts = Files.getLastModifiedTime(LEGACY_SINGLE_FILE).toMillis();
                list.add(new SaveSlotSummary(
                        LEGACY_SAVE_ID,
                        "Older single-file save",
                        ts,
                        buildDetails(snap)));
<<<<<<< Updated upstream
            } catch (Exception ignored) {}
=======
<<<<<<< HEAD
            } catch (Exception ignored) { /* skip */ }
=======
            } catch (Exception ignored) {}
>>>>>>> 500fd5138fc06faaeedc747d2b64dae2724e5e08
>>>>>>> Stashed changes
        }
        list.sort(Comparator.comparingLong(SaveSlotSummary::savedAtEpochMs).reversed());
        return list;
    }

    public static GameSession.SessionSnapshot loadById(String saveId) throws IOException, ClassNotFoundException {
        if (saveId == null || saveId.isBlank()) {
            throw new IllegalArgumentException("No save selected.");
        }
        if (LEGACY_SAVE_ID.equals(saveId)) {
            return loadLegacyRaw();
        }
        if (!isSafeSaveFileName(saveId)) {
            throw new IllegalArgumentException("Invalid save id.");
        }
        Path file = SAVES_DIR.resolve(saveId).normalize();
        if (!file.startsWith(SAVES_DIR.normalize()) || !Files.isRegularFile(file)) {
            throw new IOException("Save file not found.");
        }
        SaveGameBundle bundle = readBundle(file);
        return bundle.snapshot();
    }

    public static void deleteById(String saveId) throws IOException {
        if (saveId == null || saveId.isBlank()) return;
        if (LEGACY_SAVE_ID.equals(saveId)) {
            Files.deleteIfExists(LEGACY_SINGLE_FILE);
            return;
        }
        if (!isSafeSaveFileName(saveId)) {
            throw new IllegalArgumentException("Invalid save id.");
        }
        Path file = SAVES_DIR.resolve(saveId).normalize();
        if (!file.startsWith(SAVES_DIR.normalize())) {
            throw new IOException("Invalid path");
        }
        Files.deleteIfExists(file);
    }

    private static GameSession.SessionSnapshot loadLegacyRaw() throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(LEGACY_SINGLE_FILE))) {
            Object o = in.readObject();
            if (o instanceof SaveGameBundle bundle) {
                return bundle.snapshot();
            }
            if (o instanceof GameSession.SessionSnapshot snap) {
                return snap;
            }
            throw new ClassNotFoundException("Unknown save format");
        }
    }

    private static SaveGameBundle readBundle(Path file) throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(file))) {
            Object o = in.readObject();
            if (o instanceof SaveGameBundle bundle) {
                return bundle;
            }
            if (o instanceof GameSession.SessionSnapshot snap) {
                return new SaveGameBundle("Save", Files.getLastModifiedTime(file).toMillis(), snap);
            }
            throw new ClassNotFoundException("Unknown save format");
        }
    }

    private static SaveSlotSummary toSummary(String id, SaveGameBundle b) {
        return new SaveSlotSummary(id, b.displayName(), b.savedAtEpochMs(), buildDetails(b.snapshot()));
    }

    private static String buildDetails(GameSession.SessionSnapshot snap) {
        if (snap == null) return "";
        Sport s = snap.selectedSport();
        Team t = snap.managedTeam();
        League l = snap.league();
        String sport = s != null ? s.getName() : "—";
        String team = t != null ? t.getName() : "—";
        int week = snap.currentWeek();
        int year = snap.currentSeasonYear();
        String leagueName = l != null ? l.getName() : "—";
        return team + " · " + sport + " · Week " + week + " · " + year + " · " + leagueName;
    }

    private static boolean isSafeSaveFileName(String name) {
        if (name.length() > 120 || name.isEmpty()) return false;
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (!(c >= 'a' && c <= 'z')
                    && !(c >= 'A' && c <= 'Z')
                    && !(c >= '0' && c <= '9')
                    && c != '.' && c != '_' && c != '-') {
                return false;
            }
        }
        return name.endsWith(".sav");
    }
<<<<<<< Updated upstream
}
=======
<<<<<<< HEAD
}
=======
}
>>>>>>> 500fd5138fc06faaeedc747d2b64dae2724e5e08
>>>>>>> Stashed changes
