package com.sportmanager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class League {

    protected List<Team> teams;
    protected List<StandingEntry> standingEntries;
    protected int currentWeek;

    public League() {
        this.teams = new ArrayList<>();
        this.standingEntries = new ArrayList<>();
        this.currentWeek = 1;
    }

    public List<Team> getTeams() {
        return teams;
    }

    public void setTeams(List<Team> teams) {
        this.teams = teams;
    }

    public List<StandingEntry> getStandingEntries() {
        return standingEntries;
    }

    public void setStandingEntries(List<StandingEntry> standingEntries) {
        this.standingEntries = standingEntries;
    }

    public int getCurrentWeek() {
        return currentWeek;
    }

    public void setCurrentWeek(int currentWeek) {
        this.currentWeek = currentWeek;
    }

    public void addTeam(Team team){
        teams.add(team);
        standingEntries.add(new StandingEntry(team));
    }

    public List<StandingEntry> getTable(){
        Collections.sort(standingEntries);
        return standingEntries;
    }

    public void updateStandings(Team team, int points){
        for(StandingEntry entry : standingEntries){
            if(entry.getTeam().equals(team)){
                entry.addPoints(points);
            }
        }
    }

    public void advanceWeek(){
        currentWeek = currentWeek +1;
    }
}
