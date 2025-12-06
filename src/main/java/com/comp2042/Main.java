package com.comp2042;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        URL location = getClass().getClassLoader().getResource("gameLayout.fxml");
        ResourceBundle resources = null;
        FXMLLoader fxmlLoader = new FXMLLoader(location, resources);
        Parent root = fxmlLoader.load();
        GuiController c = fxmlLoader.getController();

        primaryStage.setTitle("TetrisJFX");

        Scene scene = new Scene(root, 300, 510);
        primaryStage.setScene(scene);

        // ⭐⭐⭐ 在这里加！固定窗口大小！
        primaryStage.setWidth(500);
        primaryStage.setHeight(620);
        primaryStage.setResizable(false);


        primaryStage.show();

        new GameController(c);
    }



    public static void main(String[] args) {
        launch(args);
    }
}
