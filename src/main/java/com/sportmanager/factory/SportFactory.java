package com.sportmanager.factory;

import com.sportmanager.core.Sport;
import com.sportmanager.football.FootballSport;

public final class SportFactory {

    private SportFactory() {}

    public static Sport create(String sportCode) {
        if (sportCode == null || sportCode.isBlank()) {
            throw new IllegalArgumentException("Sport code cannot be null or blank");
        }

        return switch (sportCode.trim().toLowerCase()) {
            case "football" -> new FootballSport();
            default -> throw new IllegalArgumentException("Unsupported sport: " + sportCode.trim().toLowerCase());
        };
    }
}
