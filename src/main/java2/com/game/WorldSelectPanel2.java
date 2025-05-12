package com.game;

import javax.swing.*;
import java.awt.*;

public class WorldSelectPanel2 extends JPanel {
    private final GameFrame2 frame;

    public WorldSelectPanel2(GameFrame2 frame) {
        this.frame = frame;
        setLayout(new GridLayout(1, 3, 20, 20));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        addWorldCard("现代世界", "使用资金\n都市探索之旅", "/worlds/world1.png",
                "<html><div style='width:400px;'><h2>现代世界规则</h2>" +
                        "<p><b>地图布局：</b>内环4商业区、中环6住宅区、外环8工业区</p>" +
                        "<p><b>购买价格：</b>内环2000、中环1500、外环1000</p>" +
                        "<p><b>骰子效果：</b><br>" +
                        "1: 当前玩家扣500资金<br>" +
                        "2: 当前玩家得200资金<br>" +
                        "3: 当前玩家得300资金<br>" +
                        "4: 当前玩家下回合冻结<br>" +
                        "5: 当前玩家得500资金<br>" +
                        "6: 当前玩家得600资金</p>" +
                        "<p><b>胜利条件：</b><br>" +
                        "1. 独占任意一环4块地<br>" +
                        "2. 对手破产<br>" +
                        "3. 形成一条住→商→工放射链</p></div></html>");

        addWorldCard("赛博世界", "使用比特\n光怪赛博穿梭", "/worlds/world2.png",
                "<html><div style='width:300px;'><h2>赛博世界规则</h2>" +
                        "<p>跑道型12格地图，使用比特币</p>" +
                        "<p>掷骰子移动，空地可购买，矿场触发奖励</p>" +
                        "<p>胜利条件：对手破产</p></div></html>");

        addWorldCard("仙界", "使用灵石\n御剑仙途问道", "/worlds/world3.png",
                "<html><div style='width:300px;'><h2>仙界规则</h2>" +
                        "<p>八字型16格地图，使用灵石</p>" +
                        "<p>4点骰子触发双倍移动，灵脉/天劫特殊事件</p>" +
                        "<p>胜利条件：对手破产</p></div></html>");
    }

    private void addWorldCard(String name, String desc, String imagePath, String rules) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        JButton btn = new JButton(new ImageIcon(getClass().getResource(imagePath)));
        btn.setPreferredSize(new Dimension(400, 200));
        btn.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(
                    frame, rules, name + "规则",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);
            if (choice == JOptionPane.OK_OPTION || choice == JOptionPane.CLOSED_OPTION) {
                frame.showGamePanel(imagePath);
            }
        });

        JLabel nameLabel = new JLabel(name, JLabel.CENTER);
        nameLabel.setFont(new Font("宋体", Font.BOLD, 16));

        JTextArea descArea = new JTextArea(desc);
        descArea.setEditable(false);
        descArea.setOpaque(false);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setFont(new Font("宋体", Font.PLAIN, 14));

        JPanel textPanel = new JPanel(new BorderLayout());
        textPanel.add(nameLabel, BorderLayout.NORTH);
        textPanel.add(descArea, BorderLayout.CENTER);

        card.add(btn, BorderLayout.CENTER);
        card.add(textPanel, BorderLayout.SOUTH);
        add(card);
    }
}