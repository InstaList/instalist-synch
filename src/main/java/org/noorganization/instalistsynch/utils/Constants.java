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

import java.util.TimeZone;

/**
 * The constants class.
 * Created by Desnoo on 23.02.2016.
 */
public class Constants {

    /**
     * Initial date is Sat Sep 08 2001 00:00:00.
     */
    public static final long INITIAL_DATE = 999900000000L;

    public static final TimeZone TIME_ZONE = TimeZone.getTimeZone("GMT+0000");
    public static final long NETWORK_OFFSET = 100000L;
}
