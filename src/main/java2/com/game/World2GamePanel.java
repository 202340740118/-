package com.game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;

public class World2GamePanel extends JPanel {
    private final GameFrame2 frame;
    private final World2GameLogic gameLogic;
    private final JLabel infoLabel;
    private final JButton rollButton;
    private final JButton exitButton;
    private final Image backgroundImage;

    public World2GamePanel(GameFrame2 frame, String worldImage) {
        this.frame = frame;
        this.gameLogic = new World2GameLogic();
        this.backgroundImage = new ImageIcon(getClass().getResource(worldImage)).getImage();

        // 加载玩家图片
        try {
            // 修正：使用 ImageIO.read 替代 ImageIcon.getImage
            Image player1Img = ImageIO.read(getClass().getResource("/roles/role3.png"));
            Image player2Img = ImageIO.read(getClass().getResource("/roles/role4.png"));
            gameLogic.getPlayers()[0].setCharacterImage(player1Img);
            gameLogic.getPlayers()[1].setCharacterImage(player2Img);
        } catch (IOException | IllegalArgumentException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "角色图片加载失败，将使用默认图标", "资源错误", JOptionPane.WARNING_MESSAGE);
        }

        setLayout(new BorderLayout());

        // 顶部信息面板
        infoLabel = new JLabel("游戏准备中，玩家1先行动", JLabel.CENTER);
        infoLabel.setFont(new Font("宋体", Font.BOLD, 16));
        add(infoLabel, BorderLayout.NORTH);

        // 底部按钮面板
        JPanel buttonPanel = new JPanel();
        rollButton = new JButton("掷骰子");
        exitButton = new JButton("退出世界");

        rollButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!gameLogic.isGameOver()) {
                    int dice = gameLogic.rollDice();
                    gameLogic.movePlayer(dice);
                    gameLogic.switchPlayer();
                    infoLabel.setText(gameLogic.getGameInfo());
                    repaint();
                }
            }
        });

        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.returnToWorldSelect();
            }
        });

        buttonPanel.add(rollButton);
        buttonPanel.add(exitButton);
        add(buttonPanel, BorderLayout.SOUTH);

        gameLogic.startGame();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        drawGameBoard(g);
    }

    private void drawGameBoard(Graphics g) {
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        int radius = 250;

        // 绘制跑道背景
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 绘制外圆
        g2d.setColor(new Color(240, 240, 255));
        g2d.fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2);

        // 绘制内圆（空心部分）
        g2d.setColor(new Color(245, 245, 245));
        g2d.fillOval(centerX - radius + 80, centerY - radius + 80, (radius - 80) * 2, (radius - 80) * 2);

        // 绘制跑道格子
        for (int i = 0; i < 12; i++) {
            double angle = i * Math.PI / 6;
            int x = centerX + (int) ((radius - 40) * Math.cos(angle));
            int y = centerY + (int) ((radius - 40) * Math.sin(angle));
            drawCell(g, gameLogic.getBoard().get(i), x, y, angle);
        }

        // 绘制玩家位置
        for (Player player : gameLogic.getPlayers()) {
            int pos = player.getPosition();
            double angle = pos * Math.PI / 6;
            int x = centerX + (int) ((radius - 40) * Math.cos(angle));
            int y = centerY + (int) ((radius - 40) * Math.sin(angle));

            // 玩家图标位置微调
            double offsetAngle = angle + (player.getPlayerId() == 1 ? Math.PI/12 : -Math.PI/12);
            int offsetX = (int) (x + 30 * Math.cos(offsetAngle));
            int offsetY = (int) (y + 30 * Math.sin(offsetAngle));

            if (player.getCharacterImage() != null) {
                g.drawImage(player.getCharacterImage(), offsetX - 20, offsetY - 20, 40, 40, this);
            } else {
                // 默认图标
                g.setColor(player.getPlayerId() == 1 ? Color.BLUE : Color.RED);
                g.fillOval(offsetX - 15, offsetY - 15, 30, 30);
                g.setColor(Color.WHITE);
                g.setFont(new Font("宋体", Font.BOLD, 12));
                g.drawString("P" + player.getPlayerId(), offsetX - 5, offsetY + 4);
            }
        }

        // 绘制游戏标题
        g.setColor(Color.BLACK);
        g.setFont(new Font("宋体", Font.BOLD, 24));
        g.drawString("赛博世界", centerX - 60, centerY);

        // 绘制货币单位
        g.setFont(new Font("宋体", Font.PLAIN, 16));
        g.drawString("货币单位: 比特币", centerX - 80, centerY + 30);
    }

    private void drawCell(Graphics g, BoardCell cell, int x, int y, double angle) {
        Graphics2D g2d = (Graphics2D) g;

        // 旋转画布以绘制倾斜的格子
        g2d.rotate(angle, x, y);

        // 绘制格子背景
        g2d.setColor(cell.getOwnerColor());
        g2d.fillRoundRect(x - 40, y - 25, 80, 50, 10, 10);

        // 绘制格子边框
        g2d.setColor(Color.BLACK);
        g2d.drawRoundRect(x - 40, y - 25, 80, 50, 10, 10);

        // 绘制格子类型
        g2d.setFont(new Font("宋体", Font.PLAIN, 12));
        g2d.drawString(cell.getType(), x - 35, y - 5);

        // 绘制所有者信息
        if (cell.getOwner() != 0) {
            g2d.setColor(cell.getOwner() == 1 ? new Color(0, 0, 150) : new Color(150, 0, 0));
            g2d.setFont(new Font("宋体", Font.BOLD, 12));
            g2d.drawString("玩家" + cell.getOwner(), x - 35, y + 15);
        }

        // 恢复画布旋转
        g2d.rotate(-angle, x, y);
    }
}