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

package com.google.appengine.tck.login;

import com.google.appengine.tck.arquillian.AbstractApplicationArchiveProcessor;
import org.jboss.arquillian.protocol.servlet.arq514hack.descriptors.api.web.WebAppDescriptor;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class UserLoginApplicationArchiveProcessor extends AbstractApplicationArchiveProcessor {
    private static final String WEB_XML_PATH = "WEB-INF/web.xml";

    @Override
    protected void handleWebArchive(WebArchive war) {
        Node node = war.get(WEB_XML_PATH);
        if (node == null) {
            throw new IllegalStateException("No web.xml in .war: " + war.toString(true));
        }

        war.addClass(GetLoginUrlServlet.class);

        WebAppDescriptor webXml = Descriptors.importAs(WebAppDescriptor.class).fromStream(node.getAsset().openStream());
        war.delete(WEB_XML_PATH); // delete first, so we can re-add
        webXml.servlet(UserLogin.USER_LOGIN_SERVLET_PATH + "-servlet", GetLoginUrlServlet.class.getName(), new String[]{"/" + UserLogin.USER_LOGIN_SERVLET_PATH});
        war.setWebXML(new StringAsset(webXml.exportAsString()));
    }
}
