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

import com.google.appengine.tck.driver.LoginContext;
import com.google.appengine.tck.util.Utils;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class UserLoginContext implements LoginContext {
    private final UserIsLoggedIn user;

    public UserLoginContext(UserIsLoggedIn user) {
        this.user = user;
    }

    public String getEmail() {
        return Utils.replace(user.email());
    }

    public String getPassword() {
        return Utils.replace("${user.login.password:${appengine.password:<MISSING_PASSWORD>}}");
    }

    public boolean isAdmin() {
        return user.isAdmin();
    }
}
