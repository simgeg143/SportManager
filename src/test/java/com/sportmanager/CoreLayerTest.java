package com.sportmanager;
import com.sportmanager.core.*;
import com.sportmanager.factory.SportFactory;
import com.sportmanager.football.FootballSport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class CoreLayerTest {
    @Test
    @DisplayName("SportFactory should return FootballSport for valid sport name")
    void sportFactoryCreatesFootballSport() {
        Sport sport = SportFactory.create("football");

        assertNotNull(sport);
        assertInstanceOf(FootballSport.class, sport);
        assertEquals("Football", sport.getName());
    }
}
