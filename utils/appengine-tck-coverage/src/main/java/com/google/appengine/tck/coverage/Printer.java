package com.google.appengine.tck.coverage;

import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public interface Printer {
    void print(Map<String, Map<Tuple, Set<CodeLine>>> report) throws Exception;
}
