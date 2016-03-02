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
 * Holds the indicator if a group member has read or write or both rights.
 * !! --> set both rights to true to indicate authorization else both to false <-- !!
 * !! Currently a placeholder for later integration!!
 *
 * Created by tinos_000 on 07.02.2016.
 */
public class AccessRight {

    private boolean mReadRight;
    private boolean mWriteRight;

    /**
     * Constructor of an AccessRight object.
     * @param readRight true if the read right should be set.
     * @param writeRight  true if the write right should be set.
     */
    public AccessRight(boolean readRight, boolean writeRight) {
        mReadRight = readRight;
        mWriteRight = writeRight;
    }

    public boolean hasReadRight() {
        return mReadRight;
    }

    public void setReadRight(boolean readRight) {
        mReadRight = readRight;
    }

    public boolean hasWriteRight() {
        return mWriteRight;
    }

    public void setWriteRight(boolean writeRight) {
        mWriteRight = writeRight;
    }


}
