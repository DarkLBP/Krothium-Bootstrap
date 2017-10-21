package kml;

import javafx.application.HostServices;
import javafx.fxml.FXML;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;


public class BootstrapFX {
    @FXML private ProgressBar progress;
    private HostServices services;
    private Stage stage;

    public void initialize(Stage stage, HostServices services) {
        this.services = services;
        this.stage = stage;
        this.stage.show();
        this.loadEverything();
    }

    public void loadEverything() {
        try {
            URL url = new URL("http://mc.krothium.com/content/krothium.jar");

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public static File getWorkingDirectory() {
        String userHome = System.getProperty("user.home", ".");
        File workingDirectory;
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("win")) {
            String applicationData = System.getenv("APPDATA");
            String folder = applicationData != null ? applicationData : userHome;
            workingDirectory = new File(folder, ".minecraft/");
        } else if (osName.contains("mac")) {
            workingDirectory = new File(userHome, "Library/Application Support/minecraft");
        } else if (osName.contains("linux") || osName.contains("unix")) {
            workingDirectory = new File(userHome, ".minecraft/");
        } else {
            workingDirectory = new File(userHome, "minecraft/");
        }
        return workingDirectory;
    }
}
