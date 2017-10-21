package kml.bootstrap;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;


class Bootstrap {
    public Bootstrap(String[] args) {
        this.download();
        this.start(args);
    }

    private void download() {
        File workingDir = getWorkingDirectory();
        File launcherETAG = new File(workingDir, "krothium.etag");
        File launcher = new File(workingDir, "krothium.jar");
        try {
            URL url = new URL("http://mc.krothium.com/content/krothium.jar");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            String etag = "";
            if (launcherETAG.exists()) {
                etag = new String(Files.readAllBytes(launcherETAG.toPath()), StandardCharsets.UTF_8);
                con.setRequestProperty("If-None-Match", etag);
            }
            if (con.getResponseCode() != HttpURLConnection.HTTP_NOT_MODIFIED) {
                etag = con.getHeaderField("ETag");
                InputStream in = con.getInputStream();
                FileOutputStream out = new FileOutputStream(launcher);
                int read;
                byte[] buffer = new byte[8192];
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
                in.close();
                out.close();
                PrintWriter writer = new PrintWriter(launcherETAG);
                writer.write(etag);
                writer.close();
            }
        } catch (IOException e) {
            if (!launcher.isFile() && !launcherETAG.isFile()) {
                System.exit(-1);
            }
        }
    }


    private void start(String[] args) {
        String path = System.getProperty("java.home") + File.separator + "bin" + File.separator;
        File javaWin = new File(path + "javaw.exe");
        File javaOther = new File(path + "java.exe");
        File workingDir = getWorkingDirectory();
        File launcher = new File(workingDir, "krothium.jar");
        ArrayList<String> arguments = new ArrayList<>();
        if (javaWin.isFile()) {
            arguments.add(javaWin.getAbsolutePath());
        } else {
            arguments.add(javaOther.getAbsolutePath());
        }
        arguments.add("-jar");
        arguments.add(launcher.getAbsolutePath());
        try {
            ProcessBuilder b = new ProcessBuilder(arguments);
            b.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static File getWorkingDirectory() {
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
