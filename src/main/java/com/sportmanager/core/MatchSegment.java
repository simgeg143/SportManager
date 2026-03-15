package com.sportmanager.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single segment of a match (a half, quarter, or set).
 * Holds the running partial scores and all events that occurred in this segment.
 * As defined in the architecture document: segmentNumber, label, partialScores, events.
 */
public class MatchSegment {

    private final int    segmentNumber;     // 0-based index
    private final String label;             // e.g. "First Half", "Quarter 3"
    private int          homePartialScore;
    private int          awayPartialScore;
    private final List<String> events;

    public MatchSegment(int segmentNumber, String label) {
        this.segmentNumber    = segmentNumber;
        this.label            = label;
        this.homePartialScore = 0;
        this.awayPartialScore = 0;
        this.events           = new ArrayList<>();
    }

    // ── Mutators called during simulation ────────────────────────────────────

    public void addHomeGoal()              { homePartialScore++; }
    public void addAwayGoal()              { awayPartialScore++; }
    public void addEvent(String event)     { events.add(event); }

    // ── Accessors ─────────────────────────────────────────────────────────────

    public int          getSegmentNumber()    { return segmentNumber; }
    public String       getLabel()            { return label; }
    public int          getHomePartialScore() { return homePartialScore; }
    public int          getAwayPartialScore() { return awayPartialScore; }
    public List<String> getEvents()           { return events; }

    @Override public String toString() {
        return label + "  [" + homePartialScore + " – " + awayPartialScore + "]";
    }
}
