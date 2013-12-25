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

package com.google.appengine.tck.jsp;

import java.io.File;

/**
 * Testing purpose.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class JspMain extends JspMojo {
    public static void main(String[] args) {
        try {
            new JspMain(args[0], args[1]).execute();
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    private final String jspLocation;
    private final String tempDir;

    private JspMain(String jsps, String tempDir) {
        this.jspLocation = jsps;
        this.tempDir = tempDir;
    }

    @Override
    protected File getJspLocation() {
        return new File(jspLocation);
    }

    @Override
    protected File getTempDir() {
        return new File(tempDir);
    }
}
