package com.google.appengine.tck.event;

import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public interface MergeLifecycleEvent extends TestLifecycleEvent {
    WebArchive getDeployment();
}
