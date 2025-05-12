package com.game;

import java.awt.Color;

public enum ZoneType {
    COMMERCIAL("商业区", Color.BLUE),
    RESIDENTIAL("住宅区", Color.GREEN),
    INDUSTRIAL("工业区", Color.ORANGE);

    private final String name;
    private final Color color;

    ZoneType(String name, Color color) {
        this.name = name;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public Color getColor() {
        return color;
    }
}