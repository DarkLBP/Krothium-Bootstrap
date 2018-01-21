package kml.bootstrap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class Logging extends PrintWriter {

    public Logging(File file) throws FileNotFoundException {
        super(file);
    }

    @Override
    public void println(Object o) {
        System.out.println(o);
        super.println(o);
    }

    @Override
    public void println(String s) {
        System.out.println(s);
        super.println(s);
    }
}
