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

package com.google.appengine.tck.multisuite.scan;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

import com.google.appengine.tck.util.Utils;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Filter;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public abstract class NotificationFilter implements Filter<ArchivePath> {
    protected static final Set<String> ALLOWED_DUPLICATES;

    static {
        ALLOWED_DUPLICATES = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        ALLOWED_DUPLICATES.add("/WEB-INF/classes/test-contexts.properties");
        ALLOWED_DUPLICATES.add("/WEB-INF/classes/timestamp.txt");
    }

    protected final Logger log = Logger.getLogger(getClass().getName());

    protected final WebArchive uber;
    protected final WebArchive archive;

    protected NotificationFilter(WebArchive uber, WebArchive archive) {
        this.uber = uber;
        this.archive = archive;
    }

    protected abstract void validate(ArchivePath path, boolean equal);

    protected boolean isAllowedDuplicate(Node node) {
        return ALLOWED_DUPLICATES.contains(node.getPath().get());
    }

    public boolean include(ArchivePath path) {
        Node node = uber.get(path);
        if (node != null && isAllowedDuplicate(node) == false) {
            Asset asset = node.getAsset();
            if (asset != null) {
                Asset other = archive.get(path).getAsset();
                validate(path, equal(asset, other));
            }
            return false;
        }
        return true;
    }

    protected static boolean equal(Asset a1, Asset a2) {
        byte[] bytes1 = toBytes(a1);
        byte[] bytes2 = toBytes(a2);
        return Arrays.equals(bytes1, bytes2);
    }

    protected static byte[] toBytes(Asset asset) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            InputStream is = asset.openStream();
            try {
                Utils.copyStream(is, baos);
            } finally {
                Utils.safeClose(is);
            }
            return baos.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
