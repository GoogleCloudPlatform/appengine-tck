package com.google.appengine.tck.category;

/**
 * Sometimes we cannot have all tests in multisuite,
 * due to resources overlap; e.g. logging.properties level tests.
 *
 * In order for JUnit categories to kick-in,
 * we need to explicity ignore tests that should NOT go into multisuite.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public @interface IgnoreMultisuite {
}
