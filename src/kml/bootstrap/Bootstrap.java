package kml.bootstrap;

import javax.swing.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


class Bootstrap {
    public static final String BOOTSTRAP_VERSION = "2.0.0";
    private Logging logger;

    Bootstrap(String[] args) {
        File workingDir = getWorkingDirectory();
        File logsFolder = new File(workingDir, "logs");
        if (!logsFolder.isDirectory()) {
            logsFolder.mkdirs();
        }
        File bootstrapLog = new File(logsFolder, "krothium-bootstrap.log");
        try {
            logger = new Logging(bootstrapLog);
            logger.println("Krothium Bootstrap " + BOOTSTRAP_VERSION);
            boolean customJava = downloadRuntime(workingDir);
            download(workingDir);
            start(workingDir, args, customJava);
        } catch (Exception ex) {
            if (logger != null) {
                ex.printStackTrace(logger);
            }
        }
        exit();
    }

    private void download(File workingDir) {
        File launcherETAG = new File(workingDir, "krothium.etag");
        File launcher = new File(workingDir, "krothium.jar");
        logger.println("Downloading launcher...");
        try {
            logger.println("Connecting to server.");
            URL url = new URL("http://mc.krothium.com/bootstrap/launcher.jar");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            String etag ;
            logger.println("Checking matching ETAG.");
            if (launcherETAG.exists()) {
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
                logger.println("Launcher downloaded.");
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

    private boolean downloadRuntime(File workingDir)  {
        ProgressGUI gui = new ProgressGUI();
        logger.println("Downloading java runtime...");
        try {
            String os = System.getProperty("os.name").toLowerCase();
            String arch = System.getProperty("os.arch").endsWith("64") ? "64" : "32";
            String fileName = "";
            if (os.contains("win")) {
                fileName = "jre-windows-" + arch;
            } else if (os.contains("mac") && !arch.equals("32")) {
                fileName = "jre-macos-" + arch;
            } else if (os.contains("linux") || os.contains("unix")) {
                fileName = "jre-linux-" + arch;
            }
            if (fileName.isEmpty()) {
                logger.println("OS " + os + " with arch " + arch + " does not have runtime.");
            } else {
                logger.println("Connecting to server.");
                URL url = new URL("http://mc.krothium.com/bootstrap/" + fileName + ".zip");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                String etag ;
                logger.println("Checking matching ETAG.");
                File jreETAG = new File(workingDir, "jre" + File.separator + "jre.etag");
                if (jreETAG.exists()) {
                    logger.println("ETAG found.");
                    etag = readFile(jreETAG);
                    con.setRequestProperty("If-None-Match", etag);
                }
                if (con.getResponseCode() != HttpURLConnection.HTTP_NOT_MODIFIED) {
                    File parent = jreETAG.getParentFile();
                    if (parent.isDirectory()) {
                        logger.println("Cleaning existing jre folder...");
                        deleteDirectory(parent);
                        logger.println("Done.");
                    }
                    gui.setVisible(true);
                    etag = con.getHeaderField("ETag");
                    int totalSize = con.getContentLength();
                    gui.setMaximum(totalSize);
                    TrackedInputStream tracked = new TrackedInputStream(con.getInputStream());
                    ZipInputStream in = new ZipInputStream(tracked);
                    ZipEntry entry;
                    while ((entry = in.getNextEntry()) != null) {
                        gui.updateLabel("Downloading " + entry.getName() + "...");
                        logger.println("Downloading " + entry.getName() + "...");
                        File outputFile = new File(workingDir, entry.getName());
                        if (entry.isDirectory()) {
                            outputFile.mkdirs();
                        } else {
                            outputFile.getParentFile().mkdir();
                            FileOutputStream out = new FileOutputStream(outputFile);
                            byte[] buffer = new byte[8192];
                            int read;
                            while ((read = in.read(buffer)) != -1) {
                                out.write(buffer, 0, read);
                                gui.setProgress(tracked.getTotalRead());
                            }
                            out.close();
                        }
                        in.closeEntry();
                    }
                    in.close();
                    logger.println("Java runtime downloaded.");
                    logger.println("Saving ETAG for later.");
                    PrintWriter writer = new PrintWriter(jreETAG);
                    writer.write(etag);
                    writer.close();
                    gui.dispose();
                    logger.println("Done.");
                } else {
                    logger.println("ETAG matched, not need to redownload.");
                }
                return true;
            }
        } catch (IOException ex) {
            logger.println("Failed to download runtime.");
            ex.printStackTrace(logger);
            gui.dispose();
        }
        return false;
    }

    private void deleteDirectory(File directory) {
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.isDirectory()) {
                        deleteDirectory(f);
                    } else {
                        f.delete();
                    }
                }
            }
        }
        directory.delete();
    }

    private void start(File workingDir, String[] args, boolean customJava) {
        File launcher = new File(workingDir, "krothium.jar");
        File etag = new File(workingDir, "krothium.etag");
        logger.println("Starting launcher.");
        if (launcher.isFile()) {
            String path;
            if (customJava) {
                path = workingDir + File.separator + "jre" + File.separator + "bin" + File.separator;
            } else {
                path = System.getProperty("java.home") + File.separator + "bin" + File.separator;
            }
            String osName = System.getProperty("os.name").toLowerCase();
            if (osName.contains("win") && new File(path + "javaw.exe").isFile()) {
                path += "javaw.exe";
            } else {
                path += "java";
            }
            File javaExec = new File(path);
            logger.println("Making " + path + " executable.");
            if (javaExec.setExecutable(true)) {
                logger.println("Done.");
            } else {
                logger.println("Failed to set file as executable.");
            }
            ArrayList<String> arguments = new ArrayList<String>();
            arguments.add(path);
            arguments.add("-Xmx100M");
            arguments.add("-Xms50M");
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
                p.waitFor();
                if (p.exitValue() != 0) {
                    logger.println("Launcher was not closed properly. Removing files.");
                    etag.delete();
                    JOptionPane.showMessageDialog(null,
                            "Failed to start the launcher. Check the logs for more information.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                logger.println("Something wrong happened.");
                e.printStackTrace(logger);
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
