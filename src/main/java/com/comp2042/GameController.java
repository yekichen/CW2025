package com.comp2042;

public class GameController implements InputEventListener {

    private Board board = new SimpleBoard(25, 10);
    private final GuiController viewGuiController;

    // ✅ 暂停状态变量
    private boolean isPaused = false;

    public GameController(GuiController c) {
        viewGuiController = c;
        board.createNewBrick();
        viewGuiController.setEventListener(this);
        viewGuiController.initGameView(board.getBoardMatrix(), board.getViewData());
        viewGuiController.bindScore(board.getScore().scoreProperty());
    }

    // ✅ 暂停/恢复控制方法（供 GuiController 调用或调试）
    public void togglePause() {
        isPaused = !isPaused;
        if (isPaused) {
            System.out.println("GameController: Game Paused");
        } else {
            System.out.println("GameController: Game Resumed");
        }
    }

    public boolean isPaused() {
        return isPaused;
    }

    @Override
    public DownData onDownEvent(MoveEvent event) {
        // ✅ 暂停状态下不处理下落
        if (isPaused) return null;

        boolean canMove = board.moveBrickDown();
        ClearRow clearRow = null;

        if (!canMove) {
            board.mergeBrickToBackground();
            clearRow = board.clearRows();

            if (clearRow.getLinesRemoved() > 0) {
                board.getScore().add(clearRow.getScoreBonus());
            }

            // 创建新方块；如已到顶部则游戏结束
            if (board.createNewBrick()) {
                viewGuiController.gameOver();
            }

            viewGuiController.refreshGameBackground(board.getBoardMatrix());
        } else {
            if (event.getEventSource() == EventSource.USER) {
                board.getScore().add(1);
            }
        }

        return new DownData(clearRow, board.getViewData());
    }

    @Override
    public ViewData onLeftEvent(MoveEvent event) {
        if (isPaused) return board.getViewData();
        board.moveBrickLeft();
        return board.getViewData();
    }

    @Override
    public ViewData onRightEvent(MoveEvent event) {
        if (isPaused) return board.getViewData();
        board.moveBrickRight();
        return board.getViewData();
    }

    @Override
    public ViewData onRotateEvent(MoveEvent event) {
        if (isPaused) return board.getViewData();
        board.rotateLeftBrick();
        return board.getViewData();
    }

    @Override
    public void createNewGame() {
        // ✅ 重新开始时自动清除暂停状态
        isPaused = false;
        board.newGame();
        viewGuiController.refreshGameBackground(board.getBoardMatrix());
    }
}
