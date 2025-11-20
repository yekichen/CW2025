package com.comp2042;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL; // 确保导入

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        System.out.println("DEBUG: 1. Application Starting FXML Load...");

        // 确保 FXML 文件路径正确
        URL resourceUrl = getClass().getResource("/gameLayout.fxml");
        if (resourceUrl == null) {
            System.err.println("FATAL ERROR: FXML file 'gameLayout.fxml' not found.");
            return;
        }

        FXMLLoader loader = new FXMLLoader(resourceUrl);
        Parent root = loader.load();
        System.out.println("DEBUG: 2. FXML Loaded.");

        GuiController guiController = loader.getController();

        // 1. 实例化游戏核心逻辑 (GameController)
        GameController gameController = new GameController(guiController);
        System.out.println("DEBUG: 3. GameController Initialized.");

        // 2. 【关键步骤】将游戏逻辑实例设置给 GUI
        guiController.setEventListener(gameController);

        // 3. 启动所有依赖于 eventListener 的 GUI 元素和游戏循环
        guiController.postLoadSetup();
        System.out.println("DEBUG: 4. GUI Setup Complete (Game Loop Started).");

        // ⭐️ 修复：设置 Scene 和 Stage ⭐️
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("COMP2042 Tetris");
        primaryStage.setResizable(false);
        primaryStage.show();
        System.out.println("DEBUG: 5. Stage Shown. Game should be visible now.");

        // 确保游戏区域获得焦点以接收键盘输入
        guiController.getGamePanel().requestFocus();
    }

    public static void main(String[] args) {
        launch(args);
    }
}