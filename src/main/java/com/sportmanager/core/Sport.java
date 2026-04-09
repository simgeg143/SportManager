package com.sportmanager.core;

public interface Sport {

    String getName();

    League createLeague(String leagueName, int teamCount);

    Team createTeam(String name);

    Match createMatch(Team home, Team away, int weekNo);

    int getRequiredLineupSize();
}
