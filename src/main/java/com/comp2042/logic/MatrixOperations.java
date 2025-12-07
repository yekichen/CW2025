package com.comp2042.logic;

import com.comp2042.logic.events.ClearRow;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;

public class MatrixOperations {


    //We don't want to instantiate this utility class
    private MatrixOperations(){

    }

    public static boolean intersect(final int[][] matrix, final int[][] brick, int x, int y) {
        for (int row = 0; row < brick.length; row++) {
            for (int col = 0; col < brick[row].length; col++) {

                if (brick[row][col] != 0) {

                    int targetRow = y + row;
                    int targetCol = x + col;

                    // 顶部区域允许越界
                    if (targetRow < 0) continue;

                    // 边界检查
                    if (targetCol < 0 || targetCol >= matrix[0].length ||
                            targetRow >= matrix.length) {
                        return true;
                    }

                    // 冲突检查
                    if (matrix[targetRow][targetCol] != 0) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


    private static boolean checkOutOfBound(int[][] matrix, int targetX, int targetY) {
        boolean returnValue = true;
        if (targetX >= 0 && targetY < matrix.length && targetX < matrix[targetY].length) {
            returnValue = false;
        }
        return returnValue;
    }

    public static int[][] copy(int[][] original) {
        int[][] myInt = new int[original.length][];
        for (int i = 0; i < original.length; i++) {
            int[] aMatrix = original[i];
            int aLength = aMatrix.length;
            myInt[i] = new int[aLength];
            System.arraycopy(aMatrix, 0, myInt[i], 0, aLength);
        }
        return myInt;
    }

    public static int[][] merge(int[][] filledFields, int[][] brick, int x, int y) {
        int[][] copy = copy(filledFields);

        for (int row = 0; row < brick.length; row++) {
            for (int col = 0; col < brick[row].length; col++) {

                if (brick[row][col] != 0) {

                    int targetRow = y + row;
                    int targetCol = x + col;

                    // 顶部区域忽略
                    if (targetRow < 0) continue;

                    copy[targetRow][targetCol] = brick[row][col];
                }
            }
        }

        return copy;
    }


    public static ClearRow checkRemoving(final int[][] matrix) {

        int[][] tmp = new int[matrix.length][matrix[0].length];
        Deque<int[]> newRows = new ArrayDeque<>();
        List<Integer> clearedRows = new ArrayList<>();

        for (int i = 0; i < matrix.length; i++) {

            int[] tmpRow = new int[matrix[i].length];
            boolean rowToClear = true;

            for (int j = 0; j < matrix[0].length; j++) {
                if (matrix[i][j] == 0) {
                    rowToClear = false;
                }
                tmpRow[j] = matrix[i][j];
            }

            if (rowToClear) {
                clearedRows.add(i);      // ⭐ 记录被清除的行
            } else {
                newRows.add(tmpRow);
            }
        }

        // ⭐ 下移剩余行
        for (int i = matrix.length - 1; i >= 0; i--) {
            int[] row = newRows.pollLast();
            if (row != null) {
                tmp[i] = row;
            } else {
                break;
            }
        }

        int removedCount = clearedRows.size();
        int scoreBonus = 50 * removedCount * removedCount;

        // ⭐ 将 List<Integer> 转为 int[]
        int[] rowsArray = clearedRows.stream().mapToInt(Integer::intValue).toArray();

        // ⭐ 使用新的构造函数（含 rows）
        return new ClearRow(removedCount, tmp, scoreBonus, rowsArray);
    }


    public static List<int[][]> deepCopyList(List<int[][]> list){
        return list.stream().map(MatrixOperations::copy).collect(Collectors.toList());
    }

    public static int[] detectFullRows(final int[][] matrix) {
        List<Integer> rows = new ArrayList<>();
        for (int i = 0; i < matrix.length; i++) {
            boolean full = true;
            for (int j = 0; j < matrix[i].length; j++) {
                if (matrix[i][j] == 0) {
                    full = false;
                    break;
                }
            }
            if (full) rows.add(i);
        }
        return rows.stream().mapToInt(i -> i).toArray();
    }


}
