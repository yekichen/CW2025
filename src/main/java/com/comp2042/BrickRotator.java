package com.comp2042;

import com.comp2042.logic.bricks.Brick;

public class BrickRotator {

    private Brick brick;
    private int currentShape = 0;

    public NextShapeInfo getNextShape() {
        int nextShape = currentShape;
        nextShape = (++nextShape) % brick.getShapeMatrix().size();
        return new NextShapeInfo(brick.getShapeMatrix().get(nextShape), nextShape);
    }

    public int[][] getCurrentShape() {
        // ⭐️ 修正：如果 brick 为空 (Game Over 状态)，则返回一个空的 0x0 矩阵 ⭐️
        if (brick == null) {
            return new int[0][0];
        }
        return brick.getShapeMatrix().get(currentShape);
    }

    public void setCurrentShape(int currentShape) {
        this.currentShape = currentShape;
    }

    public void setBrick(Brick brick) {
        this.brick = brick;
        currentShape = 0;
    }

    /**
     * ⭐️ 新增方法：清除当前方块数据（用于 Game Over 时） ⭐️
     */
    public void clearBrick() {
        this.brick = null;
    }

}