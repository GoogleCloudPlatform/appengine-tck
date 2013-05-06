package com.google.appengine.tck.coverage;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class Main {
    public static void main(String[] args) throws Exception {
        if (args == null || args.length < 2)
            throw new IllegalArgumentException("Invalid args: " + Arrays.toString(args));

        File classesToScan = new File(args[0]);
        if (classesToScan.exists() == false)
            throw new IllegalArgumentException("No such dir: " + classesToScan);
        if (classesToScan.isDirectory() == false)
            throw new IllegalArgumentException("Is not directory: " + classesToScan);

        List<String> interfaces = Arrays.asList(args).subList(1, args.length);
        CodeCoverage.report(null, new File("").getAbsoluteFile(), classesToScan, FileMethodExclusion.create(classesToScan), interfaces.toArray(new String[interfaces.size()]));
    }
}
