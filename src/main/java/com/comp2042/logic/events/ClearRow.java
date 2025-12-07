package com.comp2042.logic.events;

import com.comp2042.logic.MatrixOperations;

public final class ClearRow {

    private final int linesRemoved;
    private final int[] rows;
    private final int[][] newMatrix;
    private final int scoreBonus;

    public ClearRow(int linesRemoved, int[][] newMatrix, int scoreBonus,int[] rows) {
        this.linesRemoved = linesRemoved;
        this.newMatrix = newMatrix;
        this.scoreBonus = scoreBonus;
        this.rows = rows;
    }

    public int getLinesRemoved() {
        return linesRemoved;
    }

    public int[][] getNewMatrix() {
        return MatrixOperations.copy(newMatrix);
    }

    public int getScoreBonus() {
        return scoreBonus;
    }
    public int[] getRows() {
        return rows;
    }
}
