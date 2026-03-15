package com.sportmanager.factory;

import com.sportmanager.core.Sport;
import com.sportmanager.football.FootballSport;

/**
 * Factory that instantiates a Sport implementation by name.
 * The UI calls {@code SportFactory.create("Football")} without ever importing
 * a concrete sport class directly.
 */
public final class SportFactory {

    private SportFactory() {}

    /**
     * @param sportName case-insensitive sport name (e.g. "Football")
     * @return a new Sport instance
     * @throws IllegalArgumentException if the sport name is not recognised
     */
    public static Sport create(String sportName) {
        return switch (sportName.trim().toLowerCase()) {
            case "football" -> new FootballSport();
            default -> throw new IllegalArgumentException("Unknown sport: " + sportName);
        };
    }

    /** All sport names currently supported by the factory. */
    public static java.util.List<String> availableSports() {
        return java.util.List.of("Football");
    }
}
