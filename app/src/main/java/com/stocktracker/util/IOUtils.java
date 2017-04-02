package com.stocktracker.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by dlarson on 11/25/15.
 */
public class IOUtils {
    private static final int EOF = -1;

    private IOUtils() {

    }

    public static long copy(InputStream input, OutputStream output) throws IOException {
        long count = 0;
        int n = 0;
        final byte[] buffer = new byte[4096];
        while (EOF != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

    public static void close(InputStream s) {
        if(s != null) {
            try {
                s.close();
            } catch(IOException e) {
            }
        }
    }

    public static void close(OutputStream s) {
        if(s != null) {
            try {
                s.close();
            } catch(IOException e) {
            }
        }
    }
}
