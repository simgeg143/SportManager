package com.sportmanager.core;

public abstract class Match {

    protected Team homeTeam;
    protected Team awayTeam;
    protected int homeScore;
    protected int awayScore;
    protected int weekNo;
    protected boolean finished;

    protected Match(Team home, Team away, int weekNo) {
        this.homeTeam = home;
        this.awayTeam = away;
        this.weekNo = weekNo;
        this.homeScore = 0;
        this.awayScore = 0;
        this.finished = false;
    }

    public Team getHomeTeam() { return homeTeam; }
    public Team getAwayTeam() { return awayTeam; }

    public int getHomeScore() { return homeScore; }
    public int getAwayScore() { return awayScore; }

    public int getWeekNo() { return weekNo; }

    public boolean isFinished() { return finished; }

    public String getScore() {
        return homeScore + " - " + awayScore;
    }

    // match nasıl oynanacak → concrete class belirler
    public abstract void playMatch();
}