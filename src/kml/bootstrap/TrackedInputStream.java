package kml.bootstrap;

import java.io.IOException;
import java.io.InputStream;

public class TrackedInputStream extends InputStream {

    private InputStream in;
    private int totalRead;

    public TrackedInputStream(InputStream in) {
        this.in = in;
    }

    @Override
    public int read() throws IOException {
        int read = in.read();
        totalRead++;
        return read;
    }

    @Override
    public int read(byte[] b) throws IOException {
        int read = in.read(b);
        totalRead += read;
        return read;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int read = in.read(b, off, len);
        totalRead += read;
        return read;
    }

    @Override
    public long skip(long n) throws IOException {
        return in.skip(n);
    }

    @Override
    public int available() throws IOException {
        return in.available();
    }

    @Override
    public void close() throws IOException {
        in.close();
    }

    @Override
    public synchronized void mark(int readlimit) {
        in.mark(readlimit);
    }

    @Override
    public synchronized void reset() throws IOException {
        in.reset();
    }

    @Override
    public boolean markSupported() {
        return in.markSupported();
    }

    public int getTotalRead() {
        return totalRead;
    }
}
