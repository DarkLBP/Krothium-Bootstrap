package kml;

import javafx.application.Application;
import javafx.stage.Stage;

public class Starter extends Application{
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        new Bootstrap(primaryStage, this.getHostServices());
    }
}
