package com.google.appengine.testing.e2e.multisuite.scan;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.logging.Logger;

import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Filter;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public abstract class NotificationFilter implements Filter<ArchivePath> {
    protected final Logger log = Logger.getLogger(getClass().getName());

    protected final WebArchive uber;
    protected final WebArchive archive;

    protected NotificationFilter(WebArchive uber, WebArchive archive) {
        this.uber = uber;
        this.archive = archive;
    }

    protected abstract void validate(ArchivePath path, boolean equal);

    public boolean include(ArchivePath path) {
        Node node = uber.get(path);
        if (node != null) {
            Asset asset = node.getAsset();
            if (asset != null) {
                Asset other = archive.get(path).getAsset();
                validate(path, equal(asset, other));
            }
            return false;
        }
        return true;
    }

    protected static boolean equal(Asset a1, Asset a2) {
        byte[] bytes1 = toBytes(a1);
        byte[] bytes2 = toBytes(a2);
        return Arrays.equals(bytes1, bytes2);
    }

    protected static byte[] toBytes(Asset asset) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            InputStream is = asset.openStream();
            try {
                copyStream(is, baos);
            } finally {
                is.close();
            }
            return baos.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    static void copyStream(final InputStream in, final OutputStream out) throws IOException {
        final byte[] bytes = new byte[8192];
        int cnt;
        while ((cnt = in.read(bytes)) != -1) {
            out.write(bytes, 0, cnt);
        }
    }
}
