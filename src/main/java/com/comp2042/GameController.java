

package com.comp2042;

import javafx.animation.KeyFrame;
import javafx.util.Duration;

public class GameController implements InputEventListener {

    private Board board = new SimpleBoard(25, 10);

    private final GuiController viewGuiController;

    private boolean isPaused = false;

    private int combo = 0;


    public GameController(GuiController c) {
        viewGuiController = c;
        board.createNewBrick();
        viewGuiController.setEventListener(this);
        viewGuiController.initGameView(board.getBoardMatrix(), board.getViewData());
        viewGuiController.bindScore(board.getScore().scoreProperty());
        // ⭐ 加上 next 初始化
        viewGuiController.refreshNext(board.getNextShape());
    }

    @Override
    public DownData onDownEvent(MoveEvent event) {

        boolean canMove = board.moveBrickDown();
        ClearRow clearRow = null;

        if (!canMove) {

            // 1. 落地合并
            board.mergeBrickToBackground();

            // 2. 检查是否消行
            clearRow = board.clearRows();

            if (clearRow.getLinesRemoved() > 0) {

                int lines = clearRow.getLinesRemoved();

                // ⭐ 固定行消除加分：每行 200
                int baseScore = lines * 200;

                // ⭐ combo 连击：combo² × 50
                combo++;
                int comboBonus = combo * combo * 50;

                // ⭐ 给分
                board.getScore().add(baseScore + comboBonus);

                // ⭐ 更新 UI 显示 Combo
                viewGuiController.updateCombo(combo);

                // ⭐⭐⭐（你要加的 Level 系统）⭐⭐⭐
                board.getLevelManager().addClearedLines(lines);

                // ⭐⭐⭐（你要加的速度更新）⭐⭐⭐
                updateTimelineSpeed();

            } else {

                // ⭐ 没有消行 → combo 清零
                combo = 0;

                // UI 也要清空 combo 显示
                viewGuiController.updateCombo(combo);
            }

            // 3. 生成下一块砖
            if (board.createNewBrick()) {
                viewGuiController.gameOver();
            }else {
                viewGuiController.refreshNext(board.getNextShape());
            }

            // 4. 刷新背景
            viewGuiController.refreshGameBackground(board.getBoardMatrix());

        } else {

            // ⭐ 普通下落（按键触发）加分
            if (event.getEventSource() == EventSource.USER) {
                board.getScore().add(1);
            }
        }
        viewGuiController.refreshGhost(board.getViewData(), board.getGhostY());

        return new DownData(clearRow, board.getViewData());
    }


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


    @Override
    public void createNewGame() {
        board.newGame();
        viewGuiController.refreshGameBackground(board.getBoardMatrix());
        // ⭐ next 更新
        viewGuiController.refreshNext(board.getNextShape());

        viewGuiController.refreshGhost(board.getViewData(), board.getGhostY());

    }

    @Override
    public void onHardDrop() {

        if (isPaused) return;

        // ⭐ 立即下降
        board.hardDrop();

        // ⭐ 落地 & 消行
        board.mergeBrickToBackground();
        ClearRow clearRow = board.clearRows();

        if (clearRow.getLinesRemoved() > 0) {

            int lines = clearRow.getLinesRemoved();
            int baseScore = lines * 200;

            // ⭐ combo + 处理
            combo++;
            int comboBonus = combo * combo * 50;

            board.getScore().add(baseScore + comboBonus);

            // ⭐ 显示 Combo
            viewGuiController.updateCombo(combo);

            // ⭐⭐⭐ 这里补上 Level 逻辑 ⭐⭐⭐
            board.getLevelManager().addClearedLines(lines);  // 👈 升级
            updateTimelineSpeed();                           // 👈 更新速度 & LevelLabel


            // ⭐ 显示 “+分数” 动画（和 soft drop 一样）
            NotificationPanel np = new NotificationPanel("+" + (baseScore + comboBonus));
            viewGuiController.groupNotification.getChildren().add(np);
            np.showScore(viewGuiController.groupNotification.getChildren());
        }
        else {
            combo = 0;
            viewGuiController.updateCombo(combo);
        }

        // ⭐ 新砖
        if (board.createNewBrick()) {
            viewGuiController.gameOver();
            return;
        }
        // ⭐ next 更新
        viewGuiController.refreshNext(board.getNextShape());
        viewGuiController.refreshGameBackground(board.getBoardMatrix());
        viewGuiController.refreshBrick(board.getViewData());
        viewGuiController.refreshGhost(board.getViewData(), board.getGhostY());

    }
    //hold 存储功能
    @Override
    public void onHold() {
        board.holdBrick();

        // 落地前交换不刷新背景，只刷新当前砖
        viewGuiController.refreshBrick(board.getViewData());
        viewGuiController.refreshHold(board.getHoldShape());
        viewGuiController.refreshGhost(board.getViewData(), board.getGhostY());  // ⭐ ADD THIS LINE
    }
    //timeline

    private void updateTimelineSpeed() {
        System.out.println("LEVEL NOW = " + board.getLevelManager().getLevel());
        // 1️⃣ 获取当前速度
        int speed = board.getLevelManager().getCurrentSpeed();

        // 2️⃣ 更新 Timeline（注意：timeline 在 GuiController）
        viewGuiController.timeLine.stop();
        viewGuiController.timeLine.getKeyFrames().set(
                0,
                new KeyFrame(Duration.millis(speed),
                        e -> onDownEvent(new MoveEvent(EventType.DOWN, EventSource.THREAD)))
        );
        viewGuiController.timeLine.play();

        // 3️⃣ 更新 Level 显示
        viewGuiController.updateLevel(board.getLevelManager().getLevel());
    }





}
