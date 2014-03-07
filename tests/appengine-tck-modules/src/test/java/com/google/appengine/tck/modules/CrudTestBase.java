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

package com.google.appengine.tck.modules;

import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.protocol.modules.OperateOnModule;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public abstract class CrudTestBase extends ModulesTestBase {
    protected static EnterpriseArchive getCrudDeployment(Class<?> testClass) {
        WebArchive module1 = getTckSubDeployment(1);
        module1.addClass(CrudTestBase.class);
        module1.addClass(testClass);

        WebArchive module2 = getTckSubDeployment(2);
        module2.addClass(CrudTestBase.class);
        module2.addClass(testClass);

        return getEarDeployment(module1, module2);
    }

    @Test
    @InSequence(1)
    public void testCreate() throws Exception {
        doTestCreate();
    }

    protected abstract void doTestCreate() throws Exception;

    @Test
    @InSequence(2)
    @OperateOnModule("m2")
    public void testRead() throws Exception {
        doTestRead();
    }

    protected abstract void doTestRead() throws Exception;

    @Test
    @InSequence(3)
    public void testUpdate() throws Exception {
        doTestUpdate();
    }

    protected abstract void doTestUpdate() throws Exception;

    @Test
    @InSequence(4)
    @OperateOnModule("m2")
    public void testReRead() throws Exception {
        doTestReRead();
    }

    protected abstract void doTestReRead() throws Exception;

    @Test
    @InSequence(5)
    public void testDelete() throws Exception {
        doTestDelete();
    }

    protected abstract void doTestDelete() throws Exception;

    @Test
    @InSequence(6)
    @OperateOnModule("m2")
    public void testCheck() throws Exception {
        doTestCheck();
    }

    protected abstract void doTestCheck() throws Exception;
}
