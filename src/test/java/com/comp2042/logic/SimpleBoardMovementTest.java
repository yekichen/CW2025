package com.comp2042.logic;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SimpleBoardMovementTest {

    @Test
    public void testBrickCanMoveDownAtStart() {
        SimpleBoard board = new SimpleBoard(25, 10);
        board.createNewBrick();

        boolean moved = board.moveBrickDown();

        assertTrue(moved, "初始状态应该可以向下移动");
    }

    @Test
    public void testBrickStopsWhenTouchingBottom() {
        SimpleBoard board = new SimpleBoard(25, 10);
        board.createNewBrick();

        // 强制让砖块靠近底部（24 是最后一行索引）
        // 注意：你的 createNewBrick() 初始 y = -1
        for (int i = 0; i < 30; i++) {
            board.moveBrickDown();
        }

        boolean canMove = board.moveBrickDown();
        assertFalse(canMove, "触底后不应该继续下落");
    }

    @Test
    public void testMoveLeft() {
        SimpleBoard board = new SimpleBoard(25, 10);
        board.createNewBrick();

        int beforeX = board.getViewData().getxPosition();
        board.moveBrickLeft();
        int afterX = board.getViewData().getxPosition();

        assertEquals(beforeX - 1, afterX, "左移后位置应该减少 1");
    }

    @Test
    public void testMoveRight() {
        SimpleBoard board = new SimpleBoard(25, 10);
        board.createNewBrick();

        int beforeX = board.getViewData().getxPosition();
        board.moveBrickRight();
        int afterX = board.getViewData().getxPosition();

        assertEquals(beforeX + 1, afterX, "右移后位置应该增加 1");
    }
}
