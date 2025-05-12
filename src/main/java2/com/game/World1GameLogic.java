package com.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.JOptionPane;

public class World1GameLogic {
    private final List<RadialLand> lands;
    private int currentPlayer = 1;
    private int[] playerMoney = {10000, 10000};
    private RadialLand selectedLand;
    private final Random random = new Random();
    private boolean gameOver = false;
    private int[] frozenTurns = {0, 0}; // 玩家冻结状态
    private int lastDiceResult = 0;
    private int lastDiceBonus = 0;
    private boolean gameStarted = false;

    public World1GameLogic() {
        this.lands = createRadialLands();
    }

    private List<RadialLand> createRadialLands() {
        List<RadialLand> lands = new ArrayList<>();

        // 内环 - 4个商业区
        for (int i = 0; i < 4; i++) {
            lands.add(new RadialLand(ZoneType.COMMERCIAL, 1, i));
        }

        // 中环 - 6个住宅区
        for (int i = 0; i < 6; i++) {
            lands.add(new RadialLand(ZoneType.RESIDENTIAL, 2, i));
        }

        // 外环 - 8个工业区
        for (int i = 0; i < 8; i++) {
            lands.add(new RadialLand(ZoneType.INDUSTRIAL, 3, i));
        }

        return lands;
    }

    public void selectLand(RadialLand land) {
        if (selectedLand != null) {
            selectedLand.setSelected(false);
        }
        selectedLand = land;
        land.setSelected(true);
    }

    public void buyLand() {
        if (!gameStarted || gameOver || frozenTurns[currentPlayer - 1] > 0) return;

        if (selectedLand.getOwner() == 0) {
            int cost = getLandCost(selectedLand);

            if (playerMoney[currentPlayer - 1] >= cost) {
                selectedLand.setOwner(currentPlayer);
                playerMoney[currentPlayer - 1] -= cost;

                // 立即掷骰并显示结果
                rollDice();
                applyDiceEffect();

                checkWinConditions();
                if (!gameOver) {
                    switchPlayer();
                } else {
                    showWinMessage();
                }
            }
        }
    }

    private int getLandCost(RadialLand land) {
        switch (land.getRing()) {
            case 1: return 2000;
            case 2: return 1500;
            case 3: return 1000;
            default: return 1000;
        }
    }

    private void rollDice() {
        lastDiceResult = random.nextInt(6) + 1;

        switch (lastDiceResult) {
            case 1:
                lastDiceBonus = -500;
                break;
            case 2:
                lastDiceBonus = 200;
                break;
            case 3:
                lastDiceBonus = 300;
                break;
            case 4:
                frozenTurns[currentPlayer - 1] = 1; // 当前玩家下一回合被冻结
                lastDiceBonus = 0;
                break;
            case 5:
                lastDiceBonus = 500;
                break;
            case 6:
                lastDiceBonus = 600;
                break;
        }
    }

    private void applyDiceEffect() {
        if (lastDiceResult != 4) {
            playerMoney[currentPlayer - 1] += lastDiceBonus;
        }
    }

    public void switchPlayer() {
        // 保存当前玩家
        int previousPlayer = currentPlayer;

        // 切换到下一玩家
        currentPlayer = 3 - currentPlayer;

        // 检查新玩家是否被冻结
        if (frozenTurns[currentPlayer - 1] > 0) {
            // 显示正确的冻结提示
            JOptionPane.showMessageDialog(
                    null,
                    "玩家" + currentPlayer + "被冻结，跳过此回合",
                    "冻结状态",
                    JOptionPane.INFORMATION_MESSAGE
            );

            // 减少冻结回合
            frozenTurns[currentPlayer - 1]--;

            // 再次切换到下一玩家
            currentPlayer = 3 - currentPlayer;

            // 检查再次切换后的玩家是否也被冻结
            if (frozenTurns[currentPlayer - 1] > 0 && currentPlayer != previousPlayer) {
                JOptionPane.showMessageDialog(
                        null,
                        "玩家" + currentPlayer + "也被冻结，跳过此回合",
                        "冻结状态",
                        JOptionPane.INFORMATION_MESSAGE
                );
                frozenTurns[currentPlayer - 1]--;

                // 如果两人都被冻结，回到原始玩家
                if (frozenTurns[0] == 0 && frozenTurns[1] == 0) {
                    currentPlayer = previousPlayer;
                } else {
                    // 继续寻找未被冻结的玩家
                    currentPlayer = 3 - currentPlayer;
                    if (frozenTurns[currentPlayer - 1] > 0) {
                        frozenTurns[currentPlayer - 1]--;
                        currentPlayer = 3 - currentPlayer;
                    }
                }
            }
        }
    }

    private void checkWinConditions() {
        if (checkRingDomination()) {
            gameOver = true;
            return;
        }

        if (playerMoney[2 - currentPlayer] < 0) {
            gameOver = true;
            return;
        }

        if (checkRadialChain()) {
            gameOver = true;
        }
    }

    private boolean checkRingDomination() {
        int[] ownedInRing = new int[4];

        for (RadialLand land : lands) {
            if (land.getOwner() == currentPlayer) {
                ownedInRing[land.getRing()]++;
            }
        }

        return ownedInRing[1] >= 3 || ownedInRing[2] >= 4 || ownedInRing[3] >= 5;
    }

    private boolean checkRadialChain() {
        for (int sector = 0; sector < 12; sector++) {
            boolean hasCommercial = false;
            boolean hasResidential = false;
            boolean hasIndustrial = false;

            for (RadialLand land : lands) {
                if (land.getOwner() == currentPlayer) {
                    if (land.getRing() == 1 && land.getPosition() == sector % 4) {
                        hasCommercial = true;
                    } else if (land.getRing() == 2 && land.getPosition() == sector % 6) {
                        hasResidential = true;
                    } else if (land.getRing() == 3 && land.getPosition() == sector % 8) {
                        hasIndustrial = true;
                    }
                }
            }

            if (hasCommercial && hasResidential && hasIndustrial) {
                return true;
            }
        }

        return false;
    }

    private void showWinMessage() {
        String message = "玩家 " + currentPlayer + " 获胜！\n";
        if (playerMoney[2 - currentPlayer] < 0) {
            message += "对手破产";
        } else if (checkRingDomination()) {
            message += "独占同环足够地块";
        } else {
            message += "形成住商工放射链";
        }
        JOptionPane.showMessageDialog(null, message, "游戏结束", JOptionPane.INFORMATION_MESSAGE);
    }

    public String getGameInfo() {
        if (!gameStarted) return "请选择地块";
        if (gameOver) return "游戏结束 - 玩家 " + currentPlayer + " 获胜";

        String info = "玩家 " + currentPlayer + " 回合 | 资金: " + playerMoney[currentPlayer - 1];
        if (frozenTurns[currentPlayer - 1] > 0) {
            info += " (本回合冻结，无法操作)";
        }
        return info;
    }

    public String getDiceMessage() {
        if (lastDiceResult == 0) return "购买地块后显示骰子结果";

        String message = "玩家" + currentPlayer + "掷骰结果: " + lastDiceResult + "\n";
        if (lastDiceResult == 4) {
            message += "玩家" + (3-currentPlayer) + "下一回合被冷冻";
        } else {
            message += (lastDiceBonus >= 0) ?
                    "玩家" + currentPlayer + "获得 " + lastDiceBonus + " 资金" :
                    "玩家" + currentPlayer + "损失 " + (-lastDiceBonus) + " 资金";
        }
        return message;
    }

    public List<RadialLand> getLands() {
        return lands;
    }

    public RadialLand getSelectedLand() {
        return selectedLand;
    }

    public int getCurrentPlayer() {
        return currentPlayer;
    }

    public int[] getFrozenTurns() {
        return frozenTurns;
    }

    public void startGame() {
        this.gameStarted = true;
    }

    public boolean isCurrentPlayerFrozen() {
        return frozenTurns[currentPlayer - 1] > 0;
    }
}