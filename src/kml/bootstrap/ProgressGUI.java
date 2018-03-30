package kml.bootstrap;

import javax.swing.*;

public class ProgressGUI extends JFrame{
    private JPanel rootPanel;
    private JProgressBar progressBar;
    private JLabel progressLabel;

    ProgressGUI() {
        setTitle("Krothium Bootstrap " + Bootstrap.BOOTSTRAP_VERSION);
        setLocationRelativeTo(null);
        setResizable(false);
        setSize(450, 125);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setContentPane(rootPanel);
        ImageIcon img = new ImageIcon(getClass().getResource("/kml/bootstrap/icon.png"));
        setIconImage(img.getImage());
    }

    public void setMaximum(int max) {
        progressBar.setMaximum(max);
    }

    public void setProgress(int progress) {
        progressBar.setValue(progress);
    }

    public void updateLabel(String text) {
        progressLabel.setText(text);
    }
}
