package kml.bootstrap;

import javax.swing.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;


class Bootstrap {
    private final Logging logger;

    Bootstrap(String[] args) throws FileNotFoundException {
        File workingDir = getWorkingDirectory();
        File launcherETAG = new File(workingDir, "krothium.etag");
        File launcher = new File(workingDir, "krothium.jar");
        File logsFolder = new File(workingDir, "logs");
        if (!logsFolder.isDirectory()) {
            logsFolder.mkdirs();
        }
        File bootstrapLog = new File(logsFolder, "krothium-bootstrap.log");
        logger = new Logging(bootstrapLog);
        logger.println("Krothium Bootstrap 1.3.0");
        logger.println("Checking Java version.");
        checkJavaVersion();
        download(launcher, launcherETAG);
        start(launcher, launcherETAG, args);
        exit();
    }

    private void checkJavaVersion() {
        double javaVersion = Double.parseDouble(System.getProperty("java.specification.version"));
        logger.println("Found java version " + javaVersion);
        if (javaVersion < 1.8) {
            logger.println("Java version not supported.");
            JOptionPane.showMessageDialog(null, "Java 9 or higher required to run the launcher.\n" +
                    "Go to https://www.java.com to download a most updated version.");
            exit();
        }
    }

    private void download(File launcher, File launcherETAG) {
        try {
            logger.println("Connecting to server.");
            URL url = new URL("http://mc.krothium.com/bootstrap/1/krothium.jar");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            String etag ;
            logger.println("Checking matching ETAG.");
            if (launcherETAG.exists() && launcher.exists()) {
                logger.println("ETAG found.");
                etag = readFile(launcherETAG);
                con.setRequestProperty("If-None-Match", etag);
            }
            if (con.getResponseCode() != HttpURLConnection.HTTP_NOT_MODIFIED) {
                logger.println("Download required.");
                etag = con.getHeaderField("ETag");
                InputStream in = con.getInputStream();
                FileOutputStream out = new FileOutputStream(launcher);
                logger.println("Downloading from " + url.toString() + ".");
                pipeStreams(in, out);
                logger.println("Download completed.");
                logger.println("Saving ETAG for later.");
                PrintWriter writer = new PrintWriter(launcherETAG);
                writer.write(etag);
                writer.close();
                logger.println("Done.");
            } else {
                logger.println("ETAG matched, not need to redownload.");
            }
        } catch (Exception e) {
            logger.println("Something wrong happened.");
            e.printStackTrace(logger);
            if (!launcher.isFile() || !launcherETAG.isFile()) {
                JOptionPane.showMessageDialog(null,
                        "Failed to download the launcher. Check the logs for more information.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void start(File launcher, File etag, String[] args) {
        logger.println("Starting launcher.");
        if (launcher.isFile()) {
            String path = System.getProperty("java.home") + File.separator + "bin" + File.separator;
            String osName = System.getProperty("os.name");
            if (osName.contains("win") && new File(path + "javaw.exe").isFile()) {
                path += "javaw.exe";
            } else {
                path += "java";
            }
            ArrayList<String> arguments = new ArrayList<String>();
            arguments.add(path);
            arguments.add("-jar");
            arguments.add(launcher.getAbsolutePath());
            arguments.addAll(Arrays.asList(args));
            try {
                ProcessBuilder b = new ProcessBuilder(arguments);
                Process p = b.start();
                InputStreamReader inReader = new InputStreamReader(p.getInputStream(), Charset.forName("UTF-8"));
                BufferedReader reader = new BufferedReader(inReader);
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
                if (p.exitValue() != 0) {
                    logger.println("Launcher was not closed properly. Removing files.");
                    launcher.delete();
                    etag.delete();
                    JOptionPane.showMessageDialog(null,
                            "Failed to start the launcher. Check the logs for more information.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                logger.println("Something wrong happened.");
                e.printStackTrace(logger);
                launcher.delete();
                etag.delete();
                JOptionPane.showMessageDialog(null,
                        "Failed to start the launcher. Check the logs for more information.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            logger.println("No krothium.jar found at " + launcher.getAbsolutePath() + ".");
            logger.println("We could not start the launcher.");
            JOptionPane.showMessageDialog(null,
                    "Failed to start the launcher. Check the logs for more information.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String readFile(File file) {
        StringBuilder builder = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), Charset.forName("UTF-8")));
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace(logger);
        } catch (IOException e) {
            e.printStackTrace(logger);
        }
        return builder.toString();
    }

    private void pipeStreams(InputStream in, OutputStream out) throws IOException {
        int read;
        byte[] buffer = new byte[8192];
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
        in.close();
        out.close();
    }

    private File getWorkingDirectory() {
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

    private void exit() {
        logger.close();
        System.exit(0);
    }
}
