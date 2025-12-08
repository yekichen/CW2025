package com.comp2042.logic;

import com.comp2042.logic.bricks.BrickRotator;
import com.comp2042.logic.events.ClearRow;
import com.comp2042.logic.level.LevelManager;
import com.comp2042.logic.bricks.Brick;
import com.comp2042.logic.bricks.BrickGenerator;
import com.comp2042.logic.bricks.RandomBrickGenerator;

import java.awt.*;

public class SimpleBoard implements Board {

    private LevelManager levelManager = new LevelManager();
    private final int width;
    private final int height;
    private final BrickGenerator brickGenerator;
    private final BrickRotator brickRotator;
    private Brick currentBrick;
    private Brick nextBrick;
    private Brick holdBrick = null;
    private boolean holdUsed = false; // 本轮是否已经使用 Hold（防止无限 Hold）



    private int[][] currentGameMatrix;
    private Point currentOffset;
    private final Score score;

    public SimpleBoard(int width, int height) {
        this.width = width;
        this.height = height;
        currentGameMatrix = new int[width][height];
        brickGenerator = new RandomBrickGenerator();
        brickRotator = new BrickRotator();
        score = new Score();
        nextBrick = brickGenerator.getBrick();

    }

    @Override
    public double getFallSpeed() {
        return levelManager.getFallSpeed();
    }

    @Override
    public boolean moveBrickDown() {

        Point next = new Point(currentOffset);
        next.translate(0, 1);

        // 如果下一步会冲突 → 落地
        if (MatrixOperations.intersect(currentGameMatrix,
                brickRotator.getCurrentShape(),
                next.x, next.y)) {

            // 1️⃣ 合并到背景
            currentGameMatrix = MatrixOperations.merge(
                    currentGameMatrix,
                    brickRotator.getCurrentShape(),
                    currentOffset.x,
                    currentOffset.y
            );

            return false;  // 告诉 GameController：我落地了！
        }

        // 没冲突 → 正常下降
        currentOffset = next;
        return true;
    }



    @Override
    public boolean moveBrickLeft() {
        int[][] currentMatrix = MatrixOperations.copy(currentGameMatrix);
        Point p = new Point(currentOffset);
        p.translate(-1, 0);
        boolean conflict = MatrixOperations.intersect(currentMatrix, brickRotator.getCurrentShape(), (int) p.getX(), (int) p.getY());
        if (conflict) {
            return false;
        } else {
            currentOffset = p;
            return true;
        }
    }

    @Override
    public boolean moveBrickRight() {
        int[][] currentMatrix = MatrixOperations.copy(currentGameMatrix);
        Point p = new Point(currentOffset);
        p.translate(1, 0);
        boolean conflict = MatrixOperations.intersect(currentMatrix, brickRotator.getCurrentShape(), (int) p.getX(), (int) p.getY());
        if (conflict) {
            return false;
        } else {
            currentOffset = p;
            return true;
        }
    }

    @Override
    public boolean rotateLeftBrick() {
        int[][] currentMatrix = MatrixOperations.copy(currentGameMatrix);
        NextShapeInfo nextShape = brickRotator.getNextShape();
        boolean conflict = MatrixOperations.intersect(currentMatrix, nextShape.getShape(), (int) currentOffset.getX(), (int) currentOffset.getY());
        if (conflict) {
            return false;
        } else {
            brickRotator.setCurrentShape(nextShape.getPosition());
            return true;
        }
    }

    @Override
    public boolean createNewBrick() {
        // 第一次保险：如果 nextBrick 还没初始化
        if (nextBrick == null) {
            nextBrick = brickGenerator.getBrick();
        }
        //当前方块 = 上一次预览的 next 方块
        currentBrick = nextBrick;
        brickRotator.setBrick(currentBrick);
        currentOffset = new Point(4, -1);
        // 2️⃣ 生成新的 nextBrick（用于预览）
        nextBrick = brickGenerator.getBrick();
        holdUsed = false;   // ⭐⭐ 关键！重置 Hold 使用状态
        return MatrixOperations.intersect(currentGameMatrix, brickRotator.getCurrentShape(), (int) currentOffset.getX(), (int) currentOffset.getY());
    }
    @Override
    public int[][] getNextShape() {
        return nextBrick.getShapeMatrix().get(0);
    }

    @Override
    public int[][] getBoardMatrix() {
        return currentGameMatrix;
    }

    @Override
    public void setBoardMatrix(int[][] matrix) {
        this.currentGameMatrix = MatrixOperations.copy(matrix);
    }

    @Override
    public ViewData getViewData() {
        return new ViewData(brickRotator.getCurrentShape(), (int) currentOffset.getX(), (int) currentOffset.getY(), brickGenerator.getNextBrick().getShapeMatrix().get(0));
    }

    @Override
    public void mergeBrickToBackground() {
        currentGameMatrix = MatrixOperations.merge(currentGameMatrix, brickRotator.getCurrentShape(), (int) currentOffset.getX(), (int) currentOffset.getY());
    }

    @Override
    public ClearRow clearRows() {
        ClearRow clearRow = MatrixOperations.checkRemoving(currentGameMatrix);
        currentGameMatrix = clearRow.getNewMatrix();
        //⭐ 加上等级系统逻辑
        int cleared = clearRow.getLinesRemoved();
        if (cleared > 0) {
            levelManager.addClearedLines(cleared);
        }
           return clearRow;

    }

    @Override
    public Score getScore() {
        return score;
    }


    @Override
    public void newGame() {
        currentGameMatrix = new int[width][height];
        score.reset();
        createNewBrick();
    }
    @Override
    public void hardDrop() {

        while (tryMoveDownWithoutRender()) {

            // 不刷新 GUI，只更新坐标
        }
        score.add(10);
        mergeBrickToBackground();  // 落地直接合并
    }

    private boolean tryMoveDownWithoutRender() {
        int[][] currentMatrix = MatrixOperations.copy(currentGameMatrix);
        Point p = new Point(currentOffset);
        p.translate(0, 1);

        boolean conflict = MatrixOperations.intersect(
                currentMatrix,
                brickRotator.getCurrentShape(),
                p.x,
                p.y
        );

        if (conflict) {
            return false;
        }

        // 更新位置（不触发 UI）
        currentOffset = p;
        return true;
    }
    @Override
    public int[][] getHoldShape() {
        if (holdBrick == null) return new int[4][4];
        return holdBrick.getShapeMatrix().get(0);
    }
    @Override
    public void holdBrick() {

        // 如果本轮已经用过 hold，就不能再用（标准规则）
        if (holdUsed) return;

        Brick temp = holdBrick;   // 可能为 null
        holdBrick = currentBrick; // 当前砖放到 hold

        if (temp == null) {
            // 如果 hold 里之前没有砖，就直接生成新砖
            createNewBrick();
        } else {
            // 有砖 → 把 temp 放出来
            currentBrick = temp;
            brickRotator.setBrick(currentBrick);
            currentOffset = new Point(4, -1);
        }

        holdUsed = true; // 非下降到地面前，不允许再次 Hold
    }
    @Override
    public int getGhostY() {
        int ghostY = currentOffset.y;

        // 模拟向下移动，直到碰撞
        while (!MatrixOperations.intersect(
                currentGameMatrix,
                brickRotator.getCurrentShape(),
                currentOffset.x,
                ghostY + 1)) {

            ghostY++;
        }

        return ghostY;
    }
    @Override
    public LevelManager getLevelManager() {
        return levelManager;
    }




}
