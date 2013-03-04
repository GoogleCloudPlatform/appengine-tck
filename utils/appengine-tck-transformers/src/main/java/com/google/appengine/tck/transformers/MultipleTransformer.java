package com.google.appengine.tck.transformers;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Map;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class MultipleTransformer implements ClassFileTransformer {
    private MatchingClassFileTransformer defaultFileTransformer;
    private Map<String, ClassFileTransformer> transformers;

    public MultipleTransformer(MatchingClassFileTransformer defaultFileTransformer, Map<String, ClassFileTransformer> transformers) {
        this.defaultFileTransformer = defaultFileTransformer;
        this.transformers = transformers;
    }

    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        ClassFileTransformer transformer = transformers.get(className);
        if (transformer != null) {
            return transformer.transform(loader, className, classBeingRedefined, protectionDomain, classfileBuffer);
        }
        if (defaultFileTransformer != null && defaultFileTransformer.match(className)) {
            return defaultFileTransformer.transform(loader, className, classBeingRedefined, protectionDomain, classfileBuffer);
        }
        return classfileBuffer;
    }
}
