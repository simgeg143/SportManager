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
 * Handles saving and loading game state to disk.
 * Save files stored under ~/.sportmanager/saves/
 */
public final class SaveGameService {

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

    public record SaveSlotSummary(
            String id,
            String displayName,
            long savedAtEpochMs,
            String detailsLine
    ) {}

    public static String saveNew(String displayName, GameSession.SessionSnapshot snapshot) throws IOException {
        Files.createDirectories(SAVES_DIR);
        String name = displayName == null || displayName.isBlank() ? "Save" : displayName.trim();
        long now = System.currentTimeMillis();
        String id = now + "_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12) + ".sav";
        Path file = SAVES_DIR.resolve(id);
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
                            } catch (Exception ignored) {}
                        });
            } catch (IOException ignored) {}
        }
        list.sort(Comparator.comparingLong(SaveSlotSummary::savedAtEpochMs).reversed());
        return list;
    }

    public static GameSession.SessionSnapshot loadById(String saveId) throws IOException, ClassNotFoundException {
        Path file = SAVES_DIR.resolve(saveId);
        SaveGameBundle bundle = readBundle(file);
        return bundle.snapshot();
    }

    public static void deleteById(String saveId) throws IOException {
        Path file = SAVES_DIR.resolve(saveId);
        Files.deleteIfExists(file);
    }

    private static SaveGameBundle readBundle(Path file) throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(file))) {
            Object o = in.readObject();
            if (o instanceof SaveGameBundle bundle) {
                return bundle;
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
        String sport = s != null ? s.getName() : "—";
        String team = t != null ? t.getName() : "—";
        int week = snap.currentWeek();
        return team + " · " + sport + " · Week " + week;
    }
}