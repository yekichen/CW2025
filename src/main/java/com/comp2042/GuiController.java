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
    private static final int HIDDEN_ROWS = 2; // 顶部隐藏行数

    // 修正定位常量：用于补偿 gameBoard 的边框和内部对齐差异
    // 经验值，可能需要根据实际运行微调
    private static final double UI_ALIGNMENT_OFFSET = 15.0;

    @FXML
    private GridPane gamePanel;

    @FXML
    private Group groupNotification;

    // ⭐️ 修正：旧的 brickPanel 被用于 NEXT 预览，新的用于下落方块 ⭐️
    // 假设 NEXT 预览的 GridPane 叫 nextBlockPanel (与FXML同步)
    @FXML
    private GridPane nextBlockPanel;

    // ⭐️ 新增：用于显示下落方块的容器 ⭐️
    @FXML
    private GridPane fallingBrickPanel;

    @FXML
    private GameOverPanel gameOverPanel;

    @FXML
    private Text scoreText; // 用于分数显示

    private Rectangle[][] displayMatrix;  // 游戏背景方块
    private Rectangle[][] rectangles;     // 当前砖块方块

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

        Reflection reflection = new Reflection();
        reflection.setFraction(0.8);
        reflection.setTopOpacity(0.9);
        reflection.setTopOffset(-12);
    }

    private void handleKeyPress(KeyEvent keyEvent) {
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

    /**
     * 初始化游戏面板和当前砖块显示
     */
    public void initGameView(int[][] boardMatrix, ViewData brick) {
        int visibleRows = boardMatrix.length - HIDDEN_ROWS;
        int cols = boardMatrix[0].length;

        // 仅在第一次初始化时创建 displayMatrix
        if (displayMatrix == null) {
            displayMatrix = new Rectangle[visibleRows][cols];
            for (int i = HIDDEN_ROWS; i < boardMatrix.length; i++) {
                for (int j = 0; j < cols; j++) {
                    Rectangle rectangle = new Rectangle(BRICK_SIZE, BRICK_SIZE);
                    rectangle.setFill(Color.TRANSPARENT);
                    displayMatrix[i - HIDDEN_ROWS][j] = rectangle;
                    gamePanel.add(rectangle, j, i - HIDDEN_ROWS);
                }
            }
        } else {
            refreshGameBackground(boardMatrix);
        }

        // 初始化当前砖块显示
        rectangles = new Rectangle[brick.getBrickData().length][brick.getBrickData()[0].length];
        if (fallingBrickPanel != null) { // ⭐️ 使用新的 fallingBrickPanel ⭐️
            fallingBrickPanel.getChildren().clear();
            fallingBrickPanel.setHgap(gamePanel.getHgap());
            fallingBrickPanel.setVgap(gamePanel.getVgap());
        }

        for (int i = 0; i < brick.getBrickData().length; i++) {
            for (int j = 0; j < brick.getBrickData()[i].length; j++) {
                Rectangle rectangle = new Rectangle(BRICK_SIZE, BRICK_SIZE);
                rectangle.setFill(getFillColor(brick.getBrickData()[i][j]));
                rectangles[i][j] = rectangle;
                if (fallingBrickPanel != null) { // ⭐️ 添加到新的 fallingBrickPanel ⭐️
                    fallingBrickPanel.add(rectangle, j, i);
                }
            }
        }

        // 启动自动下落
        if (timeLine == null) {
            timeLine = new Timeline(new KeyFrame(Duration.millis(400),
                    ae -> moveDown(new MoveEvent(EventType.DOWN, EventSource.THREAD))));
            timeLine.setCycleCount(Timeline.INDEFINITE);
        }
        timeLine.play();
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

    /**
     * 刷新当前砖块位置和颜色
     */
    private void refreshBrick(ViewData brick) {
        if (brick == null || rectangles == null) return;

        if (!isPause.get()) {
            double hGap = gamePanel.getHgap();
            double vGap = gamePanel.getVgap();

            // 计算逻辑上的 X, Y 偏移量
            double xOffset = brick.getxPosition() * (BRICK_SIZE + hGap);
            double yOffset = (brick.getyPosition() - HIDDEN_ROWS) * (BRICK_SIZE + vGap);

            if (fallingBrickPanel != null) { // ⭐️ 对 fallingBrickPanel 进行平移 ⭐️
                // 核心修复：同时对 X 和 Y 轴应用 UI_ALIGNMENT_OFFSET 补偿
                fallingBrickPanel.setTranslateX(xOffset + UI_ALIGNMENT_OFFSET);
                fallingBrickPanel.setTranslateY(yOffset + UI_ALIGNMENT_OFFSET);
            }

            for (int i = 0; i < brick.getBrickData().length; i++) {
                for (int j = 0; j < brick.getBrickData()[i].length; j++) {
                    setRectangleData(brick.getBrickData()[i][j], rectangles[i][j]);
                }
            }
        }
    }

    /**
     * 刷新游戏背景 (固定砖块)
     */
    public void refreshGameBackground(int[][] board) {
        if (displayMatrix == null) return;

        int visibleRows = displayMatrix.length;
        int cols = displayMatrix[0].length;

        for (int i = 0; i < visibleRows; i++) {
            for (int j = 0; j < cols; j++) {
                setRectangleData(board[i + HIDDEN_ROWS][j], displayMatrix[i][j]);
            }
        }
    }

    private void setRectangleData(int color, Rectangle rectangle) {
        rectangle.setFill(getFillColor(color));
        rectangle.setArcHeight(9);
        rectangle.setArcWidth(9);
    }

    private void moveDown(MoveEvent event) {
        if (!isPause.get()) {
            DownData downData = eventListener.onDownEvent(event);
            if (downData.getClearRow() != null && downData.getClearRow().getLinesRemoved() > 0) {
                NotificationPanel notificationPanel = new NotificationPanel("+" + downData.getClearRow().getScoreBonus());
                if (groupNotification != null) {
                    groupNotification.getChildren().add(notificationPanel);
                    notificationPanel.showScore(groupNotification.getChildren());
                }
            }
            refreshBrick(downData.getViewData());
        }
        gamePanel.requestFocus();
    }

    public void setEventListener(InputEventListener eventListener) {
        this.eventListener = eventListener;
    }

    /**
     * 将分数属性绑定到 UI 文本
     */
    public void bindScore(IntegerProperty integerProperty) {
        if (scoreText != null) {
            // 绑定为 7 位数字，以匹配 FXML 中的 "0000000"
            scoreText.textProperty().bind(integerProperty.asString("%07d"));
        }
    }

    public void gameOver() {
        if (timeLine != null) timeLine.stop();
        if (gameOverPanel != null) gameOverPanel.setVisible(true);
        isGameOver.set(true);
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

    public void pauseGame(ActionEvent actionEvent) {
        isPause.set(!isPause.get());
        gamePanel.requestFocus();
    }
}