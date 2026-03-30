package com.sportmanager;

public class StandingEntry implements Comparable<StandingEntry> {

    private Team team;
    private int points;

    public StandingEntry(Team team) {
        this.team = team;
        this.points = 0;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public void addPoints(int p){
        this.points += p;
    }

    @Override
    public int compareTo(StandingEntry other){
        return Integer.compare(other.points, this.points);
    }
}
