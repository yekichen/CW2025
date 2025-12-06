package com.comp2042;

import javafx.scene.effect.GaussianBlur;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.util.Duration;
import javafx.scene.control.Button;
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
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class GuiController implements Initializable {

    private static final int BRICK_SIZE = 20;
    // press to start
    @FXML private Text pressStartText;
    private boolean gameStarted = false;


    @FXML private Button btnStart;
    @FXML private Button btnPause;
    @FXML private Button btnRestart;
    @FXML private Button btnQuit;

    @FXML private Label linesLabel;

    @FXML private Label scoreLabel;
    @FXML private GridPane gamePanel;
    @FXML public Group groupNotification;
    @FXML private GridPane brickPanel;
    @FXML private GameOverPanel gameOverPanel;
    @FXML private Label comboLabel;
    @FXML private GridPane holdPanel;
    private Rectangle[][] holdRectangles;
    @FXML private GridPane nextPanel;
    private Rectangle[][] nextRectangles;

    @FXML private Label levelLabel;
    @FXML private StackPane pauseLayer;
    @FXML private MediaPlayer bgmPlayer;

    public Text pauseText;

    private Rectangle[][] displayMatrix;
    private InputEventListener eventListener;

    private Rectangle[][] rectangles;
    private Rectangle[][] ghostRectangles;

    private FadeTransition startBlink;
    public Timeline timeLine;

    private GaussianBlur pauseBlur = new GaussianBlur(0);
    private final BooleanProperty isPause = new SimpleBooleanProperty(false);
    private final BooleanProperty isGameOver = new SimpleBooleanProperty(false);

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        pressStartText.setVisible(true);

        Font.loadFont(getClass().getClassLoader().getResource("digital.ttf").toExternalForm(), 38);
        btnStart.setOnAction(e -> onStartClicked());
        btnPause.setOnAction(e -> onPauseClicked());
        btnRestart.setOnAction(e -> onRestartClicked());
        btnQuit.setOnAction(e -> onQuitClicked());
        // ⭐⭐ 加入这两行，确保 UI 初始就有数字 ⭐⭐
        levelLabel.setText("1");
        linesLabel.setText("0");

        gamePanel.setEffect(pauseBlur);
        brickPanel.setEffect(pauseBlur);
        gamePanel.setFocusTraversable(true);
        gamePanel.requestFocus();
        gamePanel.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (!gameStarted) return; // 按键无效

                if (keyEvent.getCode() == KeyCode.P) {
                    togglePause();
                    keyEvent.consume();
                    return;
                }

                if (isGameOver.get() && keyEvent.getCode() != KeyCode.N) return;

                if (keyEvent.getCode() == KeyCode.N) {
                    newGame(null);
                }

                if (isPause.get()) return;

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
                if (keyEvent.getCode() == KeyCode.SPACE) {
                    eventListener.onHardDrop();
                }

                if (keyEvent.getCode() == KeyCode.C) {
                    eventListener.onHold();
                }
            }
        });

        gameOverPanel.setVisible(false);

        if (pauseLayer != null) {
            pauseLayer.setVisible(false);
            pauseLayer.toFront();
            pauseLayer.visibleProperty().bind(isPause);
        }

        Reflection reflection = new Reflection();
        reflection.setFraction(0.8);
        reflection.setTopOpacity(0.9);
        reflection.setTopOffset(-12);

        initBGM();
        //加入开始字样的淡出动画
        pressStartText.setOpacity(0);

        FadeTransition ft = new FadeTransition(Duration.seconds(1.8), pressStartText);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.setCycleCount(1);
        ft.play();

        // ⭐ 循环闪烁的 Press Start 动画
        FadeTransition blink = new FadeTransition(Duration.seconds(1.2), pressStartText);
        blink.setFromValue(0.2);
        blink.setToValue(1.0);
        blink.setCycleCount(Animation.INDEFINITE);
        blink.setAutoReverse(true);
        blink.play();

    }

    private void togglePause() {
        if (isGameOver.get()) return;

        boolean nowPaused = !isPause.get();
        isPause.set(nowPaused);

        if (nowPaused) {

            // ⭐ 暂停：PAUSED 淡入
            if (pauseText != null) {
                pauseText.setOpacity(0);
                pauseText.setVisible(true);

                FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.4), pauseText);
                fadeIn.setFromValue(0);
                fadeIn.setToValue(1);
                fadeIn.play();
            }

            // ⭐ 模糊背景淡入（0 → 20）
            Timeline blurIn = new Timeline(
                    new KeyFrame(Duration.ZERO, e -> pauseBlur.setRadius(0)),
                    new KeyFrame(Duration.seconds(0.4), e -> pauseBlur.setRadius(20))
            );
            blurIn.play();

            timeLine.pause();
            if (bgmPlayer != null) bgmPlayer.pause();

        } else {

            // ⭐ 恢复游戏：PAUSED 淡出
            if (pauseText != null) {
                FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.4), pauseText);
                fadeOut.setFromValue(1);
                fadeOut.setToValue(0);
                fadeOut.setOnFinished(e -> pauseText.setVisible(false));
                fadeOut.play();
            }

            // ⭐ 模糊背景淡出（20 → 0）
            Timeline blurOut = new Timeline(
                    new KeyFrame(Duration.ZERO, e -> pauseBlur.setRadius(20)),
                    new KeyFrame(Duration.seconds(0.4), e -> pauseBlur.setRadius(0))
            );
            blurOut.play();

            timeLine.play();
            if (bgmPlayer != null) bgmPlayer.play();
        }
    }



    public void initGameView(int[][] boardMatrix, ViewData brick) {

        // 1. 背景矩阵
        displayMatrix = new Rectangle[boardMatrix.length][boardMatrix[0].length];
        for (int i = 2; i < boardMatrix.length; i++) {
            for (int j = 0; j < boardMatrix[i].length; j++) {
                Rectangle rectangle = new Rectangle(BRICK_SIZE, BRICK_SIZE);
                rectangle.setFill(Color.TRANSPARENT);
                displayMatrix[i][j] = rectangle;
                gamePanel.add(rectangle, j, i - 2);
            }
        }

        // 2. 当前砖块矩阵
        rectangles = new Rectangle[brick.getBrickData().length][brick.getBrickData()[0].length];
        for (int i = 0; i < brick.getBrickData().length; i++) {
            for (int j = 0; j < brick.getBrickData()[i].length; j++) {
                Rectangle rectangle = new Rectangle(BRICK_SIZE, BRICK_SIZE);
                rectangle.setFill(getFillColor(brick.getBrickData()[i][j]));
                rectangles[i][j] = rectangle;
                brickPanel.add(rectangle, j, i);
            }
        }

        // 3. Ghost 初始化
        ghostRectangles = new Rectangle[4][4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                Rectangle r = new Rectangle(BRICK_SIZE, BRICK_SIZE);
                r.setFill(Color.LIGHTGRAY);
                r.setOpacity(0.35);
                r.setVisible(false);
                r.setArcHeight(9);
                r.setArcWidth(9);
                r.setMouseTransparent(true);
                ghostRectangles[i][j] = r;
                gamePanel.add(r, 0, 0);
            }
        }

        // 4. 设置方块位置
        brickPanel.setLayoutX(+12.5 + gamePanel.getLayoutX() +
                brick.getxPosition() * (brickPanel.getVgap() + BRICK_SIZE));
        brickPanel.setLayoutY(-42 + gamePanel.getLayoutY() +
                brick.getyPosition() * (brickPanel.getHgap() + BRICK_SIZE));
        /*brickPanel.setLayoutX(
                gamePanel.getLayoutX() +
                        brick.getxPosition() * (BRICK_SIZE + brickPanel.getHgap())
        );
        brickPanel.setLayoutY(
                gamePanel.getLayoutY() +
                        (brick.getyPosition() - 2) * (BRICK_SIZE + brickPanel.getVgap())
        );*/


        // ⭐⭐⭐ 5. 修复：初始速度 = LevelManager.getCurrentSpeed()
        GameController gc = (GameController) eventListener;  // 强转
        int initSpeed = gc.getBoard().getLevelManager().getCurrentSpeed();

        timeLine = new Timeline(
                new KeyFrame(Duration.millis(initSpeed),
                        e -> moveDown(new MoveEvent(EventType.DOWN, EventSource.THREAD)))
        );
        timeLine.setCycleCount(Timeline.INDEFINITE);
        timeLine.pause();

        gameOverPanel.getRestartButton().setOnAction(e -> {
            onRestartClicked();
        });

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
            brickPanel.setLayoutX(+12.5 + gamePanel.getLayoutX() +
                    brick.getxPosition() * (brickPanel.getVgap() + BRICK_SIZE));
            brickPanel.setLayoutY(-42 + gamePanel.getLayoutY() +
                    brick.getyPosition() * (brickPanel.getHgap() + BRICK_SIZE));

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
        if (!gameStarted) return;
        if (isPause.get()) return;

        DownData downData = eventListener.onDownEvent(event);

        if (downData == null) return;

        if (downData.getClearRow() != null && downData.getClearRow().getLinesRemoved() > 0) {
            NotificationPanel panel =
                    new NotificationPanel("+" + downData.getClearRow().getScoreBonus());
            groupNotification.getChildren().add(panel);
            panel.showScore(groupNotification.getChildren());
        }

        refreshBrick(downData.getViewData());
        gamePanel.requestFocus();
    }

    public void setEventListener(InputEventListener eventListener) {
        this.eventListener = eventListener;
    }

    public void updateCombo(int combo) {
        if (combo <= 1) comboLabel.setText("");
        else comboLabel.setText("×" + combo + " 🔥");
    }

    public void bindScore(IntegerProperty scoreintegerProperty) {
        scoreLabel.textProperty().bind(scoreintegerProperty.asString("%d"));
    }

    public void gameOver() {
        timeLine.stop();
        isGameOver.set(true);
        isPause.set(false);

        // ⭐ 让 PAUSED 文本消失
        if (pauseText != null) pauseText.setVisible(false);

        // ⭐ 背景暗化
        gamePanel.setOpacity(0.4);
        brickPanel.setOpacity(0.4);

        // ⭐ Game Over 面板淡入
        gameOverPanel.setOpacity(0);
        gameOverPanel.setVisible(true);

        FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.8), gameOverPanel);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();

        if (bgmPlayer != null) bgmPlayer.pause();
    }


    public void newGame(ActionEvent actionEvent) {
        gamePanel.setOpacity(1);
        brickPanel.setOpacity(1);

        timeLine.stop();
        gameOverPanel.setVisible(false);
        eventListener.createNewGame();
        gamePanel.requestFocus();
        timeLine.play();

        isPause.set(false);
        isGameOver.set(false);

        if (pauseLayer != null) pauseLayer.setVisible(false);
    }

    private void initBGM() {
        try {
            String bgmPath = "src/main/resources/audio/bgm.mp3";
            Media bgm = new Media(new File(bgmPath).toURI().toString());
            bgmPlayer = new MediaPlayer(bgm);
            bgmPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            bgmPlayer.setVolume(0.5);
            bgmPlayer.play();
        } catch (Exception e) {
            System.out.println("加载 BGM 失败：" + e.getMessage());
        }
    }

    public void refreshNext(int[][] nextShape) {
        nextPanel.getChildren().clear();
        int rows = nextShape.length;
        int cols = nextShape[0].length;

        nextPanel.setPrefWidth(cols * (20 + nextPanel.getHgap()));
        nextPanel.setPrefHeight(rows * (20 + nextPanel.getVgap()));

        StackPane.setAlignment(nextPanel, javafx.geometry.Pos.CENTER);

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                Rectangle r = new Rectangle(20, 20);
                r.setArcWidth(8);
                r.setArcHeight(8);
                r.setFill(nextShape[i][j] == 0 ? Color.TRANSPARENT : getFillColor(nextShape[i][j]));
                nextPanel.add(r, j, i);
            }
        }
    }

    public void refreshHold(int[][] holdShape) {
        if (holdRectangles == null) {
            holdRectangles = new Rectangle[holdShape.length][holdShape[0].length];
            for (int i = 0; i < holdShape.length; i++) {
                for (int j = 0; j < holdShape[i].length; j++) {
                    Rectangle r = new Rectangle(20, 20);
                    r.setFill(Color.TRANSPARENT);
                    holdRectangles[i][j] = r;
                    holdPanel.add(r, j, i);
                }
            }
        }

        for (int i = 0; i < holdShape.length; i++) {
            for (int j = 0; j < holdShape[i].length; j++) {
                if (holdShape[i][j] != 0)
                    holdRectangles[i][j].setFill(Color.YELLOW);
                else
                    holdRectangles[i][j].setFill(Color.TRANSPARENT);
            }
        }
    }

    public void refreshGhost(ViewData brick, int ghostY) {
        if (ghostRectangles == null || brick == null) return;

        int[][] shape = brick.getBrickData();
        int xPos = brick.getxPosition();
        int yPos = brick.getyPosition();

        for (Rectangle[] row : ghostRectangles)
            for (Rectangle r : row) r.setVisible(false);

        if (ghostY <= yPos) return;

        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                if (shape[i][j] != 0) {

                    int ghostRow = ghostY + i;

                    if (ghostRow >= 2 && ghostRow < 25) {
                        Rectangle r = ghostRectangles[i][j];
                        r.setVisible(true);

                        gamePanel.getChildren().remove(r);
                        gamePanel.add(r, xPos + j, ghostRow - 2);
                    }
                }
            }
        }
    }

    public void updateLevel(int level) {
        if (levelLabel != null) {
            levelLabel.setText(String.valueOf(level));
        }
    }

    public void flashRows(int[] rows, boolean white) {
        for (int r : rows) {
            if (r < 2 || r >= displayMatrix.length) continue;

            for (int c = 0; c < displayMatrix[r].length; c++) {
                displayMatrix[r][c].setFill(
                        white ? Color.WHITE : Color.TRANSPARENT
                );
            }
        }
    }

    public void hideBrick() {
        for (Rectangle[] row : rectangles) {
            for (Rectangle r : row) {
                r.setFill(Color.TRANSPARENT);
            }
        }
    }

    public void showFloatingScore(int score) {
        NotificationPanel p = new NotificationPanel("+" + score);
        groupNotification.getChildren().add(p);
        p.showScore(groupNotification.getChildren());
    }



    public void updateLinesCleared(int totalLines) {
        if (linesLabel != null) {
            linesLabel.setText(String.valueOf(totalLines));
        }
    }
    public void onStartClicked() {

        // 1️⃣ 游戏已经开始且暂停 → 调用 togglePause() 恢复游戏（含淡出动画）
        if (gameStarted && isPause.get()) {
            togglePause();
            return;
        }

        // 2️⃣ 游戏已经开始 → 不重复开始
        if (gameStarted) return;

        // 3️⃣ 第一次开始游戏
        gameStarted = true;
        isGameOver.set(false);
        isPause.set(false);

        // ⭐ 停止闪烁动画
        if (startBlink != null) {
            startBlink.stop();
        }

        // ⭐ 淡出动画（代替直接 setVisible(false)）
        if (pressStartText != null) {
            FadeTransition hide = new FadeTransition(Duration.seconds(0.8), pressStartText);
            hide.setFromValue(1);
            hide.setToValue(0);
            hide.setOnFinished(e -> pressStartText.setVisible(false));
            hide.play();
        }

        timeLine.play();
        gamePanel.requestFocus();
    }





    public void onPauseClicked() {
        togglePause();
    }

    public void onRestartClicked() {
        gameStarted = true;
        if (pressStartText != null) pressStartText.setVisible(false);
        newGame(null);
    }


    public void onQuitClicked() {
        System.exit(0);
    }


}
