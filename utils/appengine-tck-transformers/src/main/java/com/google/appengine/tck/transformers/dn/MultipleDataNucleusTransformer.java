package com.google.appengine.tck.transformers.dn;

import java.lang.instrument.ClassFileTransformer;
import java.util.HashMap;
import java.util.Map;

import com.google.appengine.tck.transformers.MultipleTransformer;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class MultipleDataNucleusTransformer extends MultipleTransformer {
    static Map<String, ClassFileTransformer> transformers() {
        Map<String, ClassFileTransformer> map = new HashMap<String, ClassFileTransformer>();
        map.put("com.google.appengine.datanucleus.DatastoreTestCase", new DatastoreTestCaseTransformer());
        return map;
    }

    public MultipleDataNucleusTransformer() {
        super(new AppEngineDataNucleusTransformer(), transformers());
    }
}
