package com.sportmanager.core;

public class MatchSegment {

    private int segmentNumber;
    private String label;

    public MatchSegment(int segmentNumber, String label) {
        this.segmentNumber = segmentNumber;
        this.label = label;
    }

    public int getSegmentNumber() {
        return segmentNumber;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}