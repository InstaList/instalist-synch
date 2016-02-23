package org.noorganization.instalistsynch.controller.synch.comparator;

import org.jetbrains.annotations.NotNull;

/**
 * Interface for the comperators to compare client models with server models.
 * Use this to compare not deleted elements.
 * Created by Desnoo on 16.02.2016.
 */
public interface ISynchComperator<T, S> {
    /**
     * Comparator for two different items.
     *
     * @param _param1 parameter 1 to be compared with param2.
     * @param _param2 parameter 2 to be compared with param1.
     * @return true if equal else if not.
     */
    boolean compare(@NotNull T _param1, @NotNull S _param2);
}
