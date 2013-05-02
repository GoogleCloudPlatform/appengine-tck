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
