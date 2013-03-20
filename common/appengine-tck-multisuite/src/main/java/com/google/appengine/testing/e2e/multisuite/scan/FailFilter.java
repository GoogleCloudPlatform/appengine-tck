package com.google.appengine.testing.e2e.multisuite.scan;

import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class FailFilter extends NotificationFilter {
    public FailFilter(WebArchive uber, WebArchive archive) {
        super(uber, archive);
    }

    protected void validate(ArchivePath path, boolean equal) {
        if (equal == false) {
            throw new IllegalArgumentException("Different resource already exists: " + path.get() + " !!");
        }
    }
}
