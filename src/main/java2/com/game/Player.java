package com.game;

import java.awt.Image;

public class Player {
    private int playerId;
    private int money;
    private int position;
    private Image characterImage; // 添加图片字段

    public Player(int playerId, int money, Image characterImage) {
        this.playerId = playerId;
        this.money = money;
        this.position = 0;
        this.characterImage = characterImage;
    }

    // Getters and setters
    public int getPlayerId() {
        return playerId;
    }

    public int getMoney() {
        return money;
    }

    public void setMoney(int money) {
        this.money = money;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    // 添加图片设置和获取方法
    public void setCharacterImage(Image image) {
        this.characterImage = image;
    }

    public Image getCharacterImage() {
        return characterImage;
    }
}