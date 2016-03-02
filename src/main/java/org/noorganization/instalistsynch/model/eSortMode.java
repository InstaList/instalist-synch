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

package org.noorganization.instalistsynch.model;

/**
 * The mode to sort the entries.
 */
public enum eSortMode {
    ASC("ASC"),
    DESC("DESC");

    private final String mMode;

    /**
     * Constructor
     *
     * @param _string the string by sort mode
     */
    eSortMode(String _string) {
        mMode = _string;
    }

    @Override
    public String toString() {
        return this.mMode;
    }
}