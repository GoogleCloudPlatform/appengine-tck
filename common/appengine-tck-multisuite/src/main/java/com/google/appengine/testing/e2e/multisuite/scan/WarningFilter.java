package com.google.appengine.testing.e2e.multisuite.scan;

import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class WarningFilter extends NotificationFilter {
    public WarningFilter(WebArchive uber, WebArchive archive) {
        super(uber, archive);
    }

    protected void validate(ArchivePath path, boolean equal) {
        if (equal == false) {
            log.warning("Duplicate resource: " + path.get() + " !!");
        }
    }
}
