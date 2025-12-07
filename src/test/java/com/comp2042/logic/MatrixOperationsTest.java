package com.comp2042.logic;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MatrixOperationsTest {

    @Test
    public void testDetectFullRows() {
        int[][] matrix = new int[5][5];

        // 填满第 2 行
        for (int c = 0; c < 5; c++) {
            matrix[2][c] = 1;
        }

        int[] fullRows = MatrixOperations.detectFullRows(matrix);

        assertArrayEquals(new int[]{2}, fullRows, "应检测到第 2 行被填满");
    }

    @Test
    public void testDetectMultipleFullRows() {
        int[][] matrix = new int[5][5];

        // 填满 1,3 行
        for (int c = 0; c < 5; c++) {
            matrix[1][c] = 1;
            matrix[3][c] = 1;
        }

        int[] fullRows = MatrixOperations.detectFullRows(matrix);

        assertArrayEquals(new int[]{1, 3}, fullRows, "应检测到第 1 和 3 行");
    }

    @Test
    public void testMerge() {
        int[][] board = new int[5][5];

        int[][] brick = {
                {1, 0},
                {1, 1}
        };

        int x = 2;
        int y = 1;

        int[][] merged = MatrixOperations.merge(board, brick, x, y);

        assertEquals(1, merged[1][2], "砖块位置应该合并到矩阵中");
        assertEquals(1, merged[2][2], "砖块位置应该合并到矩阵中");
        assertEquals(1, merged[2][3], "砖块位置应该合并到矩阵中");
    }

    @Test
    public void testIntersect() {
        int[][] board = new int[5][5];

        board[3][2] = 1; // 设置一个障碍

        int[][] brick = {
                {1, 0},
                {1, 1}
        };

        // 放在不会碰撞的位置
        boolean noCollision = MatrixOperations.intersect(board, brick, 1, 1);
        assertFalse(noCollision, "此位置不应发生碰撞");

        // 放在有障碍的位置（会碰撞）
        boolean collision = MatrixOperations.intersect(board, brick, 2, 2);
        assertTrue(collision, "此位置应检测到碰撞");
    }
}
