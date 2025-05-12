package com.game;

import java.awt.Color;

public class BoardCell {
    private final int id;
    private final String type;
    private int owner; // 0=无主，1=玩家1，2=玩家2
    private final int price;
    private final int rent;
    private final int x;
    private final int y;

    public BoardCell(int id, String type, int x, int y, int price, int rent) {
        this.id = id;
        this.type = type;
        this.x = x;
        this.y = y;
        this.price = price;
        this.rent = rent;
        this.owner = 0; // 默认无主
    }

    // 核心属性访问
    public int getId() { return id; }
    public String getType() { return type; }
    public int getOwner() { return owner; }
    public void setOwner(int owner) { this.owner = owner; }
    public int getPrice() { return price; }
    public int getRent() { return rent; }
    public int getX() { return x; }
    public int getY() { return y; }

    // 获取地块显示颜色
    public Color getOwnerColor() {
        return switch(owner) {
            case 1 -> new Color(0, 100, 200); // 玩家1颜色（蓝色系）
            case 2 -> new Color(200, 50, 0);  // 玩家2颜色（红色系）
            default -> new Color(220, 240, 255); // 无主颜色
        };
    }
}