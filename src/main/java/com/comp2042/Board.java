package com.comp2042;
import com.comp2042.logic.level.LevelManager;


public interface Board {

    boolean moveBrickDown();

    boolean moveBrickLeft();

    boolean moveBrickRight();

    boolean rotateLeftBrick();

    boolean createNewBrick();

    int[][] getBoardMatrix();

    ViewData    getViewData();

    void mergeBrickToBackground();

    ClearRow clearRows();

    Score getScore();

    void newGame();

    void hardDrop();

    // ⭐ 新增 Next Block（下一块方块显示）支持方法
    int[][] getNextShape();
    // ⭐ 新增 Hold 支持
    void holdBrick();
    int[][] getHoldShape();
    int getGhostY();
    // ⭐⭐⭐ 添加这个方法（你现在缺的）
    LevelManager getLevelManager();
}
