package com.sportmanager.factory;

import com.sportmanager.core.Sport;
import com.sportmanager.basketball.BasketballSport;
import com.sportmanager.football.FootballSport;

public final class SportFactory {

    private SportFactory() {}

    public static Sport create(String sportCode) {
        if (sportCode == null || sportCode.isBlank()) {
            throw new IllegalArgumentException("Sport code cannot be null or blank");
        }

        return switch (sportCode.trim().toLowerCase()) {
            case "football" -> new FootballSport();
<<<<<<< Updated upstream
            case "basketbal" -> new BasketballSport();
=======
<<<<<<< HEAD
            case "basketball" -> new BasketballSport();
=======
            case "basketbal" -> new BasketballSport();
>>>>>>> 500fd5138fc06faaeedc747d2b64dae2724e5e08
>>>>>>> Stashed changes
            default -> throw new IllegalArgumentException("Unsupported sport: " + sportCode.trim().toLowerCase());
        };
    }
}
