package com.game;

import java.awt.Color;

public class RadialLand {
    private int owner;
    private final ZoneType zoneType;
    private final int ring;
    private final int position;
    private boolean selected;

    public RadialLand(ZoneType zoneType, int ring, int position) {
        this.zoneType = zoneType;
        this.ring = ring;
        this.position = position;
        this.owner = 0;
        this.selected = false;
    }

    public int getOwner() {
        return owner;
    }

    public void setOwner(int owner) {
        this.owner = owner;
    }

    public ZoneType getZoneType() {
        return zoneType;
    }

    public int getRing() {
        return ring;
    }

    public int getPosition() {
        return position;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public Color getDisplayColor() {
        if (selected) return Color.YELLOW;
        if (owner == 1) return new Color(0, 100, 255);
        if (owner == 2) return new Color(255, 100, 100);
        return zoneType.getColor();
    }

    public String getDisplayText() {
        if (owner == 0) return zoneType.getName();
        return "玩家" + owner;
    }
}