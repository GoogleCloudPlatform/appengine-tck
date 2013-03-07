package com.google.appengine.tck.datanucleus;

import com.google.appengine.testing.e2e.multisuite.MultiContext;
import com.google.appengine.testing.e2e.multisuite.scan.ScanStrategy;

/**
 * One time merge, as all archives are the same.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class DataNucleusScanStrategy implements ScanStrategy {
    private boolean flag = true;

    public boolean doMerge(MultiContext context, Class<?> clazz) {
        boolean tmp = flag;
        flag = false;
        return tmp;
    }
}
