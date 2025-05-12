package com.game;

import java.util.*;
import javax.swing.*;

public class World3GameLogic {
    private final List<BoardCell> board;
    private final Player[] players;
    private int currentPlayerIndex;
    private final Random random;
    private boolean gameOver;

    public World3GameLogic() {
        this.board = createImmortalWorldBoard();
        this.players = new Player[]{
                new Player(1, 1500, null),  // 玩家1初始灵石1500
                new Player(2, 1500, null)    // 玩家2初始灵石1500
        };
        this.random = new Random();
        this.currentPlayerIndex = 0;
        this.gameOver = false;
    }

    private List<BoardCell> createImmortalWorldBoard() {
        List<BoardCell> cells = new ArrayList<>();
        // 八字形16格地图坐标及属性
        int[][] positions = {
                {350, 100},  {450, 150},  {550, 200},  {650, 250},   // 右上区域（0-3）
                {650, 350},  {550, 400},  {450, 450},  {350, 500},   // 右下区域（4-7）
                {250, 450},  {150, 400},  {50, 350},   {50, 250},    // 左下区域（8-11）
                {150, 200},  {250, 150},  {350, 300},  {350, 100}    // 左上区域+交叉点（12-15，15为修炼圣地）
        };

        String[] types = {
                "灵泉", "仙草园", "炼丹房", "灵脉核心",
                "天劫谷", "修炼洞府", "仙器阁", "灵兽园",
                "迷雾森林", "上古遗迹", "渡劫台", "灵石矿",
                "飞升台", "仙宫别院", "起点", "修炼圣地"
        };

        for (int i = 0; i < 16; i++) {
            int price = switch (i) {
                case 3, 11 -> 500;    // 灵脉核心、灵石矿
                case 4, 10 -> 0;       // 天劫谷、渡劫台
                case 15 -> 800;        // 修炼圣地
                default -> 200 + (i % 3) * 100;
            };
            int rent = price > 0 ? price / 5 : 0;
            cells.add(new BoardCell(i, types[i], positions[i][0], positions[i][1], price, rent));
        }
        return cells;
    }

    public void startGame() {
        JOptionPane.showMessageDialog(null, "仙界之旅开始！玩家1先行");
    }

    public int rollDice() {
        if (gameOver) return 0;
        int dice = random.nextInt(6) + 1;
        JOptionPane.showMessageDialog(null, "玩家" + (currentPlayerIndex + 1) + "掷出骰子：" + dice);
        return dice;
    }

    public void movePlayer(int steps) {
        Player currentPlayer = getCurrentPlayer();
        int oldPosition = currentPlayer.getPosition();
        int newPosition = (oldPosition + steps) % 16;

        // 处理交叉点逻辑
        if ((oldPosition < 8 && newPosition >= 8) || (oldPosition >= 8 && newPosition < 8)) {
            newPosition = 15;
        }

        currentPlayer.setPosition(newPosition);
        handleCellAction(currentPlayer);
        checkGameOver();
    }

    private void handleCellAction(Player player) {
        BoardCell cell = board.get(player.getPosition());
        switch (cell.getType()) {
            case "灵脉核心", "灵石矿" -> {
                int reward = 300 + random.nextInt(200);
                player.addMoney(reward);
                showMessage("发现" + cell.getType() + "！获得" + reward + "灵石", "灵脉富集");
            }
            case "天劫谷", "渡劫台" -> {
                int loss = 200 + random.nextInt(300);
                player.deductMoney(loss);
                showMessage("遭遇" + cell.getType() + "！损失" + loss + "灵石", "渡劫失败");
            }
            case "修炼圣地" -> applySpecialEvent(player);
            default -> handlePropertyCell(player, cell);
        }
    }

    private void applySpecialEvent(Player player) {
        int event = random.nextInt(4);
        switch (event) {
            case 0 -> {
                player.addMoney(800);
                showMessage("在修炼圣地顿悟！获得800灵石", "天道感悟");
            }
            case 1 -> {
                for (Player p : players) {
                    if (p.getId() != player.getId()) {
                        int steal = p.getMoney() / 10;
                        p.deductMoney(steal);
                        player.addMoney(steal);
                        showMessage("掠夺玩家" + p.getId() + "获得" + steal + "灵石", "资源掠夺");
                    }
                }
            }
            case 2 -> {
                int loss = player.getMoney() / 5;
                player.deductMoney(loss);
                showMessage("在圣地遭遇伏击！损失" + loss + "灵石", "危险事件");
            }
            case 3 -> {
                player.addMoney(1000);
                showMessage("发现上古传承！获得1000灵石", "传承奇遇");
            }
        }
    }

    private void handlePropertyCell(Player player, BoardCell cell) {
        if (cell.getOwner() == 0) {
            if (player.getMoney() >= cell.getPrice() && cell.getPrice() > 0) {
                int choice = JOptionPane.showConfirmDialog(null,
                        "是否花费" + cell.getPrice() + "灵石购买" + cell.getType() + "？",
                        "地块购买", JOptionPane.YES_NO_OPTION);
                if (choice == JOptionPane.YES_OPTION) {
                    cell.setOwner(player.getId());
                    player.deductMoney(cell.getPrice());
                    showMessage("成功购买" + cell.getType() + "！", "购买成功");
                }
            }
        } else if (cell.getOwner() != player.getId()) {
            int rent = cell.getRent();
            if (cell.getType().equals("仙宫别院")) rent *= 2;
            player.deductMoney(rent);
            players[cell.getOwner() - 1].addMoney(rent);
            showMessage("支付" + rent + "灵石租金给玩家" + cell.getOwner(), "租金扣除");
        }
    }

    private void showMessage(String message, String title) {
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.INFORMATION_MESSAGE);
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
                int winner = (player.getId() == 1) ? 2 : 1;
                showMessage("玩家" + winner + "获胜！对手灵石耗尽破产", "游戏结束");
                return;
            }
        }

        // 检查地块数
        int[] owned = new int[2];
        for (BoardCell cell : board) {
            if (cell.getOwner() > 0) owned[cell.getOwner() - 1]++;
        }
        if (owned[0] >= 12 || owned[1] >= 12) {
            gameOver = true;
            int winner = (owned[0] >= 12) ? 1 : 2;
            showMessage("玩家" + winner + "获胜！成功占领12块以上仙府", "游戏结束");
        }
    }

    // Getters
    public Player getCurrentPlayer() { return players[currentPlayerIndex]; }
    public List<BoardCell> getBoard() { return board; }
    public boolean isGameOver() { return gameOver; }
    public String getGameInfo() {
        return "玩家" + (currentPlayerIndex + 1) + "回合 | 灵石: " + getCurrentPlayer().getMoney();
    }
}