package com.comp2042;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.effect.Reflection;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

import javafx.scene.control.Label;
import javafx.beans.property.IntegerProperty;


import java.net.URL;
import java.util.ResourceBundle;

public class GuiController implements Initializable {

    private static final int BRICK_SIZE = 20;

    @FXML
    private Label scoreLabel;

    @FXML
    private GridPane gamePanel;

    @FXML
    private Group groupNotification;

    @FXML
    private GridPane brickPanel;

    @FXML
    private GameOverPanel gameOverPanel;

    // 新增：暂停图层
    @FXML
    private StackPane pauseLayer;

    @FXML
    private Text pauseText;

    private Rectangle[][] displayMatrix;

    private InputEventListener eventListener;

    private Rectangle[][] rectangles;

    public Timeline timeLine;

    private final BooleanProperty isPause = new SimpleBooleanProperty(false);
    private final BooleanProperty isGameOver = new SimpleBooleanProperty(false);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Font.loadFont(getClass().getClassLoader().getResource("digital.ttf").toExternalForm(), 38);

        gamePanel.setFocusTraversable(true);
        gamePanel.requestFocus();
        gamePanel.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {

                // 1️⃣ P = 暂停/继续
                if (keyEvent.getCode() == KeyCode.P) {
                    togglePause();
                    keyEvent.consume();
                    return;
                }

                // 2️⃣ 暂停 or Game Over → 不接受输入
                if (isPause.get() || isGameOver.get()) {
                    return;
                }

                // 3️⃣ 正常输入
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

                // N 键：重新开始
                if (keyEvent.getCode() == KeyCode.N) {
                    newGame(null);
                }
                if (keyEvent.getCode() == KeyCode.SPACE) {
                    eventListener.onHardDrop();
                }


            }
        });

        // GameOver 隐藏
        gameOverPanel.setVisible(false);

        // 暂停层隐藏
        if (pauseLayer != null) {
            pauseLayer.setVisible(false);
            pauseLayer.toFront();
            pauseLayer.visibleProperty().bind(isPause);
        }

        Reflection reflection = new Reflection();
        reflection.setFraction(0.8);
        reflection.setTopOpacity(0.9);
        reflection.setTopOffset(-12);
    }

    // 暂停 / 恢复
    private void togglePause() {
        if (isGameOver.get()) return;

        boolean nowPaused = !isPause.get();
        isPause.set(nowPaused);

        if (nowPaused) {
            timeLine.pause();
            if (pauseText != null) pauseText.setVisible(true);
        } else {
            timeLine.play();
            if (pauseText != null) pauseText.setVisible(false);
        }
    }

    public void initGameView(int[][] boardMatrix, ViewData brick) {
        displayMatrix = new Rectangle[boardMatrix.length][boardMatrix[0].length];
        for (int i = 2; i < boardMatrix.length; i++) {
            for (int j = 0; j < boardMatrix[i].length; j++) {
                Rectangle rectangle = new Rectangle(BRICK_SIZE, BRICK_SIZE);
                rectangle.setFill(Color.TRANSPARENT);
                displayMatrix[i][j] = rectangle;
                gamePanel.add(rectangle, j, i - 2);
            }
        }

        rectangles = new Rectangle[brick.getBrickData().length][brick.getBrickData()[0].length];
        for (int i = 0; i < brick.getBrickData().length; i++) {
            for (int j = 0; j < brick.getBrickData()[i].length; j++) {
                Rectangle rectangle = new Rectangle(BRICK_SIZE, BRICK_SIZE);
                rectangle.setFill(getFillColor(brick.getBrickData()[i][j]));
                rectangles[i][j] = rectangle;
                brickPanel.add(rectangle, j, i);
            }
        }

        brickPanel.setLayoutX(+12.5 +gamePanel.getLayoutX() + brick.getxPosition() * (brickPanel.getVgap() + BRICK_SIZE));
        brickPanel.setLayoutY(-42 + gamePanel.getLayoutY() + brick.getyPosition() * (brickPanel.getHgap() + BRICK_SIZE));

        timeLine = new Timeline(new KeyFrame(
                Duration.millis(400),
                ae -> moveDown(new MoveEvent(EventType.DOWN, EventSource.THREAD))
        ));
        timeLine.setCycleCount(Timeline.INDEFINITE);
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

    public void refreshBrick(ViewData brick) {
        if (!isPause.get()) {
            brickPanel.setLayoutX(+12.5 +gamePanel.getLayoutX() + brick.getxPosition() * (brickPanel.getVgap() + BRICK_SIZE));
            brickPanel.setLayoutY(-42 + gamePanel.getLayoutY() + brick.getyPosition() * (brickPanel.getHgap() + BRICK_SIZE));

            for (int i = 0; i < brick.getBrickData().length; i++) {
                for (int j = 0; j < brick.getBrickData()[i].length; j++) {
                    setRectangleData(brick.getBrickData()[i][j], rectangles[i][j]);
                }
            }
        }
    }

    public void refreshGameBackground(int[][] board) {
        for (int i = 2; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                setRectangleData(board[i][j], displayMatrix[i][j]);
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
                groupNotification.getChildren().add(notificationPanel);
                notificationPanel.showScore(groupNotification.getChildren());
            }
            refreshBrick(downData.getViewData());
        }
        gamePanel.requestFocus();
    }

    public void setEventListener(InputEventListener eventListener) {
        this.eventListener = eventListener;
    }

    public void bindScore(IntegerProperty scoreintegerProperty) {
        scoreLabel.textProperty().bind(scoreintegerProperty.asString("Score: %d"));
    }

    public void gameOver() {
        timeLine.stop();
        gameOverPanel.setVisible(true);
        isGameOver.set(true);
        isPause.set(false);

        if (pauseLayer != null) pauseLayer.setVisible(false);
    }

    public void newGame(ActionEvent actionEvent) {
        timeLine.stop();
        gameOverPanel.setVisible(false);
        eventListener.createNewGame();
        gamePanel.requestFocus();
        timeLine.play();

        isPause.set(false);
        isGameOver.set(false);

        if (pauseLayer != null) pauseLayer.setVisible(false);
    }

    public void pauseGame(ActionEvent actionEvent) {
        gamePanel.requestFocus();
    }
}
