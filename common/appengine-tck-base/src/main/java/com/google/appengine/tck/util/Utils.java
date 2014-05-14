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

package com.google.appengine.tck.util;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public final class Utils {
    private static final Logger log = Logger.getLogger(Utils.class.getName());

    public static void copyStream(final InputStream in, final OutputStream out) throws IOException {
        final byte[] bytes = new byte[8192];
        int cnt;
        while ((cnt = in.read(bytes)) != -1) {
            out.write(bytes, 0, cnt);
        }
    }

    public static void safeClose(Closeable closeable) {
        try {
            closeable.close();
        } catch (IOException e) {
            log.warning("Exception closing " + closeable);
        }
    }

    public static String readFullyAndClose(InputStream in) throws IOException {
        try {
            StringBuilder sbuf = new StringBuilder();
            int ch;
            while ((ch = in.read()) != -1) {
                sbuf.append((char) ch);
            }
            return sbuf.toString().trim();
        } finally {
            in.close();
        }
    }

    // replace ${x:default}
    public static String replace(String string) {
        if (string.startsWith("${") && string.endsWith("}")) {
            string = string.substring(2, string.length() - 1);
            int p = string.indexOf(":");
            if (p >= 0) {
                return System.getProperty(string.substring(0, p), replace(string.substring(p + 1)));
            } else {
                return System.getProperty(string);
            }
        } else {
            return string;
        }
    }
}
