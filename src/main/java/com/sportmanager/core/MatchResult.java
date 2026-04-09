package com.sportmanager.core;

public class MatchResult {

    private Team homeTeam;
    private Team awayTeam;
    private int homeScore;
    private int awayScore;

    public MatchResult(Team homeTeam, Team awayTeam, int homeScore, int awayScore) {
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.homeScore = homeScore;
        this.awayScore = awayScore;
    }

    public Team getHomeTeam() { return homeTeam; }
    public Team getAwayTeam() { return awayTeam; }

    public int getHomeScore() { return homeScore; }
    public int getAwayScore() { return awayScore; }

    public String getScore() {
        return homeScore + " - " + awayScore;
    }
}