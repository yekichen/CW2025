package com.comp2042.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class GameOverPanel extends BorderPane {

    private Button restartButton;

    public GameOverPanel() {

        // 标题
        Label gameOverLabel = new Label("GAME OVER");
        gameOverLabel.getStyleClass().add("gameOverStyle");

        // 按钮
        restartButton = new Button("Restart");
        restartButton.setStyle(
                "-fx-font-size: 18px; " +
                        "-fx-padding: 8px 25px; " +
                        "-fx-background-color: #222; " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 8;"
        );

        // 垂直布局
        VBox box = new VBox(15, gameOverLabel, restartButton);
        box.setAlignment(Pos.CENTER);

        setCenter(box);
    }

    public Button getRestartButton() {
        return restartButton;
    }
}
