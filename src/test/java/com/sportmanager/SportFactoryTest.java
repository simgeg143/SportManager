package com.sportmanager;
import com.sportmanager.core.Sport;
import com.sportmanager.factory.SportFactory;
import com.sportmanager.football.FootballSport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class SportFactoryTest {

    @Test
    @DisplayName("Should create FootballSport when sport code is football***")
    void shouldCreateFootballSportWhenCodeIsFootball() {
        Sport sport = SportFactory.create("football");

        assertNotNull(sport);
        assertInstanceOf(FootballSport.class, sport);
        assertEquals("Football", sport.getName());
    }

    @Test
    @DisplayName("Should create FootballSport when sport code is case-insensitive")
    void shouldCreateFootballSportCaseInsensitive() {
        Sport sport = SportFactory.create("FOOTBALL");

        assertNotNull(sport);
        assertInstanceOf(FootballSport.class, sport);
    }

    @Test
    @DisplayName("Should throw exception for unsupported sport code")
    void shouldThrowExceptionForUnsupportedSportCode() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> SportFactory.create("curling")
        );

        assertEquals("Unsupported sport: curling", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when sport code is null")
    void shouldThrowExceptionWhenSportCodeIsNull() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> SportFactory.create(null)
        );

        assertEquals("Sport code cannot be null or blank", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when sport code is blank")
    void shouldThrowExceptionWhenSportCodeIsBlank() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> SportFactory.create("   ")
        );

        assertEquals("Sport code cannot be null or blank", exception.getMessage());
    }
}