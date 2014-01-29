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

package org.jboss.capedwarf.tck;

import com.google.appengine.tck.base.TestBase;
import com.google.appengine.tck.mail.EmailAddressFormatter;
import com.google.appengine.tck.mail.EmailMessageField;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
public class CapedwarfEmailAddressFormatter implements EmailAddressFormatter {

    public static final CapedwarfEmailAddressFormatter INSTANCE = new CapedwarfEmailAddressFormatter();

    @Override
    public String format(String user, String appId, String domain, EmailMessageField emailMessageField) {
        String outboundEmail = TestBase.getTestSystemProperty("capedwarf.mail.email");
        if (outboundEmail == null) {
            throw new IllegalStateException("-Dcapedwarf.mail.email not specified");
        }
        if (emailMessageField == EmailMessageField.FROM || emailMessageField == EmailMessageField.REPLY_TO) {
            return outboundEmail;
        } else {
            return outboundEmail.replaceFirst("@", "+" + user + "@");
        }
    }

}
