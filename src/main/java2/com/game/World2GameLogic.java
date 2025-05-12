package com.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.JOptionPane;

public class World2GameLogic {
    private final List<BoardCell> board;
    private final Player[] players;
    private int currentPlayerIndex;
    private final Random random;
    private boolean gameStarted;
    private boolean gameOver;

    public World2GameLogic() {
        this.board = createBoard();
        this.players = new Player[]{
                new Player(1, 1000, null), // 玩家1图片在面板中设置
                new Player(2,1000, null)  // 玩家2图片在面板中设置
        };
        this.currentPlayerIndex = 0;
        this.random = new Random();
        this.gameStarted = false;
        this.gameOver = false;
    }

    private List<BoardCell> createBoard() {
        List<BoardCell> cells = new ArrayList<>();
        // 跑道型12格地图（起点、矿场、普通地块、比特币交易所）
        cells.add(new BoardCell(0, "起点", 0, 0,0,0));
        cells.add(new BoardCell(1, "普通地块", 150, 30,0,0));
        cells.add(new BoardCell(2, "普通地块", 150, 30,0,0));
        cells.add(new BoardCell(3, "比特币矿场", 300, 100,0,0));
        cells.add(new BoardCell(4, "普通地块", 200, 50,0,0));
        cells.add(new BoardCell(5, "普通地块", 200, 50,0,0));
        cells.add(new BoardCell(6, "比特币交易所", 0, 0,0,0)); // 特殊事件
        cells.add(new BoardCell(7, "普通地块", 250, 70,0,0));
        cells.add(new BoardCell(8, "普通地块", 250, 70,0,0));
        cells.add(new BoardCell(9, "比特币矿场", 350, 120,0,0));
        cells.add(new BoardCell(10, "普通地块", 300, 90,0,0));
        cells.add(new BoardCell(11, "普通地块", 300, 90,0,0));
        return cells;
    }

    public void startGame() {
        this.gameStarted = true;
    }

    public int rollDice() {
        if (gameOver) return 0;

        int dice = random.nextInt(6) + 1;
        JOptionPane.showMessageDialog(null,
                "玩家" + (currentPlayerIndex + 1) + "掷出" + dice,
                "骰子结果", JOptionPane.INFORMATION_MESSAGE);
        return dice;
    }

    public void movePlayer(int dice) {
        if (gameOver) return;

        Player currentPlayer = players[currentPlayerIndex];
        int oldPosition = currentPlayer.getPosition();
        int newPosition = (oldPosition + dice) % 12;
        currentPlayer.setPosition(newPosition);

        // 经过起点奖励
        if (newPosition < oldPosition) {
            currentPlayer.setMoney(currentPlayer.getMoney() + 200);
            JOptionPane.showMessageDialog(null,
                    "经过起点，获得200比特币",
                    "奖励", JOptionPane.INFORMATION_MESSAGE);
        }

        handleCell(currentPlayer);
        checkGameOver();
    }

    private void handleCell(Player player) {
        BoardCell cell = board.get(player.getPosition());

        if (cell.getType().equals("起点")) {
            // 已在movePlayer中处理
        } else if (cell.getType().equals("比特币矿场")) {
            int reward = random.nextInt(300) + 200; // 200-500随机奖励
            player.setMoney(player.getMoney() + reward);
            JOptionPane.showMessageDialog(null,
                    "发现矿场，获得" + reward + "比特币",
                    "奖励", JOptionPane.INFORMATION_MESSAGE);
        } else if (cell.getType().equals("比特币交易所")) {
            // 随机事件
            int event = random.nextInt(3);
            switch (event) {
                case 0:
                    int gain = (int) (player.getMoney() * 0.2);
                    player.setMoney(player.getMoney() + gain);
                    JOptionPane.showMessageDialog(null,
                            "比特币大涨！获得" + gain + "比特币",
                            "市场波动", JOptionPane.INFORMATION_MESSAGE);
                    break;
                case 1:
                    int loss = (int) (player.getMoney() * 0.15);
                    player.setMoney(player.getMoney() - loss);
                    JOptionPane.showMessageDialog(null,
                            "比特币大跌！损失" + loss + "比特币",
                            "市场波动", JOptionPane.INFORMATION_MESSAGE);
                    break;
                case 2:
                    for (int i = 0; i < players.length; i++) {
                        if (i != currentPlayerIndex) {
                            int steal = (int) (players[i].getMoney() * 0.1);
                            if (steal > 0) {
                                players[i].setMoney(players[i].getMoney() - steal);
                                player.setMoney(player.getMoney() + steal);
                                JOptionPane.showMessageDialog(null,
                                        "黑客攻击！从玩家" + (i + 1) + "窃取" + steal + "比特币",
                                        "特殊事件", JOptionPane.INFORMATION_MESSAGE);
                            }
                        }
                    }
                    break;
            }
        } else if (cell.getOwner() == 0) {
            // 可购买地块
            if (player.getMoney() >= cell.getPrice()) {
                int choice = JOptionPane.showConfirmDialog(null,
                        "是否花费" + cell.getPrice() + "比特币购买" + cell.getType() + "？",
                        "购买选项", JOptionPane.YES_NO_OPTION);
                if (choice == JOptionPane.YES_OPTION) {
                    player.setMoney(player.getMoney() - cell.getPrice());
                    cell.setOwner(player.getPlayerId());
                    JOptionPane.showMessageDialog(null,
                            "成功购买" + cell.getType() + "！",
                            "购买成功", JOptionPane.INFORMATION_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(null,
                        "资金不足，无法购买" + cell.getType(),
                        "购买失败", JOptionPane.INFORMATION_MESSAGE);
            }
        } else if (cell.getOwner() != player.getPlayerId()) {
            // 支付租金
            Player owner = players[cell.getOwner() - 1];
            int rent = cell.getRent();

            // 如果拥有相邻地块，租金翻倍
            int adjacentCount = 0;
            int pos = cell.getId();
            if (pos > 0 && board.get(pos-1).getOwner() == cell.getOwner()) adjacentCount++;
            if (pos < 11 && board.get(pos+1).getOwner() == cell.getOwner()) adjacentCount++;
            if (pos == 0 && board.get(11).getOwner() == cell.getOwner()) adjacentCount++;
            if (pos == 11 && board.get(0).getOwner() == cell.getOwner()) adjacentCount++;

            if (adjacentCount > 0) {
                rent *= (adjacentCount + 1); // 1个相邻翻倍，2个相邻3倍
                JOptionPane.showMessageDialog(null,
                        "玩家" + cell.getOwner() + "拥有相邻地块，租金翻倍！",
                        "租金加倍", JOptionPane.INFORMATION_MESSAGE);
            }

            if (player.getMoney() >= rent) {
                player.setMoney(player.getMoney() - rent);
                owner.setMoney(owner.getMoney() + rent);
                JOptionPane.showMessageDialog(null,
                        "支付" + rent + "比特币租金给玩家" + cell.getOwner(),
                        "租金扣除", JOptionPane.INFORMATION_MESSAGE);
            } else {
                // 破产处理
                owner.setMoney(owner.getMoney() + player.getMoney());
                JOptionPane.showMessageDialog(null,
                        "玩家" + (currentPlayerIndex + 1) + "资金不足，支付剩余" + player.getMoney() + "比特币后破产！",
                        "破产", JOptionPane.INFORMATION_MESSAGE);
                player.setMoney(0);
                gameOver = true;
            }
        }
    }

    public void switchPlayer() {
        if (gameOver) return;
        currentPlayerIndex = 1 - currentPlayerIndex;
    }

    private void checkGameOver() {
        // 检查破产
        for (Player player : players) {
            if (player.getMoney() <= 0) {
                gameOver = true;
                int winner = (player.getPlayerId() == 1) ? 2 : 1;
                JOptionPane.showMessageDialog(null,
                        "玩家" + winner + "获胜！对手已破产。",
                        "游戏结束", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
        }

        // 检查拥有地块数
        int[] ownedCount = new int[2];
        for (BoardCell cell : board) {
            if (cell.getOwner() > 0) {
                ownedCount[cell.getOwner() - 1]++;
            }
        }

        for (int i = 0; i < 2; i++) {
            if (ownedCount[i] >= 8) {
                gameOver = true;
                JOptionPane.showMessageDialog(null,
                        "玩家" + (i + 1) + "获胜！拥有超过8块地块。",
                        "游戏结束", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
        }
    }

    // Getters
    public Player[] getPlayers() { return players; }
    public List<BoardCell> getBoard() { return board; }
    public int getCurrentPlayerIndex() { return currentPlayerIndex; }
    public boolean isGameOver() { return gameOver; }
    public String getGameInfo() {
        if (!gameStarted) return "游戏准备中，玩家" + (currentPlayerIndex + 1) + "先行动";
        if (gameOver) return "游戏结束 - 玩家" + (currentPlayerIndex + 1) + "获胜！";

        Player currentPlayer = players[currentPlayerIndex];
        return "玩家" + (currentPlayerIndex + 1) + "回合 | 比特币: " + currentPlayer.getMoney();
    }
}