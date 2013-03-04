package com.google.appengine.tck.transformers;

import java.io.File;
import java.util.Arrays;

import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.PomEquippedResolveStage;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public abstract class ShrinkWrapTransformer extends JavassistTransformer {
    protected static PomEquippedResolveStage getResolver(String pom) {
        return Maven.resolver().loadPomFromFile(pom);
    }

    protected static File[] resolve(PomEquippedResolveStage resolver, String... coordinates) {
        final File[] files = resolver.resolve(coordinates).withoutTransitivity().as(File.class);
        if (files == null || files.length == 0)
            throw new IllegalArgumentException("Null or empty files (" + Arrays.toString(files) + "): " + Arrays.toString(coordinates));
        return files;
    }
}
