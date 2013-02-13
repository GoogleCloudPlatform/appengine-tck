package com.google.appengine.testing.e2e.common;

/**
 * Describe context root.
 *
 * e.g. JBoss - ROOT.war + jboss-web.xml
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public interface ContextRoot {
    String getDeploymentName();
    String getDescriptor();
}