package com.google.appengine.tck.coverage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javassist.bytecode.ClassFile;
import javassist.bytecode.MethodInfo;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class FileMethodExclusion extends BaseMethodExclusion {
    private Set<String> exclusions = new HashSet<String>();

    public FileMethodExclusion(File root) {
        File ef = new File(root, "exclusions.txt");
        if (ef.exists()) {
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader(ef));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("#")) continue;
                    exclusions.add(line);
                }
            } catch (Exception e) {
                throw new IllegalArgumentException(e);
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException ignore) {
                    }
                }
            }
        }
    }

    @Override
    public boolean exclude(ClassFile clazz, MethodInfo mi) {
        return (super.exclude(clazz, mi) || doExclude(clazz, mi));
    }

    protected boolean doExclude(ClassFile clazz, MethodInfo mi) {
        String exc = clazz.getName() + "@" + mi.getName() + "@" + mi.getDescriptor();
        return exclusions.contains(exc);
    }
}
