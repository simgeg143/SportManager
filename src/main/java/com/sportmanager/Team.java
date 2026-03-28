package com.sportmanager;

import com.sportmanager.core.Coach;
import com.sportmanager.core.Lineup;
import com.sportmanager.core.Tactic;

import java.util.ArrayList;
import java.util.List;

public abstract class Team {

    protected String name;
    protected String logoPath;
    protected List<Player> players;
    protected List<Coach> coaches;
    protected Lineup currentLineup;
    protected Tactic currentTactic;

    public Team(String name, String logoPath, List<Player> players, Lineup currentLineup, List<Coach> coaches, Tactic currentTactic) {
        this.name = name;
        this.logoPath = logoPath;
        this.players = new ArrayList<>();
        this.currentLineup = currentLineup;
        this.coaches = new ArrayList<>();
        this.currentTactic = currentTactic;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLogoPath() {
        return logoPath;
    }

    public void setLogoPath(String logoPath) {
        this.logoPath = logoPath;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public List<Coach> getCoaches() {
        return coaches;
    }

    public void setCoaches(List<Coach> coaches) {
        this.coaches = coaches;
    }

    public Lineup getCurrentLineup() {
        return currentLineup;
    }

    public void setCurrentLineup(Lineup lineup) {
        this.currentLineup = lineup;
    }

    public Tactic getCurrentTactic() {
        return currentTactic;
    }

    public void setCurrentTactic(Tactic tactic) {
        this.currentTactic = tactic;
    }

    public void addPlayer(Player player){
        players.add(player);
    }

    public void removePlayer(Player player){
        players.remove(player);
    }

    public List<Player> getAvailablePlayers(){
        List<Player> available = new ArrayList<>();

        for (Player p : players){
            if (p.isAvailable()){
                available.add(p);
            }
        }
        return available;
    }

    public void addCoach (Coach coach){
        coaches.add(coach);
    }
}
