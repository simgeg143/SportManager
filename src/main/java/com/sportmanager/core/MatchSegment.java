package com.sportmanager.core;
import java.io.Serializable;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents one segment of a match (e.g. a half in football).
 * Stores the events that occurred and the partial score for that segment.
 */
public class MatchSegment implements Serializable {

    private final int    segmentNumber;
    private final String label;
    private int    homeGoals;
    private int    awayGoals;
    private final List<String> events = new ArrayList<>();

    public MatchSegment(int segmentNumber, String label) {
        this.segmentNumber = segmentNumber;
        this.label         = label;
    }

    public int    getSegmentNumber() { return segmentNumber; }
    public String getLabel()         { return label; }
    public int    getHomeGoals()     { return homeGoals; }
    public int    getAwayGoals()     { return awayGoals; }
    public List<String> getEvents()  { return events; }

    public void addEvent(String event)  { events.add(event); }
    public void addHomeGoal()           { homeGoals++; }
    public void addAwayGoal()           { awayGoals++; }

    @Override
    public String toString() { return label; }
}
