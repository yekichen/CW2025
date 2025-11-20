package com.comp2042;

import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class MatrixOperations {

    private MatrixOperations() {}

    /**
     * 检查 shape 是否与背景矩阵冲突
     * 参数顺序固定为：
     *   matrix[row][col]
     *   shape[row][col]
     *   offsetCol = x
     *   offsetRow = y
     */
    public static boolean intersect(final int[][] matrix, final int[][] brick,
                                    int offsetCol, int offsetRow) {

        for (int r = 0; r < brick.length; r++) {
            for (int c = 0; c < brick[r].length; c++) {

                if (brick[r][c] == 0) continue;

                int targetRow = offsetRow + r;
                int targetCol = offsetCol + c;

                if (isOutOfBounds(matrix, targetCol, targetRow) ||
                        matrix[targetRow][targetCol] != 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isOutOfBounds(int[][] matrix, int col, int row) {

        if (row < 0 || row >= matrix.length) return true;
        if (col < 0 || col >= matrix[row].length) return true;

        return false;
    }

    /** 深拷贝矩阵 */
    public static int[][] copy(int[][] original) {
        int[][] result = new int[original.length][];
        for (int r = 0; r < original.length; r++) {
            result[r] = Arrays.copyOf(original[r], original[r].length);
        }
        return result;
    }

    /** 合并方块进入背景 */
    public static int[][] merge(int[][] matrix, int[][] brick, int offsetCol, int offsetRow) {

        int[][] result = copy(matrix);

        for (int r = 0; r < brick.length; r++) {
            for (int c = 0; c < brick[r].length; c++) {

                if (brick[r][c] == 0) continue;

                int targetRow = offsetRow + r;
                int targetCol = offsetCol + c;

                if (!isOutOfBounds(result, targetCol, targetRow)) {
                    result[targetRow][targetCol] = brick[r][c];
                }
            }
        }
        return result;
    }

    /** 清除已满的行 */
    public static ClearRow checkRemoving(final int[][] matrix) {

        List<int[]> rows = new ArrayList<>();
        int cleared = 0;

        for (int r = 0; r < matrix.length; r++) {
            boolean full = true;
            for (int c = 0; c < matrix[r].length; c++) {
                if (matrix[r][c] == 0) {
                    full = false;
                    break;
                }
            }
            if (full) {
                cleared++;
            } else {
                rows.add(matrix[r]);
            }
        }

        int[][] result = new int[matrix.length][matrix[0].length];

        int idx = matrix.length - 1;

        for (int i = rows.size() - 1; i >= 0; i--) {
            result[idx--] = rows.get(i);
        }

        while (idx >= 0) {
            result[idx--] = new int[matrix[0].length];
        }

        // 俄罗斯方块标准计分
        int bonus = switch (cleared) {
            case 1 -> 100;
            case 2 -> 300;
            case 3 -> 500;
            case 4 -> 800;
            default -> 0;
        };

        return new ClearRow(cleared, result, bonus);
    }
    public static List<int[][]> deepCopyList(List<int[][]> list) {
        return list.stream()
                .map(MatrixOperations::copy)
                .collect(Collectors.toList());
    }

}
