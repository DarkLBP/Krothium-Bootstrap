package kml.bootstrap;

import javax.swing.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;


class Bootstrap {
    Bootstrap(String[] args) {
        File workingDir = getWorkingDirectory();
        File launcherETAG = new File(workingDir, "krothium.etag");
        File launcher = new File(workingDir, "krothium.jar");
        File logsFolder = new File(workingDir, "logs");
        if (!logsFolder.isDirectory()) {
            logsFolder.mkdirs();
        }
        File bootstrapLog = new File(logsFolder, "krothium-bootstrap.log");
        try (Logging logWriter = new Logging(bootstrapLog)){
            logWriter.println("Krothium Bootstrap 1.2.0");
            logWriter.println("Starting.");
            this.download(launcher, launcherETAG, logWriter);
            this.start(launcher, args, logWriter);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void download(File launcher, File launcherETAG, Logging logWriter) {
        try {
            logWriter.println("Connecting to server.");
            URL url = new URL("http://mc.krothium.com/bootstrap/1/krothium.jar");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            String etag = "";
            logWriter.println("Checking matching ETAG.");
            if (launcherETAG.exists()) {
                logWriter.println("ETAG found.");
                etag = new String(Files.readAllBytes(launcherETAG.toPath()), StandardCharsets.UTF_8);
                con.setRequestProperty("If-None-Match", etag);
            }
            if (con.getResponseCode() != HttpURLConnection.HTTP_NOT_MODIFIED) {
                logWriter.println("Download required.");
                etag = con.getHeaderField("ETag");
                InputStream in = con.getInputStream();
                FileOutputStream out = new FileOutputStream(launcher);
                logWriter.println("Downloading from " + url.toString() + ".");
                int read;
                byte[] buffer = new byte[8192];
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
                in.close();
                out.close();
                logWriter.println("Download completed.");
                logWriter.println("Saving ETAG for later.");
                PrintWriter writer = new PrintWriter(launcherETAG);
                writer.write(etag);
                writer.close();
                logWriter.println("Done.");
            } else {
                logWriter.println("ETAG matched, not need to redownload.");
            }
        } catch (Exception e) {
            logWriter.println("Something wrong happened.");
            e.printStackTrace(logWriter);
            if (!launcher.isFile() || !launcherETAG.isFile()) {
                JOptionPane.showMessageDialog(null,
                        "Failed to download the launcher. Check the logs for more information.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void start(File launcher, String[] args, Logging logWriter) {
        logWriter.println("Starting launcher.");
        if (launcher.isFile()) {
            String path = System.getProperty("java.home") + File.separator + "bin" + File.separator;
            String osName = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
            if (osName.contains("win") && new File(path + "javaw.exe").isFile()) {
                path += "javaw.exe";
            } else {
                path += "java";
            }
            ArrayList<String> arguments = new ArrayList<>();
            arguments.add(path);
            arguments.add("-jar");
            arguments.add(launcher.getAbsolutePath());
            arguments.addAll(Arrays.asList(args));
            try {
                ProcessBuilder b = new ProcessBuilder(arguments);
                Process p = b.start();
                InputStreamReader inReader = new InputStreamReader(p.getInputStream(), StandardCharsets.ISO_8859_1);
                BufferedReader reader = new BufferedReader(inReader);
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            } catch (Exception e) {
                logWriter.println("Something wrong happened.");
                e.printStackTrace(logWriter);
                JOptionPane.showMessageDialog(null,
                        "Failed to start the launcher. Check the logs for more information.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            logWriter.println("No krothium.jar found at " + launcher.getAbsolutePath() + ".");
            logWriter.println("We could not start the launcher.");
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
