package com.sportmanager.core;
import java.io.Serializable;

import java.util.List;

/** Contract that every sport module must satisfy. */
public interface Sport extends Serializable {

    String       getName();
    List<String> getPositions();
    List<String> getTactics();

    int  getSegmentCount();
    String getSegmentLabel();
    int  getRequiredLineupSize();
    int  getMaxSubstituteCount();

    League createLeague(String leagueName, int teamCount);
    Team   createTeam(String name);
    Match  createMatch(Team home, Team away, int weekNo);
}
