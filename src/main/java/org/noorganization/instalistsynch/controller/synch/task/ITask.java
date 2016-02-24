package org.noorganization.instalistsynch.controller.synch.task;

/**
 * Task to process the synchronization.
 * Created by Desnoo on 14.02.2016.
 */
public interface ITask {

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
    int execute(int _resolveCode);

    String getServerUUID();
}
