package com.google.appengine.tck.event;

import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
class MergeLifecycleEventImpl extends AbstractTestLifecycleEventImpl<WebArchive> implements MergeLifecycleEvent {
    MergeLifecycleEventImpl(Class<?> caller, WebArchive context) {
        super(caller, context);
    }

    public WebArchive getDeployment() {
        return getContext();
    }
}
