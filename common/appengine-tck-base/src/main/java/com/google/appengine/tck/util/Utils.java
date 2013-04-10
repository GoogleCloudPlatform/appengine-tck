package com.google.appengine.tck.util;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public final class Utils {
    private static final Logger log = Logger.getLogger(Utils.class.getName());

    public static void copyStream(final InputStream in, final OutputStream out) throws IOException {
        final byte[] bytes = new byte[8192];
        int cnt;
        while ((cnt = in.read(bytes)) != -1) {
            out.write(bytes, 0, cnt);
        }
    }

    public static void safeClose(Closeable closeable) {
        try {
            closeable.close();
        } catch (IOException e) {
            log.warning("Exception closing " + closeable);
        }
    }

    public static String readFullyAndClose(InputStream in) throws IOException {
        try {
            StringBuilder sbuf = new StringBuilder();
            int ch;
            while ((ch = in.read()) != -1) {
                sbuf.append((char) ch);
            }
            return sbuf.toString().trim();
        } finally {
            in.close();
        }
    }
}
