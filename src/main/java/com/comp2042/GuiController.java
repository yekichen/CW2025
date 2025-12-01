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
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.io.File;



import java.net.URL;
import java.util.ResourceBundle;

public class GuiController implements Initializable {

    private static final int BRICK_SIZE = 20;

    @FXML
    private Label scoreLabel;

    @FXML
    private GridPane gamePanel;

    @FXML
    public Group groupNotification;

    @FXML
    private GridPane brickPanel;

    @FXML
    private GameOverPanel gameOverPanel;

    @FXML
    private Label comboLabel;

    //方块hold储存
    @FXML
    private GridPane holdPanel;
    private Rectangle[][] holdRectangles;


    //下一个方块的预览
    @FXML
    private GridPane nextPanel;

    private Rectangle[][] nextRectangles;

    //加入level up
    @FXML
    private Label levelLabel;

    // 新增：暂停图层
    @FXML
    private StackPane pauseLayer;

    @FXML

    private MediaPlayer bgmPlayer;

    public Text pauseText;

    private Rectangle[][] displayMatrix;

    private InputEventListener eventListener;

    private Rectangle[][] rectangles;

    private Rectangle[][] ghostRectangles;

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
                if (isGameOver.get() && keyEvent.getCode() != KeyCode.N) {
                    return;
                }// 允许 Game Over 状态下用 N 重新开始，其它键忽略
                if (isGameOver.get() && keyEvent.getCode() != KeyCode.N) {
                    return;
                }
                // N 键：重新开始
                if (keyEvent.getCode() == KeyCode.N) {
                    newGame(null);
                }

                // 暂停状态下，除了 P 以外都不处理
                if (isPause.get()) {
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

                if (keyEvent.getCode() == KeyCode.SPACE) {
                    eventListener.onHardDrop();
                }

                if (keyEvent.getCode() == KeyCode.C) {
                    eventListener.onHold();
                    return;
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
        initBGM();
    }

    // 暂停 / 恢复
    private void togglePause() {
        if (isGameOver.get()) return;

        boolean nowPaused = !isPause.get();
        isPause.set(nowPaused);

        if (nowPaused) {
            timeLine.pause();
            if (pauseText != null) pauseText.setVisible(true);
            // ⭐ 暂停 BGM
            if (bgmPlayer != null) bgmPlayer.pause();
        } else {
            timeLine.play();
            if (pauseText != null) pauseText.setVisible(false);
            // ⭐ 暂停 BGM
            if (bgmPlayer != null) bgmPlayer.play();
        }
    }

    public void initGameView(int[][] boardMatrix, ViewData brick) {

        // ========== 1. 初始化背景矩阵 ==========
        displayMatrix = new Rectangle[boardMatrix.length][boardMatrix[0].length];
        for (int i = 2; i < boardMatrix.length; i++) {
            for (int j = 0; j < boardMatrix[i].length; j++) {
                Rectangle rectangle = new Rectangle(BRICK_SIZE, BRICK_SIZE);
                rectangle.setFill(Color.TRANSPARENT);
                displayMatrix[i][j] = rectangle;
                gamePanel.add(rectangle, j, i - 2);
            }
        }

        // ========== 2. 初始化当前方块矩阵 rectangles ==========
        rectangles = new Rectangle[brick.getBrickData().length][brick.getBrickData()[0].length];

        for (int i = 0; i < brick.getBrickData().length; i++) {
            for (int j = 0; j < brick.getBrickData()[i].length; j++) {
                Rectangle rectangle = new Rectangle(BRICK_SIZE, BRICK_SIZE);
                rectangle.setFill(getFillColor(brick.getBrickData()[i][j]));
                rectangles[i][j] = rectangle;
                brickPanel.add(rectangle, j, i);
            }
        }
        //========== 3. 初始化当前方块矩阵 ghostRectangles ==========
        ghostRectangles = new Rectangle[4][4]; // Maximum brick size is 4x4

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                Rectangle r = new Rectangle(BRICK_SIZE, BRICK_SIZE);
                r.setFill(Color.LIGHTGRAY);
                r.setOpacity(0.35);
                r.setArcHeight(9);
                r.setArcWidth(9);
                r.setVisible(false);
                r.setMouseTransparent(true); // Important: ghost shouldn't block clicks
                ghostRectangles[i][j] = r;
            }
        }

// Add all ghost rectangles to displayMatrix area (same layer as background)
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                // Use a temporary position, will be updated in refreshGhost
                gamePanel.add(ghostRectangles[i][j], 0, 0);
            }
        }


        // ========== 4. 设置当前方块的位置 ==========
        brickPanel.setLayoutX(+12.5 + gamePanel.getLayoutX() + brick.getxPosition() * (brickPanel.getVgap() + BRICK_SIZE));
        brickPanel.setLayoutY(-42 + gamePanel.getLayoutY() + brick.getyPosition() * (brickPanel.getHgap() + BRICK_SIZE));

        // ========== 5. ⭐ 最后启动 Timeline（不能放前面） ==========
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
            brickPanel.setLayoutX(+12.5 + gamePanel.getLayoutX() + brick.getxPosition() * (brickPanel.getVgap() + BRICK_SIZE));
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

    // ⭐⭐⭐ 新增 Combo 显示更新方法
    public void updateCombo(int combo) {
        if (combo <= 1) {
            comboLabel.setText("");
        } else {
            comboLabel.setText("Combo ×" + combo + " 🔥");
        }
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

        nextPanel.getChildren().clear(); // 清空旧内容

        int rows = nextShape.length;
        int cols = nextShape[0].length;

        nextPanel.setMinSize(GridPane.USE_PREF_SIZE, GridPane.USE_PREF_SIZE);

        double blockSize = 20;  // 你的 BRICK_SIZE

        // 动态控制 next 画布大小（让大形状不会溢出）
        nextPanel.setPrefWidth(cols * (blockSize + nextPanel.getHgap()));
        nextPanel.setPrefHeight(rows * (blockSize + nextPanel.getVgap()));

        // 让 GridPane 在 StackPane 中自动居中
        StackPane.setAlignment(nextPanel, javafx.geometry.Pos.CENTER);

        // 画 next 方块
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {

                Rectangle rect = new Rectangle(blockSize, blockSize);
                rect.setArcWidth(8);
                rect.setArcHeight(8);

                if (nextShape[i][j] != 0) {
                    rect.setFill(getFillColor(nextShape[i][j]));
                } else {
                    rect.setFill(Color.TRANSPARENT);
                }

                nextPanel.add(rect, j, i);
            }
        }
    }
    //hold储存
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
                if (holdShape[i][j] != 0) {
                    holdRectangles[i][j].setFill(Color.YELLOW);
                } else {
                    holdRectangles[i][j].setFill(Color.TRANSPARENT);
                }
            }
        }
    }

    public void refreshGhost(ViewData brick, int ghostY) {
        if (ghostRectangles == null || brick == null) return;

        int[][] shape = brick.getBrickData();
        int xPos = brick.getxPosition();
        int currentY = brick.getyPosition();

        // First, hide all ghost rectangles
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                ghostRectangles[i][j].setVisible(false);
            }
        }

        // Only show ghost if it's below the current piece
        if (ghostY <= currentY) {
            return; // Ghost would overlap with current piece
        }

        // Now show and position the visible ghost blocks
        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                if (shape[i][j] != 0) { // There's a block here

                    int ghostRow = ghostY + i;

                    // Check if this block is within the visible game area
                    if (ghostRow >= 2 && ghostRow < 25) {

                        Rectangle r = ghostRectangles[i][j];
                        r.setVisible(true);

                        // Remove from current position
                        gamePanel.getChildren().remove(r);

                        // Add at correct position (subtract 2 for hidden rows)
                        gamePanel.add(r, xPos + j, ghostRow - 2);
                    }
                }
            }
        }
    }

    public void updateLevel(int level) {
        System.out.println("GUI UPDATE LEVEL = " + level); // ⭐⭐ 加这一行

        if (levelLabel != null) {
            levelLabel.setText("LEVEL  " + level);
        } else {
            System.out.println("LEVEL LABEL IS NULL !!!!!"); // ⭐⭐ 关键测试
        }
    }












}