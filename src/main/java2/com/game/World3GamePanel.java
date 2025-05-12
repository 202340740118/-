package com.game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.util.*;
import java.awt.geom.GeneralPath;

public class World3GamePanel extends JPanel {
    private final GameFrame2 frame;
    private final World3GameLogic gameLogic;
    private final JLabel infoLabel;
    private final Image backgroundImage;
    private final Image[] roleImages = new Image[2];
    private List<Point> cellPositions;

    public World3GamePanel(GameFrame2 frame, String worldImage) {
        this.frame = frame;
        this.gameLogic = new World3GameLogic();
        this.backgroundImage = new ImageIcon(getClass().getResource(worldImage)).getImage();

        // 加载角色图片
        try {
            roleImages[0] = ImageIO.read(getClass().getResource("/roles/role5.png"));
            roleImages[1] = ImageIO.read(getClass().getResource("/roles/role6.png"));
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "角色图片加载失败", "错误", JOptionPane.ERROR_MESSAGE);
        }

        setLayout(new BorderLayout());

        // 信息面板
        infoLabel = new JLabel("", JLabel.CENTER);
        infoLabel.setFont(new Font("宋体", Font.BOLD, 16));
        add(infoLabel, BorderLayout.NORTH);

        // 游戏面板
        JPanel gamePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawGameElements(g);
            }
        };
        add(gamePanel, BorderLayout.CENTER);

        // 按钮面板
        JPanel buttonPanel = new JPanel();
        JButton rollBtn = new JButton("掷骰子");
        JButton exitBtn = new JButton("退出");

        rollBtn.addActionListener(e -> handleRollDice());
        exitBtn.addActionListener(e -> frame.returnToWorldSelect());

        buttonPanel.add(rollBtn);
        buttonPanel.add(exitBtn);
        add(buttonPanel, BorderLayout.SOUTH);

        gameLogic.startGame();
        updateGameInfo();
    }

    private void handleRollDice() {
        if (!gameLogic.isGameOver()) {
            int dice = gameLogic.rollDice();
            gameLogic.movePlayer(dice);
            updateGameInfo();
            repaint();
            gameLogic.switchPlayer();
        }
    }

    private void updateGameInfo() {
        infoLabel.setText(gameLogic.getGameInfo());
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        cellPositions = calculateCellPositions();
        g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        drawGameElements(g);
    }

    private List<Point> calculateCellPositions() {
        List<Point> positions = new ArrayList<>();
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2 - 50;
        int radius = 200;

        // 上半环 0-7
        for (int i = 0; i < 8; i++) {
            double angle = i * Math.PI / 4;
            positions.add(new Point(
                    centerX + (int)(radius * Math.cos(angle)),
                    centerY - (int)(radius * Math.sin(angle))
            );
        }

        // 交叉点 8
        positions.add(new Point(centerX, centerY));

        // 下半环 9-15
        for (int i = 0; i < 7; i++) {
            double angle = Math.PI + i * Math.PI / 3.5;
            positions.add(new Point(
                    centerX + (int)(radius * Math.cos(angle)),
                    centerY + (int)(radius * Math.sin(angle))
            );
        }
        return positions;
    }

    private void drawGameElements(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 绘制路径
        drawPath(g2d);

        // 绘制地块
        for (int i = 0; i < gameLogic.getBoard().size(); i++) {
            BoardCell cell = gameLogic.getBoard().get(i);
            Point pos = cellPositions.get(i);
            drawCell(g2d, cell, pos.x, pos.y);
        }

        // 绘制玩家
        drawPlayers(g2d);
    }

    private void drawPath(Graphics2D g2d) {
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2 - 50;
        g2d.setColor(new Color(180, 230, 255, 100));
        g2d.setStroke(new BasicStroke(40));
        g2d.drawArc(centerX - 200, centerY - 200, 400, 400, 0, -180); // 上半环
        g2d.drawArc(centerX - 200, centerY, 400, 400, 0, 180);        // 下半环
    }

    private void drawCell(Graphics2D g2d, BoardCell cell, int x, int y) {
        GeneralPath cloud = createCloud(x, y);
        g2d.setColor(getCellColor(cell.getOwner()));
        g2d.fill(cloud);
        g2d.setColor(Color.DARK_GRAY);
        g2d.draw(cloud);

        // 绘制文字
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("楷体", Font.BOLD, 12));
        String[] lines = cell.getType().split(" ");
        for (int i = 0; i < lines.length; i++) {
            g2d.drawString(lines[i], x - 25, y - 30 + i * 15);
        }

        if (cell.getOwner() != 0) {
            g2d.drawString("P" + cell.getOwner(), x - 8, y + 20);
        }
    }

    private Color getCellColor(int owner) {
        return switch (owner) {
            case 1 -> new Color(135, 206, 250);
            case 2 -> new Color(255, 182, 193);
            default -> new Color(240, 248, 255);
        };
    }

    private GeneralPath createCloud(int x, int y) {
        GeneralPath path = new GeneralPath();
        path.moveTo(x - 30, y + 10);
        path.curveTo(x - 40, y, x - 30, y - 20, x, y - 20);
        path.curveTo(x + 30, y - 20, x + 40, y, x + 30, y + 10);
        path.curveTo(x + 40, y + 20, x + 20, y + 30, x, y + 30);
        path.curveTo(x - 20, y + 30, x - 40, y + 20, x - 30, y + 10);
        return path;
    }

    private void drawPlayers(Graphics2D g2d) {
        for (int i = 0; i < 2; i++) {
            Player player = gameLogic.getPlayers()[i];
            int posIndex = player.getPosition();
            if (posIndex >= cellPositions.size()) continue;

            Point pos = cellPositions.get(posIndex);
            Point offset = calculateOffset(posIndex, i);

            Image img = roleImages[i];
            if (img != null) {
                g2d.drawImage(img, offset.x - 20, offset.y - 20, 40, 40, null);
            } else {
                g2d.setColor(i == 0 ? Color.BLUE : Color.RED);
                g2d.fillOval(offset.x - 15, offset.y - 15, 30, 30);
            }
        }
    }

    private Point calculateOffset(int posIndex, int playerId) {
        Point pos = cellPositions.get(posIndex);
        int offset = 25;

        if (posIndex < 8) { // 上半环
            double angle = posIndex * Math.PI / 4;
            return new Point(
                    pos.x + (int)(offset * Math.cos(angle + (playerId == 0 ? Math.PI/6 : -Math.PI/6)),
                    pos.y - (int)(offset * Math.sin(angle + (playerId == 0 ? Math.PI/6 : -Math.PI/6))
                    );
        } else if (posIndex == 8) { // 中心
            return new Point(pos.x + (playerId == 0 ? -offset : offset), pos.y);
        } else { // 下半环
            double angle = Math.PI + (posIndex - 9) * Math.PI / 3.5;
            return new Point(
                    pos.x + (int)(offset * Math.cos(angle + (playerId == 0 ? Math.PI/6 : -Math.PI/6)),
                    pos.y + (int)(offset * Math.sin(angle + (playerId == 0 ? Math.PI/6 : -Math.PI/6))
                    );
        }
    }
}