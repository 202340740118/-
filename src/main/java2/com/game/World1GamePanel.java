package com.game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class World1GamePanel extends JPanel {
    private final GameFrame2 frame;
    private final World1GameLogic gameLogic;
    private final JLabel infoLabel;
    private final JLabel diceLabel;
    private final JButton buyButton;
    private final JButton passButton;
    private final JButton exitButton;
    private final Image backgroundImage;

    public World1GamePanel(GameFrame2 frame, String worldImage) {
        this.frame = frame;
        this.gameLogic = new World1GameLogic();
        this.backgroundImage = new ImageIcon(getClass().getResource(worldImage)).getImage();

        setLayout(new BorderLayout());

        // 顶部信息面板
        JPanel topPanel = new JPanel(new GridLayout(2, 1));
        infoLabel = new JLabel("玩家1回合，请选择地块", JLabel.CENTER);
        diceLabel = new JLabel("等待掷骰...", JLabel.CENTER);
        topPanel.add(infoLabel);
        topPanel.add(diceLabel);
        add(topPanel, BorderLayout.NORTH);

        // 中心游戏面板
        JPanel centerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawGameBoard(g);
            }
        };
        centerPanel.setPreferredSize(new Dimension(800, 800));
        centerPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!gameLogic.isCurrentPlayerFrozen()) {
                    handleBoardClick(e.getX(), e.getY());
                }
            }
        });
        add(centerPanel, BorderLayout.CENTER);

        // 底部按钮面板
        JPanel bottomPanel = new JPanel();
        buyButton = new JButton("购买");
        passButton = new JButton("跳过");
        exitButton = new JButton("退出世界");

        buyButton.addActionListener(e -> {
            gameLogic.buyLand();
            diceLabel.setText(gameLogic.getDiceMessage());
            refreshUI();
        });

        passButton.addActionListener(e -> {
            gameLogic.switchPlayer();
            refreshUI();
        });

        exitButton.addActionListener(e -> frame.returnToWorldSelect());

        bottomPanel.add(buyButton);
        bottomPanel.add(passButton);
        bottomPanel.add(exitButton);
        add(bottomPanel, BorderLayout.SOUTH);

        gameLogic.startGame();
        refreshUI();
    }

    private void drawGameBoard(Graphics g) {
        // 绘制背景
        g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;

        // 绘制三个环
        drawRing(g2d, centerX, centerY, 100, gameLogic.getLands().subList(0, 4)); // 内环
        drawRing(g2d, centerX, centerY, 200, gameLogic.getLands().subList(4, 10)); // 中环
        drawRing(g2d, centerX, centerY, 300, gameLogic.getLands().subList(10, 18)); // 外环

        // 绘制中心信息
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("宋体", Font.BOLD, 20));
        g2d.drawString("现代世界", centerX - 40, centerY);

        // 绘制冻结状态指示
        if (gameLogic.isCurrentPlayerFrozen()) {
            g2d.setColor(Color.RED);
            g2d.setFont(new Font("宋体", Font.BOLD, 24));
            g2d.drawString("玩家" + gameLogic.getCurrentPlayer() + "被冻结！", centerX - 80, centerY + 30);
        }
    }

    private void drawRing(Graphics2D g2d, int centerX, int centerY, int radius, List<RadialLand> lands) {
        int landCount = lands.size();
        double angleStep = 2 * Math.PI / landCount;

        for (int i = 0; i < landCount; i++) {
            double angle = i * angleStep;
            int landRadius = radius / 3;

            // 计算地块中心位置
            int x = (int) (centerX + radius * Math.cos(angle));
            int y = (int) (centerY + radius * Math.sin(angle));

            // 绘制地块
            RadialLand land = lands.get(i);
            g2d.setColor(land.getDisplayColor());
            g2d.fillOval(x - landRadius, y - landRadius, landRadius * 2, landRadius * 2);

            // 绘制边框
            g2d.setColor(Color.BLACK);
            g2d.drawOval(x - landRadius, y - landRadius, landRadius * 2, landRadius * 2);

            // 绘制地块信息
            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("宋体", Font.PLAIN, 12));
            String[] lines = land.getDisplayText().split(" ");
            for (int j = 0; j < lines.length; j++) {
                g2d.drawString(lines[j], x - landRadius + 5, y - landRadius + 15 + j * 15);
            }

            // 绘制人物图片
            if (land.getOwner() != 0) {
                try {
                    // 注意：世界1使用role1.png和role2.png
                    String imagePath = "/roles/role" + land.getOwner() + ".png";
                    Image img = new ImageIcon(getClass().getResource(imagePath)).getImage();
                    g2d.drawImage(img, x - 20, y - 20, 80, 80, this);
                } catch (Exception e) {
                    // 图片加载失败时显示玩家编号
                    g2d.setColor(land.getOwner() == 1 ? Color.BLUE : Color.RED);
                    g2d.setFont(new Font("宋体", Font.BOLD, 16));
                    g2d.drawString("P" + land.getOwner(), x - 10, y + 10);
                }
            }
        }
    }

    private void handleBoardClick(int x, int y) {
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;

        // 计算点击位置与中心的距离和角度
        double dx = x - centerX;
        double dy = y - centerY;
        double distance = Math.sqrt(dx * dx + dy * dy);
        double angle = Math.atan2(dy, dx);
        if (angle < 0) angle += 2 * Math.PI;

        // 确定点击的环
        int ring;
        if (distance < 150) {
            ring = 1; // 内环
        } else if (distance < 250) {
            ring = 2; // 中环
        } else if (distance < 350) {
            ring = 3; // 外环
        } else {
            return; // 点击在外部
        }

        // 确定点击的地块
        List<RadialLand> landsInRing;
        switch (ring) {
            case 1: landsInRing = gameLogic.getLands().subList(0, 4); break;
            case 2: landsInRing = gameLogic.getLands().subList(4, 10); break;
            case 3: landsInRing = gameLogic.getLands().subList(10, 18); break;
            default: return;
        }

        int landCount = landsInRing.size();
        double angleStep = 2 * Math.PI / landCount;
        int position = (int) (angle / angleStep) % landCount;

        gameLogic.selectLand(landsInRing.get(position));
        refreshUI();
    }

    private void refreshUI() {
        infoLabel.setText(gameLogic.getGameInfo());

        // 更新按钮状态
        boolean isFrozen = gameLogic.isCurrentPlayerFrozen();
        RadialLand selected = gameLogic.getSelectedLand();

        buyButton.setEnabled(!isFrozen && selected != null && selected.getOwner() == 0);
        passButton.setEnabled(!isFrozen);

        repaint();
    }
}