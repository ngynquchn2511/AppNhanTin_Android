package com.example.nesvie_copyzalo;

public class ActivityRecord {
    private String label;
    private float duration;

    public ActivityRecord(String label, float duration) {
        this.label = label;
        this.duration = duration;
    }

    public String getLabel() {
        return label;
    }

    public float getDuration() {
        return duration;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setDuration(float duration) {
        this.duration = duration;
    }
}
