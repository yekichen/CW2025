package com.comp2042.logic;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SimpleBoardHoldTest {

    @Test
    public void testFirstHoldStoresCurrentBrick() {
        SimpleBoard board = new SimpleBoard(25, 10);
        board.createNewBrick();

        int[][] currentBeforeHold = board.getViewData().getBrickData();
        board.holdBrick();

        int[][] holdShape = board.getHoldShape();

        assertArrayEquals(currentBeforeHold, holdShape,
                "第一次 hold 应该把当前砖块放入 hold 中");
    }

    @Test
    public void testCannotHoldTwiceInSameTurn() {
        SimpleBoard board = new SimpleBoard(25, 10);
        board.createNewBrick();

        // 第一次 hold
        int[][] firstCurrent = board.getViewData().getBrickData();
        board.holdBrick();
        int[][] holdAfterFirst = board.getHoldShape();

        // 第二次 hold（应无效果）
        board.holdBrick();
        int[][] holdAfterSecond = board.getHoldShape();

        assertArrayEquals(holdAfterFirst, holdAfterSecond,
                "同一次下落周期内第二次 hold 不应改变 hold 内容");
    }

    @Test
    public void testHoldResetsAfterSpawnNewBrick() {
        SimpleBoard board = new SimpleBoard(25, 10);
        board.createNewBrick();

        board.holdBrick();  // 使用一次 hold
        int[][] holdShapeBefore = board.getHoldShape();

        board.createNewBrick(); // 生成新砖后 holdUsed 应自动重置

        board.holdBrick();  // 再次 hold —— 这次应该允许

        int[][] holdShapeAfter = board.getHoldShape();

        assertFalse(holdShapeBefore == holdShapeAfter,
                "新砖生成后应该可以再次 hold（第二次 hold 应发生变化）");
    }
}
