/*
 * Copyright 2016 Tino Siegmund, Michael Wodniok
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.noorganization.instalistsynch.utils;

import android.util.Base64;
import android.util.Log;

import java.io.UnsupportedEncodingException;

/**
 * Utility to generate a RFC2617 conform authorization header.
 * Created by tinos_000 on 29.01.2016.
 */
public class RFC2617Authorization {
    private static final String LOG_TAG = RFC2617Authorization.class.getSimpleName();

    /**
     * Generates a RFC2617 authorization header.
     *
     * @param _userId   the user id.
     * @param _password the password for that user.
     * @return the base64 encoded string with "Basic " in front.
     */
    public static final String generate(int _userId, String _password) {
        try {
            return "Basic ".concat(Base64.encodeToString(String.valueOf(_userId).concat(":").concat(_password).getBytes("UTF-8"), Base64.NO_WRAP));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "generate: unsupported encoding", e.getCause());
        }
        return null;
    }
}
