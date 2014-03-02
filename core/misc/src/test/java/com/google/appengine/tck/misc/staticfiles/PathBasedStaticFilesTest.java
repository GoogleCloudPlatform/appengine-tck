/*
 * Copyright 2014 Google Inc. All Rights Reserved.
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

package com.google.appengine.tck.misc.staticfiles;

import java.net.URL;

import com.google.appengine.tck.base.TestContext;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
@RunWith(Arquillian.class)
public class PathBasedStaticFilesTest extends StaticFilesTestBase {

    @Deployment
    public static WebArchive getDeployment() {
        WebArchive archive = getTckDeployment(new TestContext()
            .setAppEngineWebXmlFile("appengine-web-path-based-static-files.xml"));
        createFile(archive, "/foo/bar.txt");
        createFile(archive, "/included_shallow/foo.txt");
        createFile(archive, "/included_shallow/subdir/foo.txt");
        createFile(archive, "/included_shallow/subdir/subsubdir/foo.txt");
        createFile(archive, "/included_deep/foo.txt");
        createFile(archive, "/included_deep/subdir/foo.txt");
        createFile(archive, "/included_deep/subdir/subsubdir/foo.txt");
        createFile(archive, "/included_deep/not_excluded/foo.txt");
        createFile(archive, "/included_deep/excluded_shallow/foo.txt");
        createFile(archive, "/included_deep/excluded_shallow/subdir/foo.txt");
        return archive;
    }

    @Test
    @RunAsClient
    public void testUnincludedFilesNotAccessible(@ArquillianResource URL url) throws Exception {
        assertPageNotFound(url, "foo/bar.txt");
    }

    @Test
    @RunAsClient
    public void testShallowInclude(@ArquillianResource URL url) throws Exception {
        assertPageFound(url, "included_shallow/foo.txt");
        assertPageNotFound(url, "included_shallow/subdir/foo.txt");
        assertPageNotFound(url, "included_shallow/subdir/subsubdir/foo.txt");
    }

    @Test
    @RunAsClient
    public void testDeepInclude(@ArquillianResource URL url) throws Exception {
        assertPageFound(url, "included_deep/foo.txt");
        assertPageFound(url, "included_deep/subdir/foo.txt");
        assertPageFound(url, "included_deep/subdir/subsubdir/foo.txt");
    }

    @Test
    @RunAsClient
    public void testShallowExclude(@ArquillianResource URL url) throws Exception {
        assertPageFound(url, "included_deep/not_excluded/foo.txt");
        assertPageNotFound(url, "included_deep/excluded_shallow/foo.txt");
        assertPageFound(url, "included_deep/excluded_shallow/subdir/foo.txt");
    }

    @Test
    @RunAsClient
    public void testDeepExclude(@ArquillianResource URL url) throws Exception {
        assertPageNotFound(url, "included_deep/excluded_deep/foo.txt");
        assertPageNotFound(url, "included_deep/excluded_deep/subdir/foo.txt");
        assertPageNotFound(url, "included_deep/excluded_deep/subdir/subsubdir/foo.txt");
    }

}
