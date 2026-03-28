package com.sportmanager;

import java.util.List;

public class Lineup {

    private List<Player> players;

    public Lineup(List<Player> players) {
        this.players = players;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public boolean isValid(){
        return players != null && players.size()>0;
    }
}
