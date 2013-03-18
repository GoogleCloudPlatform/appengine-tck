package com.google.appengine.tck.lib;

import java.io.File;

import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.PomEquippedResolveStage;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class LibUtils {
    private PomEquippedResolveStage resolver;

    public void addGaeAsLibrary(WebArchive war) {
        addLibrary(war, "com.google.appengine:appengine-api-1.0-sdk");
    }

    public void addLibrary(WebArchive war, String groupId, String artifactId) {
        addLibrary(war, groupId + ":" + artifactId);
    }

    public void addLibrary(WebArchive war, String coordinate) {
        war.addAsLibraries(getDependency(coordinate));
    }

    // ------------

    protected PomEquippedResolveStage getResolver() {
        if (resolver == null)
            resolver = Maven.resolver().loadPomFromFile(buildPomPath());
        return resolver;
    }

    protected String buildPomPath() {
        return "pom.xml";
    }

    private File getDependency(final String coordinates) {
        return getResolver().resolve(coordinates).withoutTransitivity().asSingle(File.class);
    }
}
