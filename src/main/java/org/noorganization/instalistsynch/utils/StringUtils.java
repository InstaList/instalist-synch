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

/**
 * Utils to manipulate {@link String}'s.
 * Created by tinos_000 on 29.01.2016.
 */
public class StringUtils {

    /**
     * Fetch the first value from a given json string.
     *
     * @param _jsonString the json string to extract the value from.
     * @return the extracted value.
     */
    public static String getFirstValueFromJSON(String _jsonString) {
        String[] splitString = _jsonString.split("\"");
        String value = null;
        if (splitString.length >= 4) {
            value = splitString[3];
        }
        return value;
    }
}
