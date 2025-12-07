package com.comp2042.logic.level;

public class LevelManager {

    private int level = 1;         // 当前等级
    private int linesCleared = 0;  // 累积消除行数

    public int getLevel() {
        return level;
    }

    /**
     * 增加清除行数，并根据规则升级
     */
    public void addClearedLines(int count) {
        linesCleared += count;

        // 每 10 行升级
        while (linesCleared >= 10) {
            level++;
            linesCleared -= 10;
        }
    }
    /*public void addClearedLines(int count) {
        level += count;   // 每清一行升一级（仅用于测试）
    }*/


    /**
     * 根据当前等级返回方块下落速度（毫秒）
     */
    public int getCurrentSpeed() {
        int speed = 800 - (level - 1) * 50;
        return Math.max(speed, 200);   // 不低于 200ms
    }
    // 根据等级返回 Timeline 的速率（越大越快）
    public double getFallSpeed() {
        return 1.0 + (level - 1) * 0.2; // 等级越高，下落越快
    }

}
