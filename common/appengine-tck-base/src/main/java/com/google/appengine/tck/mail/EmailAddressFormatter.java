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

package com.google.appengine.tck.mail;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
public interface EmailAddressFormatter {

    /**
     * Returns the full email address created from the input arguments (the simplest would be to simply return
     * &lt;user&gt;@&lt;appId&gt;.&lt;domain&gt;, where user, appId and domain are arguments passed to this method.
     *
     * @param user the &lt;user&gt; in user@appId.domain
     * @param appId the &lt;appId&gt; in user@appId.domain
     * @param domain the &lt;domain&gt; in user@appId.domain
     * @param emailMessageField the field where the returned address will be used (FROM, TO, CC, ...)
     * @return the full email address
     */
    String format(String user, String appId, String domain, EmailMessageField emailMessageField);
}

