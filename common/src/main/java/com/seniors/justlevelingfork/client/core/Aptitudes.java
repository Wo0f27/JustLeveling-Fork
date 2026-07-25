package com.seniors.justlevelingfork.client.core;

import com.seniors.justlevelingfork.registry.aptitude.Aptitude;

public final class Aptitudes {
    private final String key;
    private final String resource;
    private final boolean droppable;
    private final Aptitude aptitude;
    private final int aptitudeLevel;

    public Aptitudes(String key, String resource, boolean droppable, Aptitude aptitude, int aptitudeLevel) {
        this.key = key;
        this.resource = resource;
        this.droppable = droppable;
        this.aptitude = aptitude;
        this.aptitudeLevel = aptitudeLevel;
    }

    public String getKey() {
        return key;
    }

    public String getResource() {
        return resource;
    }

    public boolean isDroppable() {
        return droppable;
    }

    public Aptitude getAptitude() {
        return aptitude;
    }

    public int getAptitudeLvl() {
        return aptitudeLevel;
    }
}
