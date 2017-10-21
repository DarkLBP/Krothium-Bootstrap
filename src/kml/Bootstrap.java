package kml;

import javafx.application.HostServices;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class Bootstrap {

    public Bootstrap(Stage mainStage, HostServices hostServices) {
        FXMLLoader loader = new FXMLLoader(this.getClass().getResource("/kml/Bootstrap.fxml"));
        Parent splash;
        try {
            splash = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
            splash = null;
            System.exit(-1);
        }
        mainStage.setResizable(false);
        Scene sc = new Scene(splash);
        sc.setFill(null);
        mainStage.setScene(sc);
        mainStage.getIcons().add(new Image("/kml/icon.png"));
        mainStage.initStyle(StageStyle.TRANSPARENT);
        BootstrapFX controller = loader.getController();
        controller.initialize(mainStage, hostServices);
    }
}
