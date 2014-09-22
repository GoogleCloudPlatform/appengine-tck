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

package com.google.appengine.tck.byteman;

import com.google.appengine.tck.byteman.support.Poke;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.extension.byteman.api.BMRule;
import org.jboss.arquillian.extension.byteman.api.ExecType;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@RunWith(Arquillian.class)
public class SimpleBytemanTest extends BytemanTestBase {

    @Deployment
    public static WebArchive getDeployment() {
        return getBytemanDeployment();
    }

    @BMRule(
        name = "modified",
        targetClass = "com.google.appengine.tck.byteman.support.Poke",
        targetMethod = "modified",
        action = "throw new RuntimeException(\"Bang!\")",
        exec = ExecType.CONTAINER)
    @Test(expected = RuntimeException.class)
    public void testModified() throws Exception {
        Poke.modified();
    }
}
