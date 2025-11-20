package com.comp2042;

public class GameController implements InputEventListener {

    private final Board board = new SimpleBoard(10, 23);
    private final GuiController viewGuiController;

    private boolean isPaused = false;

    public GameController(GuiController c) {
        viewGuiController = c;
        board.createNewBrick();
        viewGuiController.setEventListener(this);
        viewGuiController.initGameView(board.getBoardMatrix(), board.getViewData());
        viewGuiController.bindScore(board.getScore().scoreProperty());
    }

    @Override
    public void togglePause() {
        isPaused = !isPaused;
        System.out.println(isPaused ? "Paused" : "Resumed");
    }

    @Override
    public DownData onDownEvent(MoveEvent event) {
        if (isPaused) return null;

        boolean canMove = board.moveBrickDown();
        ClearRow clearRow = null;

        if (!canMove) {
            board.mergeBrickToBackground();
            clearRow = board.clearRows();

            if (clearRow.getLinesRemoved() > 0) {
                board.getScore().add(clearRow.getScoreBonus());
            }

            boolean success = board.createNewBrick();
            if (!success) {
                viewGuiController.gameOver();
            }
        } else {
            if (event.getEventSource() == EventSource.USER) {
                board.getScore().add(1);
            }
        }

        viewGuiController.refreshGameBackground(board.getBoardMatrix());
        return new DownData(clearRow, board.getViewData());
    }

    @Override
    public ViewData onLeftEvent(MoveEvent event) {
        if (!isPaused) board.moveBrickLeft();
        viewGuiController.refreshGameBackground(board.getBoardMatrix());
        return board.getViewData();
    }

    @Override
    public ViewData onRightEvent(MoveEvent event) {
        if (!isPaused) board.moveBrickRight();
        viewGuiController.refreshGameBackground(board.getBoardMatrix());
        return board.getViewData();
    }

    @Override
    public ViewData onRotateEvent(MoveEvent event) {
        if (!isPaused) board.rotateLeftBrick();
        viewGuiController.refreshGameBackground(board.getBoardMatrix());
        return board.getViewData();
    }

    @Override
    public void createNewGame() {
        isPaused = false;
        board.newGame();
        viewGuiController.refreshGameBackground(board.getBoardMatrix());
    }
}
