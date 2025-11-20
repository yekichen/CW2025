package com.comp2042;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.effect.Reflection;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

public class GuiController implements Initializable {

    private static final int BRICK_SIZE = 20;
    private static final int HIDDEN_ROWS = 0;
    private static final double UI_ALIGNMENT_OFFSET = 15.0;

    @FXML
    private GridPane gamePanel;

    @FXML
    private Group groupNotification;

    @FXML
    private GridPane nextBlockPanel;

    @FXML
    private GridPane fallingBrickPanel;

    @FXML
    private GameOverPanel gameOverPanel;

    @FXML
    private Text scoreText;

    @FXML
    private Button pauseButton;   // ⭐ 你要求添加的按钮

    private Rectangle[][] displayMatrix;
    private Rectangle[][] rectangles;

    private InputEventListener eventListener;
    private Timeline timeLine;

    private final BooleanProperty isPause = new SimpleBooleanProperty(false);
    private final BooleanProperty isGameOver = new SimpleBooleanProperty(false);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Font.loadFont(getClass().getClassLoader().getResource("digital.ttf").toExternalForm(), 38);

        if (gamePanel != null) {
            gamePanel.setFocusTraversable(true);
            gamePanel.requestFocus();
            gamePanel.setOnKeyPressed(this::handleKeyPress);
        }

        if (gameOverPanel != null) {
            gameOverPanel.setVisible(false);
        }

        if (pauseButton != null) {
            pauseButton.setFocusTraversable(false);
        }

        Reflection reflection = new Reflection();
        reflection.setFraction(0.8);
        reflection.setTopOpacity(0.9);
        reflection.setTopOffset(-12);
    }

    public void setEventListener(InputEventListener eventListener) {
        this.eventListener = eventListener;
    }

    // ============================================================
    // 初始化游戏显示
    // ============================================================
    public void initGameView(int[][] boardMatrix, ViewData brick) {
        int visibleRows = boardMatrix.length;
        int cols = boardMatrix[0].length;

        if (displayMatrix == null) {
            displayMatrix = new Rectangle[visibleRows][cols];
            for (int i = 0; i < visibleRows; i++) {
                for (int j = 0; j < cols; j++) {
                    Rectangle r = new Rectangle(BRICK_SIZE, BRICK_SIZE);
                    r.setFill(Color.TRANSPARENT);
                    displayMatrix[i][j] = r;
                    gamePanel.add(r, j, i);
                }
            }
        } else {
            refreshGameBackground(boardMatrix);
        }

        // 移除旧的 falling brick
        if (fallingBrickPanel != null) {
            fallingBrickPanel.getChildren().clear();
        }

        // 初始化当前砖块
        rectangles = new Rectangle[brick.getBrickData().length][brick.getBrickData()[0].length];
        if (fallingBrickPanel != null) {
            fallingBrickPanel.getChildren().clear();
            fallingBrickPanel.setHgap(gamePanel.getHgap());
            fallingBrickPanel.setVgap(gamePanel.getVgap());
        }

        for (int i = 0; i < brick.getBrickData().length; i++) {
            for (int j = 0; j < brick.getBrickData()[i].length; j++) {
                Rectangle rect = new Rectangle(BRICK_SIZE, BRICK_SIZE);
                rect.setFill(getFillColor(brick.getBrickData()[i][j]));
                rectangles[i][j] = rect;
                fallingBrickPanel.add(rect, j, i);
            }
        }

        // 自动下落
        if (timeLine == null) {
            timeLine = new Timeline(new KeyFrame(Duration.millis(400),
                    ae -> moveDown(new MoveEvent(EventType.DOWN, EventSource.THREAD))));
            timeLine.setCycleCount(Timeline.INDEFINITE);
        }
        timeLine.play();
    }

    // ============================================================
    // 暂停按钮
    // ============================================================
    public void pauseGame(ActionEvent actionEvent) {

        eventListener.togglePause();

        isPause.set(!isPause.get());

        if (pauseButton != null) {
            pauseButton.setText(isPause.get() ? "Resume" : "Pause");
        }

        if (timeLine != null) {
            if (isPause.get()) {
                timeLine.pause();
            } else {
                timeLine.play();
            }
        }

        gamePanel.requestFocus();
    }

    // ============================================================
    // 键盘控制
    // ============================================================
    private void handleKeyPress(KeyEvent keyEvent) {

        // ⭐ 空格键暂停/恢复
        if (keyEvent.getCode() == KeyCode.SPACE) {
            pauseGame(null);
            keyEvent.consume();
            return;
        }

        if (!isPause.get() && !isGameOver.get()) {

            if (keyEvent.getCode() == KeyCode.LEFT || keyEvent.getCode() == KeyCode.A) {
                refreshBrick(eventListener.onLeftEvent(new MoveEvent(EventType.LEFT, EventSource.USER)));
                keyEvent.consume();
            }

            if (keyEvent.getCode() == KeyCode.RIGHT || keyEvent.getCode() == KeyCode.D) {
                refreshBrick(eventListener.onRightEvent(new MoveEvent(EventType.RIGHT, EventSource.USER)));
                keyEvent.consume();
            }

            if (keyEvent.getCode() == KeyCode.UP || keyEvent.getCode() == KeyCode.W) {
                refreshBrick(eventListener.onRotateEvent(new MoveEvent(EventType.ROTATE, EventSource.USER)));
                keyEvent.consume();
            }

            if (keyEvent.getCode() == KeyCode.DOWN || keyEvent.getCode() == KeyCode.S) {
                moveDown(new MoveEvent(EventType.DOWN, EventSource.USER));
                keyEvent.consume();
            }
        }

        if (keyEvent.getCode() == KeyCode.N) {
            newGame(null);
        }
    }


    // ============================================================
    // 下落处理
    // ============================================================
    private void moveDown(MoveEvent event) {
        if (!isPause.get()) {
            DownData data = eventListener.onDownEvent(event);

            if (data.getClearRow() != null && data.getClearRow().getLinesRemoved() > 0) {
                NotificationPanel np = new NotificationPanel("+" + data.getClearRow().getScoreBonus());
                groupNotification.getChildren().add(np);
                np.showScore(groupNotification.getChildren());
            }

            refreshBrick(data.getViewData());
        }

        gamePanel.requestFocus();
    }

    // ============================================================
    // 刷新背景
    // ============================================================
    public void refreshGameBackground(int[][] board) {
        if (displayMatrix == null) return;

        int rows = displayMatrix.length;
        int cols = displayMatrix[0].length;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                setRectangleData(board[i][j], displayMatrix[i][j]);
            }
        }
    }

    // ============================================================
    // 刷新当前砖块显示
    // ============================================================
    private void refreshBrick(ViewData brick) {
        if (brick == null || rectangles == null) return;

        if (!isPause.get()) {

            double hGap = gamePanel.getHgap();
            double vGap = gamePanel.getVgap();

            double xOffset = brick.getxPosition() * (BRICK_SIZE + hGap);
            double yOffset = (brick.getyPosition() - HIDDEN_ROWS) * (BRICK_SIZE + vGap);

            fallingBrickPanel.setTranslateX(xOffset + UI_ALIGNMENT_OFFSET);
            fallingBrickPanel.setTranslateY(yOffset + UI_ALIGNMENT_OFFSET);

            for (int i = 0; i < brick.getBrickData().length; i++) {
                for (int j = 0; j < brick.getBrickData()[i].length; j++) {
                    setRectangleData(brick.getBrickData()[i][j], rectangles[i][j]);
                }
            }
        }
    }

    private void setRectangleData(int color, Rectangle rect) {
        rect.setFill(getFillColor(color));
        rect.setArcHeight(9);
        rect.setArcWidth(9);
    }

    private Paint getFillColor(int i) {
        return switch (i) {
            case 0 -> Color.TRANSPARENT;
            case 1 -> Color.AQUA;
            case 2 -> Color.BLUEVIOLET;
            case 3 -> Color.DARKGREEN;
            case 4 -> Color.YELLOW;
            case 5 -> Color.RED;
            case 6 -> Color.BEIGE;
            case 7 -> Color.BURLYWOOD;
            default -> Color.WHITE;
        };
    }

    // ============================================================
    // 游戏结束 & 新游戏
    // ============================================================
    public void gameOver() {
        if (timeLine != null) timeLine.stop();
        if (gameOverPanel != null) gameOverPanel.setVisible(true);
        isGameOver.set(true);

        // 避免残影
        if (fallingBrickPanel != null) {
            fallingBrickPanel.getChildren().clear();
        }
    }

    public void newGame(ActionEvent actionEvent) {
        if (timeLine != null) timeLine.stop();
        if (gameOverPanel != null) gameOverPanel.setVisible(false);

        eventListener.createNewGame();
        gamePanel.requestFocus();

        if (timeLine != null) timeLine.play();
        isPause.set(false);
        isGameOver.set(false);
    }
    /*绑定分数*/
    public void bindScore(IntegerProperty integerProperty) {
        if (scoreText != null) {
            scoreText.textProperty().bind(integerProperty.asString("%07d"));
        }
    }
    public void postLoadSetup() {
    }

    public GridPane getGamePanel() {
        return gamePanel;
    }

}
