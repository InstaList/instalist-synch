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

package org.noorganization.instalistsynch.controller.synch.task;

import org.noorganization.instalistsynch.controller.callback.ICallbackCompleted;

/**
 * Task to process the synchronization.
 * Created by Desnoo on 14.02.2016.
 */
public interface ITask<T> {

    public static class ReturnCodes {
        public final static int MERGE_CONFLICT = 0;
        public final static int SUCCESS = 1;
        public final static int PARSE_CONFLICT = 2;
        public final static int WAITING_FOR_RESOURCE = 3;
        public final static int INTERNAL_DB_ERROR = 4;
        /**
         * The integrity in the data holding is somewhat corrupted.
         */
        public final static int INTEGRITY_ERROR = 5;
        public final static int UNAUTHORIZED = 6;

    }

    public static class ResolveCodes {
        public final static int NO_RESOLVE = 0;
        public final static int RESOLVE_USE_CLIENT_SIDE = 1;
        public final static int RESOLVE_USE_SERVER_SIDE = 2;
    }

    /**
     * Executes the given command.
     *
     * @param _resolveCode use one of the {@link org.noorganization.instalistsynch.controller.synch.task.ITask.ResolveCodes}.
     * @return return codes
     */
    int executeSynch(int _resolveCode);

    void executeAsynch(int _resolveCode, ICallbackCompleted<T> _callback);

    String getServerUUID();

    eModelType getTaskModelType();


}
