/*
 * Copyright 2013 Google Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

    private FileMethodExclusion() {
    }

    public static MethodExclusion create(File root) {
        File ef = new File(root, "exclusions.txt");
        if (ef.exists()) {
            FileMethodExclusion fme = new FileMethodExclusion();
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader(ef));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("#")) continue;
                    fme.exclusions.add(line);
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
            return fme;
        } else {
            return new BaseMethodExclusion();
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
