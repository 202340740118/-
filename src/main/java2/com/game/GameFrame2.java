package com.game;

import javax.swing.*;

public class GameFrame2 extends JFrame {
    public GameFrame2() {
        setTitle("三界争霸");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1500, 800);
        setLocationRelativeTo(null);
        setResizable(false);

        getContentPane().add(new WorldSelectPanel2(this));
    }

    public void showGamePanel(String worldImage) {
        getContentPane().removeAll();

        if (worldImage.contains("world1")) {
            getContentPane().add(new World1GamePanel(this, worldImage));
        } else if (worldImage.contains("world2")) {
            getContentPane().add(new World2GamePanel(this, worldImage));
        } else if (worldImage.contains("world3")) {
            getContentPane().add(new World3GamePanel(this, worldImage));
        }

        revalidate();
        repaint();
    }

    public void returnToWorldSelect() {
        getContentPane().removeAll();
        getContentPane().add(new WorldSelectPanel2(this));
        revalidate();
        repaint();
    }
}