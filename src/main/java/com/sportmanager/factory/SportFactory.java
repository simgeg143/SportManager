package com.sportmanager.factory;

import com.sportmanager.core.Sport;
import com.sportmanager.football.FootballSport;

public final class SportFactory {

    private SportFactory() {}

    public static Sport create(String sportName) {

        if (sportName == null) {
            throw new IllegalArgumentException("Sport name cannot be null");
        }

        return switch (sportName.trim().toLowerCase()) {
            case "football" -> new FootballSport();
            default -> throw new IllegalArgumentException("Unknown sport: " + sportName);
        };
    }
}
