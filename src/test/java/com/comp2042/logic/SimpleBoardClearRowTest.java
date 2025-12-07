package com.comp2042.logic;

import com.comp2042.logic.events.ClearRow;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SimpleBoardClearRowTest {

    @Test
    public void testClearSingleFullRow() {
        SimpleBoard board = new SimpleBoard(25, 10);

        // 创建一个空棋盘
        int[][] matrix = new int[25][10];

        // 填满最底行（第 24 行）
        for (int col = 0; col < 10; col++) {
            matrix[24][col] = 1;
        }

        board.setBoardMatrix(matrix);

        ClearRow result = board.clearRows();

        assertEquals(1, result.getLinesRemoved(), "应当消除 1 行");

        // 清除后最底行应当为 0
        int[][] newMatrix = result.getNewMatrix();

        for (int col = 0; col < 10; col++) {
            assertEquals(0, newMatrix[24][col], "消行后底部应为空");
        }
    }

    @Test
    public void testClearMultipleRows() {
        SimpleBoard board = new SimpleBoard(25, 10);

        int[][] matrix = new int[25][10];

        // 连续两行满行（23 和 24）
        for (int col = 0; col < 10; col++) {
            matrix[23][col] = 1;
            matrix[24][col] = 1;
        }

        board.setBoardMatrix(matrix);

        ClearRow result = board.clearRows();

        assertEquals(2, result.getLinesRemoved(), "应当消除 2 行");

        // 消除后底部两行应全部是 0
        int[][] newMatrix = result.getNewMatrix();

        for (int col = 0; col < 10; col++) {
            assertEquals(0, newMatrix[24][col], "底部应为空");
            assertEquals(0, newMatrix[23][col], "倒数第二行应为空");
        }
    }
}
