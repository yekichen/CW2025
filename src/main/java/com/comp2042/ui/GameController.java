package com.comp2042.ui;

import com.comp2042.logic.Board;
import com.comp2042.logic.MatrixOperations;
import com.comp2042.logic.SimpleBoard;
import com.comp2042.logic.ViewData;
import com.comp2042.logic.events.*;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

public class GameController implements InputEventListener {

    private Board board = new SimpleBoard(25, 10);

    private final GuiController viewGuiController;

    private boolean isPaused = false;

    private int combo = 0;
    private int totalLinesCleared = 0;

    // ⭐⭐ 就把这个加在这里 ⭐⭐
    public Board getBoard() {
        return board;
    }


    public GameController(GuiController c) {
        viewGuiController = c;
        board.createNewBrick();
        viewGuiController.setEventListener(this);
        viewGuiController.initGameView(board.getBoardMatrix(), board.getViewData());
        viewGuiController.bindScore(board.getScore().scoreProperty());
        viewGuiController.refreshNext(board.getNextShape());
    }

    // --------------------------------------------------------
    //    NORMAL DOWN EVENT (THREAD / USER INPUT)
    // --------------------------------------------------------
    @Override
    public DownData onDownEvent(MoveEvent event) {
        viewGuiController.timeLine.setRate(board.getFallSpeed());


        boolean canMove = board.moveBrickDown();

        // -------------------------
        //    CAN MOVE DOWN
        // -------------------------
        if (canMove) {

            // MUST refresh brick BEFORE returning (smooth falling)
            ViewData vd = board.getViewData();
            viewGuiController.refreshBrick(vd);
            viewGuiController.refreshGhost(vd, board.getGhostY());

            if (event != null && event.getEventSource() == EventSource.USER) {
                board.getScore().add(1);
            }

            return new DownData(null, vd);
        }

        // -------------------------
        //    CANNOT MOVE → MERGE
        // -------------------------
        board.mergeBrickToBackground();

        // detect but DO NOT clear
        int[] fullRows = MatrixOperations.detectFullRows(board.getBoardMatrix());

        // -------------------------
        //    HAS CLEAR ANIMATION
        // -------------------------
        if (fullRows.length > 0) {

            int removedCount = fullRows.length;

            playClearAnimation(fullRows, () -> {

                // Final clear
                ClearRow finalClear = board.clearRows();
                viewGuiController.refreshGameBackground(board.getBoardMatrix());

                // Score
                int base = removedCount * 200;
                combo++;
                int comboBonus = combo * combo * 50;
                board.getScore().add(base + comboBonus);
                viewGuiController.updateCombo(combo);
                viewGuiController.showFloatingScore(base + comboBonus);
                // ⭐ 统计并更新总消行数（onDownEvent）⭐
                totalLinesCleared += removedCount;
                viewGuiController.updateLinesCleared(totalLinesCleared);
                // ⭐⭐ 这里刷新 Level
                viewGuiController.updateLevel(board.getLevelManager().getLevel());



                // Level

                            // ⭐ CRITICAL

                // New brick
                if (board.createNewBrick()) {
                    viewGuiController.gameOver();
                } else {
                    viewGuiController.refreshNext(board.getNextShape());
                }

                // Draw new brick IMMEDIATELY
                ViewData vd = board.getViewData();
                viewGuiController.refreshBrick(vd);
                viewGuiController.refreshGhost(vd, board.getGhostY());
            });

            return null; // animation → pause logic
        }

        // -------------------------
        //    NO CLEAR
        // -------------------------
        combo = 0;
        viewGuiController.updateCombo(combo);

        if (board.createNewBrick()) {
            viewGuiController.gameOver();
        } else {
            viewGuiController.refreshNext(board.getNextShape());
        }

        viewGuiController.refreshGameBackground(board.getBoardMatrix());
        ViewData vd = board.getViewData();
        viewGuiController.refreshBrick(vd);
        viewGuiController.refreshGhost(vd, board.getGhostY());

        return new DownData(null, vd);
    }

    // --------------------------------------------------------
    //    MOVE LEFT / RIGHT / ROTATE
    // --------------------------------------------------------
    @Override
    public ViewData onLeftEvent(MoveEvent event) {
        board.moveBrickLeft();
        viewGuiController.refreshGhost(board.getViewData(), board.getGhostY());
        return board.getViewData();
    }

    @Override
    public ViewData onRightEvent(MoveEvent event) {
        board.moveBrickRight();
        viewGuiController.refreshGhost(board.getViewData(), board.getGhostY());
        return board.getViewData();
    }

    @Override
    public ViewData onRotateEvent(MoveEvent event) {
        board.rotateLeftBrick();
        viewGuiController.refreshGhost(board.getViewData(), board.getGhostY());
        return board.getViewData();
    }

    // --------------------------------------------------------
    //    NEW GAME
    // --------------------------------------------------------
    @Override
    public void createNewGame() {
        board.newGame();
        viewGuiController.refreshGameBackground(board.getBoardMatrix());
        viewGuiController.refreshNext(board.getNextShape());
        viewGuiController.refreshGhost(board.getViewData(), board.getGhostY());
    }


    // --------------------------------------------------------
    //    HARD DROP (必须与 normal 保持一致)
    // --------------------------------------------------------
    @Override
    public DownData onHardDrop() {
        viewGuiController.timeLine.setRate(board.getFallSpeed());


        if (isPaused) return null;

        // 1. DROP IMMEDIATELY
        board.hardDrop();

        // 2. MERGE
        board.mergeBrickToBackground();

        // 3. DETECT FULL ROWS
        int[] fullRows = MatrixOperations.detectFullRows(board.getBoardMatrix());

        // -------------------------
        //    WITH ANIMATION
        // -------------------------
        if (fullRows.length > 0) {

            int removedCount = fullRows.length;

            playClearAnimation(fullRows, () -> {

                ClearRow finalClear = board.clearRows();
                viewGuiController.refreshGameBackground(board.getBoardMatrix());

                int base = removedCount * 200;
                combo++;
                int comboBonus = combo * combo * 50;
                board.getScore().add(base + comboBonus);
                viewGuiController.updateCombo(combo);
                viewGuiController.showFloatingScore(base + comboBonus);
                // ⭐ 统计并更新总消行数（onDownEvent）⭐
                totalLinesCleared += removedCount;
                viewGuiController.updateLinesCleared(totalLinesCleared);

                // ⭐⭐ 这里刷新 Level
                viewGuiController.updateLevel(board.getLevelManager().getLevel());


                // ⭐ CRITICAL

                if (board.createNewBrick()) {
                    viewGuiController.gameOver();
                } else {
                    viewGuiController.refreshNext(board.getNextShape());
                }

                ViewData vd = board.getViewData();
                viewGuiController.refreshBrick(vd);
                viewGuiController.refreshGhost(vd, board.getGhostY());
            });

            return null;
        }

        // -------------------------
        //    NO CLEAR
        // -------------------------
        combo = 0;
        viewGuiController.updateCombo(combo);

        if (board.createNewBrick()) {
            viewGuiController.gameOver();
            return null;
        }

        viewGuiController.refreshNext(board.getNextShape());
        viewGuiController.refreshGameBackground(board.getBoardMatrix());

        ViewData vd = board.getViewData();
        viewGuiController.refreshBrick(vd);
        viewGuiController.refreshGhost(vd, board.getGhostY());

             // ⭐ MUST — keep speed consistent

        return null;
    }

    // --------------------------------------------------------
    //    HOLD
    // --------------------------------------------------------
    @Override
    public void onHold() {
        board.holdBrick();
        viewGuiController.refreshBrick(board.getViewData());
        viewGuiController.refreshHold(board.getHoldShape());
        viewGuiController.refreshGhost(board.getViewData(), board.getGhostY());
    }


    // --------------------------------------------------------
    //    UPDATE TIMELINE SPEED
    // --------------------------------------------------------


    // --------------------------------------------------------
    //    PLAY CLEAR ANIMATION
    // --------------------------------------------------------
    private void playClearAnimation(int[] rows, Runnable after) {

        viewGuiController.timeLine.pause();

        Timeline anim = new Timeline(
                new KeyFrame(Duration.millis(80), e -> viewGuiController.flashRows(rows, true)),
                new KeyFrame(Duration.millis(160), e -> viewGuiController.flashRows(rows, false))
        );

        anim.setCycleCount(2);

        anim.setOnFinished(e -> {

            after.run();   // 清行、更新 UI、创建新砖

            // ⭐⭐ 动画完毕恢复自动下落 ⭐⭐
            viewGuiController.timeLine.play();
        });


        anim.play();
    }

}
