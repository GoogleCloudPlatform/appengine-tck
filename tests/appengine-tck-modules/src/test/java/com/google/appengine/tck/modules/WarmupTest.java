/*
 * Copyright 2015 Google Inc. All Rights Reserved.
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

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.protocol.modules.OperateOnModule;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@RunWith(Arquillian.class)
public class WarmupTest extends ModulesTestBase {

    @Deployment
    public static EnterpriseArchive getWarmupDeployment() {
        WebArchive module3 = getTckSubDeployment(3);
        module3.addClass(WarmupTest.class);

        WebArchive module4 = getTckSubDeployment(4);
        module4.addClass(WarmupTest.class);

        return getEarDeployment("warmup-application.xml", module3, module4);
    }

    @Test
    @InSequence(2)
    @OperateOnModule("m3")
    public void testM3() throws Exception {
    }

    @Test
    @InSequence(3)
    @OperateOnModule("m4")
    public void testM4() throws Exception {
    }

}
